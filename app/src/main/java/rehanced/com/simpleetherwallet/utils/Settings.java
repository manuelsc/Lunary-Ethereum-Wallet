package rehanced.com.simpleetherwallet.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Manuel on 22.05.2017.
 */

public class Settings {

    public static boolean showTransactionsWithZero = false;

    public static boolean startWithWalletTab = false;

    public static boolean walletBeingGenerated = false;

    public static boolean displayAds = true;

    public static void initiate(Context c) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
        showTransactionsWithZero = prefs.getBoolean("zeroAmountSwitch", false);
        startWithWalletTab = prefs.getBoolean("startAtWallet", true);
    }

}
