package rehanced.com.simpleetherwallet.fragments;

import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.utils.Settings;

public class FragmentWallets extends FragmentWalletsAbstract{

    @Override
    public void adHandling(View rootView) {
        // Ads
        if (((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
            if (Settings.displayAds) {
                MobileAds.initialize(ac, "ca-app-pub-8285849835347571~6235180375");
                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            } else {
                mAdView.setVisibility(View.GONE);
            }
        }
    }
}