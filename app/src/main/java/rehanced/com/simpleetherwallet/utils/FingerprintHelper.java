package rehanced.com.simpleetherwallet.utils;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.annotation.RequiresApi;

import rehanced.com.simpleetherwallet.interfaces.FingerprintListener;

@RequiresApi(api = Build.VERSION_CODES.M)
public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    private FingerprintListener listener;

    public FingerprintHelper(FingerprintListener listener) {
        this.listener = listener;
    }

    private CancellationSignal cancellationSignal;

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();

        try {
            manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
        } catch (SecurityException ex) {
            listener.authenticationFailed("An error occurred:\n" + ex.getMessage());
        } catch (Exception ex) {
            listener.authenticationFailed("An error occurred\n" + ex.getMessage());
        }
    }

    public void cancel() {
        if (cancellationSignal != null)
            cancellationSignal.cancel();
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        listener.authenticationFailed(errString.toString());
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        listener.authenticationFailed(helpString.toString());
    }

    @Override
    public void onAuthenticationFailed() {
        listener.authenticationFailed("Authentication failed");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        listener.authenticationSucceeded(result);
    }

}
