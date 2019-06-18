package rehanced.com.simpleetherwallet.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.interfaces.FingerprintListener;
import rehanced.com.simpleetherwallet.utils.AppLockUtils;
import rehanced.com.simpleetherwallet.utils.FingerprintHelper;

public class AppLockActivity extends BasePatternActivity implements PatternView.OnPatternListener, FingerprintListener {

    public static final int REQUEST_CODE = 1000;

    private int mNumFailedAttempts = 0;
    private LinearLayout fingerprintcontainer;
    private boolean hasFingerprintSupport = false;
    private FingerprintHelper fingerprintHelper;

    private KeyStore keyStore;
    private Cipher cipher;
    private FingerprintManager fingerprintManager;
    private FingerprintManager.CryptoObject cryptoObject;
    private boolean unlockedWithoutFprint = false;

    private SharedPreferences sharedPreferences;

    private static boolean pausedFirst = false;
    private static boolean unlockedFirst = false;

    public static void protectWithLock(Activity c, boolean onResume) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
        if (!preferences.getBoolean("use_app_lock", false)) return;

        Log.d("secureactivity", onResume + " ||>> " + unlockedFirst);
        if (!onResume && unlockedFirst) { //pausedFirst
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("APP_UNLOCKED", System.currentTimeMillis());
            editor.apply();
        }

        // Ask for login if pw protection is enabled and last login is more than 4 minutes ago
        if (preferences.getLong("APP_UNLOCKED", 0) <= System.currentTimeMillis() - 4 * 60 * 1000
                && onResume && !pausedFirst && !preferences.getString("APP_LOCK_PATTERN", "").equals("")) {
            Intent patternLock = new Intent(c, AppLockActivity.class);
            c.startActivityForResult(patternLock, AppLockActivity.REQUEST_CODE);
        }
        pausedFirst = onResume;
    }

    public static void handleLockResponse(Activity c, int resultCode) {
        if (resultCode != RESULT_OK) {
            c.finish();
            unlockedFirst = false;
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("APP_UNLOCKED", System.currentTimeMillis());
            editor.apply();
            unlockedFirst = true;
        }
    }

    public void onBackPressed() {
        unlockedFirst = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMessageText.setText(R.string.pl_draw_pattern_to_unlock);
        //mPatternView.setInStealthMode(true);
        mPatternView.setOnPatternListener(this);
        fingerprintcontainer = (LinearLayout) findViewById(R.id.fingerprintcontainer);
        hasFingerprintSupport = AppLockUtils.hasDeviceFingerprintSupport(this);

        if (hasFingerprintSupport()) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            setupFingerprintStuff();
        }

        fingerprintcontainer.setVisibility(hasFingerprintSupport ? View.VISIBLE : View.GONE);

        if (savedInstanceState == null) {
            mNumFailedAttempts = 0;
        } else {
            mNumFailedAttempts = savedInstanceState.getInt("num_failed_attempts");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void setupFingerprintStuff() {
        fingerprintManager = (FingerprintManager) this.getSystemService(Context.FINGERPRINT_SERVICE);
        fingerprintHelper = new FingerprintHelper(this);
        try {
            generateKey();

            if (cipherInit()) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                fingerprintHelper.startAuth(fingerprintManager, cryptoObject);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey("Lunary", null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder("Lunary",
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("num_failed_attempts", mNumFailedAttempts);
    }


    @Override
    public void onPatternDetected(List<PatternView.Cell> pattern) {
        if (sharedPreferences.getLong("WRONG_PATTERN_LOCK", 0) != 0 && sharedPreferences.getLong("WRONG_PATTERN_LOCK", 0) > System.currentTimeMillis() - 60 * 1000) {
            mMessageText.setText("Locked for 1 minute!");
            postClearPatternRunnable();
            return;
        }
        if (isPatternCorrect(pattern)) {
            unlockedWithoutFprint = true;
            mNumFailedAttempts = 0;
            onConfirmed();
        } else {
            mMessageText.setText(R.string.pl_wrong_pattern);
            mPatternView.setDisplayMode(PatternView.DisplayMode.Wrong);
            postClearPatternRunnable();
            onWrongPattern();
        }
    }

    public boolean hasFingerprintSupport() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasFingerprintSupport;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (fingerprintHelper != null && hasFingerprintSupport())
            fingerprintHelper.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sharedPreferences == null)
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (fingerprintHelper != null && hasFingerprintSupport())
            setupFingerprintStuff();
    }

    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        return PatternUtils.patternToSha1String(pattern).equals(sharedPreferences.getString("APP_LOCK_PATTERN", ""));
    }

    protected void onConfirmed() {
        setResult(RESULT_OK);
        finish();
    }

    protected void onWrongPattern() {
        ++mNumFailedAttempts;
        if (mNumFailedAttempts >= 5) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong("WRONG_PATTERN_LOCK", System.currentTimeMillis());
            editor.commit();
            mMessageText.setText("Locked for 1 minute!");
            mNumFailedAttempts = 0;
        }
    }

    @Override
    public void onPatternStart() {
        removeClearPatternRunnable();
        mPatternView.setDisplayMode(PatternView.DisplayMode.Correct);
    }

    @Override
    public void onPatternCleared() {
        removeClearPatternRunnable();
    }

    @Override
    public void onPatternCellAdded(List<PatternView.Cell> pattern) {
    }

    @Override
    public void authenticationFailed(String error) {
        Log.d("fingerprintauth", "FAILED: " + error);
        if (!unlockedWithoutFprint && !error.equals("Fingerprint operation cancelled."))
            Toast.makeText(this, "You are not authorized!", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void authenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        Log.d("fingerprintauth", "SUCCESS!");
        onConfirmed();
    }
}
