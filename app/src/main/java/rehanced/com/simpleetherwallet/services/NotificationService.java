package rehanced.com.simpleetherwallet.services;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.Blockies;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

public class NotificationService extends IntentService {

    public NotificationService() {
        super("Notification Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("notifications_new_message", true) || WalletStorage.getInstance(this).get().size() <= 0) {
            NotificationLauncher.getInstance().stop();
            return;
        }

        try {
            EtherscanAPI.getInstance().getBalances(WalletStorage.getInstance(this).get(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    JSONArray data = null;
                    try {
                        data = new JSONObject(response.body().string()).getJSONArray("result");
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(NotificationService.this);

                        boolean notify = false;
                        BigInteger amount = new BigInteger("0");
                        String address = "";
                        SharedPreferences.Editor editor = preferences.edit();
                        for (int i = 0; i < data.length(); i++) {
                            if (!preferences.getString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance")).equals(data.getJSONObject(i).getString("balance"))) {
                                if (new BigInteger(preferences.getString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance"))).compareTo(new BigInteger(data.getJSONObject(i).getString("balance"))) < 1) { // Nur wenn höhere Balance als vorher
                                    notify = true;
                                    address = data.getJSONObject(i).getString("account");
                                    amount = amount.add((new BigInteger(data.getJSONObject(i).getString("balance")).subtract(new BigInteger(preferences.getString(address, "0")))));
                                }
                            }
                            editor.putString(data.getJSONObject(i).getString("account"), data.getJSONObject(i).getString("balance"));
                        }
                        editor.commit();
                        if (notify) {
                            try {
                                String amountS = new BigDecimal(amount).divide(ExchangeCalculator.ONE_ETHER, 4, BigDecimal.ROUND_DOWN).toPlainString();
                                sendNotification(address, amountS);
                            } catch (Exception e) {

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendNotification(String address, String amount) {
        String channelId = "6321";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(Blockies.createIcon(address.toLowerCase()))
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_ticker))
                .setLights(Color.CYAN, 3000, 3000)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentTitle(this.getResources().getString(R.string.notification_title))
                .setAutoCancel(true)
                .setContentText(amount + " ETH");

        if (android.os.Build.VERSION.SDK_INT >= 18) // Android bug in 4.2, just disable it for everyone then...
            builder.setVibrate(new long[]{1000, 1000});


        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, "Lunary", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.CYAN);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{1000, 1000});
            mNotifyMgr.createNotificationChannel(notificationChannel);
            builder.setChannelId(channelId);
        }

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("STARTAT", 2);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);

        final int mNotificationId = (int) (Math.random() * 150);
        mNotifyMgr.notify(mNotificationId, builder.build());
    }


}
