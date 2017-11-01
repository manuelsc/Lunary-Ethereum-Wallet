package rehanced.com.simpleetherwallet.activities;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Protects Activity with a password if user configured one
 */
public class SecureAppCompatActivity extends AppCompatActivity {

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AppLockActivity.REQUEST_CODE) {
            AppLockActivity.handleLockResponse(this, resultCode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppLockActivity.protectWithLock(this, true);
    }

    @Override
    public void onPause() {
        super.onPause();
        AppLockActivity.protectWithLock(this, false);
    }
}
