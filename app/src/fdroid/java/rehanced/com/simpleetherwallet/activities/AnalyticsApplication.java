package rehanced.com.simpleetherwallet.activities;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

public class AnalyticsApplication extends Application {

    private boolean isGooglePlayBuild = false;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public boolean isGooglePlayBuild() {
        return isGooglePlayBuild;
    }

    public void track(String s) {
        return;
    }

    public void event(String s) {
        return;
    }

}