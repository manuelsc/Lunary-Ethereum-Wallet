package rehanced.com.simpleetherwallet.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;

public class AppLockUtils {

    public static boolean hasDeviceFingerprintSupport(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
            return fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
        } else {
            return false;
        }
    }

}
