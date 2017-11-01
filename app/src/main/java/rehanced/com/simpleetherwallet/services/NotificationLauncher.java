package rehanced.com.simpleetherwallet.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import rehanced.com.simpleetherwallet.utils.WalletStorage;


public class NotificationLauncher {

    private PendingIntent pintent;
    private AlarmManager service;
    private static NotificationLauncher instance;

    private NotificationLauncher() {
    }

    public void start(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        if (prefs.getBoolean("notifications_new_message", true) && WalletStorage.getInstance(c).get().size() >= 1) {
            service = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            Intent i = new Intent(c, NotificationService.class);
            pintent = PendingIntent.getService(c, 23, i, 0);

            int syncInt = Integer.parseInt(prefs.getString("sync_frequency", "4"));

            service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR * syncInt, pintent);
        }
    }

    public void stop() {
        if (service == null || pintent == null) return;
        service.cancel(pintent);
    }

    public static NotificationLauncher getInstance() {
        if (instance == null)
            instance = new NotificationLauncher();
        return instance;
    }

}
