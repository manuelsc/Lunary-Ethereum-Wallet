package rehanced.com.simpleetherwallet.activities;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import rehanced.com.simpleetherwallet.R;

public class AnalyticsApplication extends Application {
    private Tracker mTracker;

    private boolean isGooglePlayBuild = true;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public boolean isGooglePlayBuild() {
        return isGooglePlayBuild;
    }

    synchronized public void track(String s) {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        mTracker.setScreenName(s);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public void event(String s) {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction(s)
                .build());
    }

}