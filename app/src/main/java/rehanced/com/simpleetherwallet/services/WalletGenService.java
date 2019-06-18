package rehanced.com.simpleetherwallet.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.app.NotificationCompat;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;

import java.io.File;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.data.FullWallet;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.Blockies;
import rehanced.com.simpleetherwallet.utils.OwnWalletUtils;
import rehanced.com.simpleetherwallet.utils.Settings;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

public class WalletGenService extends IntentService {

    private NotificationCompat.Builder builder;
    final int mNotificationId = 152;

    private boolean normalMode = true;

    public WalletGenService() {
        super("WalletGen Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String password = intent.getStringExtra("PASSWORD");
        String privatekey = "";

        if (intent.hasExtra("PRIVATE_KEY")) {
            normalMode = false;
            privatekey = intent.getStringExtra("PRIVATE_KEY");
        }

        sendNotification();
        try {
            String walletAddress;
            if (normalMode) { // Create new key
                walletAddress = OwnWalletUtils.generateNewWalletFile(password, new File(this.getFilesDir(), ""), true);
            } else { // Privatekey passed
                ECKeyPair keys = ECKeyPair.create(Hex.decode(privatekey));
                walletAddress = OwnWalletUtils.generateWalletFile(password, keys, new File(this.getFilesDir(), ""), true);
            }

            WalletStorage.getInstance(this).add(new FullWallet("0x" + walletAddress, walletAddress), this);
            AddressNameConverter.getInstance(this).put("0x" + walletAddress, "Wallet " + ("0x" + walletAddress).substring(0, 6), this);
            Settings.walletBeingGenerated = false;

            finished("0x" + walletAddress);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
    }

    private void sendNotification() {
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x2d435c)
                .setTicker(normalMode ? getString(R.string.notification_wallgen_title) : getString(R.string.notification_wallimp_title))
                .setContentTitle(this.getResources().getString(normalMode ? R.string.wallet_gen_service_title : R.string.wallet_gen_service_title_import))
                .setOngoing(true)
                .setProgress(0, 0, true)
                .setContentText(getString(R.string.notification_wallgen_maytake));
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void finished(String address) {
        builder
                .setContentTitle(normalMode ? getString(R.string.notification_wallgen_finished) : getString(R.string.notification_wallimp_finished))
                .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
                .setAutoCancel(true)
                .setLights(Color.CYAN, 3000, 3000)
                .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(getString(R.string.notification_click_to_view));

        if (android.os.Build.VERSION.SDK_INT >= 18) // Android bug in 4.2, just disable it for everyone then...
            builder.setVibrate(new long[]{1000, 1000});

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("STARTAT", 1);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }


}
