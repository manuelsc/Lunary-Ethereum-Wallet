package rehanced.com.simpleetherwallet.utils;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import rehanced.com.simpleetherwallet.R;

/**
 * Created by Manuel on 12.11.2017.
 */

public class AdRecyclerHolder extends RecyclerView.ViewHolder {
    AdView ad;
    public AdRecyclerHolder(View view) {
        super(view);
        ad = (AdView) view.findViewById(R.id.adView);
    }

    void loadAd(Context c){
        if(c == null) return;
        if (Settings.displayAds) {
            MobileAds.initialize(c, "ca-app-pub-8285849835347571~6235180375");
            AdRequest adRequest = new AdRequest.Builder().build();
            ad.loadAd(adRequest);
        }
    }

}
