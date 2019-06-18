package rehanced.com.simpleetherwallet.activities;


import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.interfaces.AdDialogResponseHandler;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.Settings;

public class SettingsActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PrefsFragment()).commit();
    }

    private void setupActionBar() {
        ViewGroup rootView = (ViewGroup) findViewById(R.id.action_bar_root); //id from appcompat

        if (rootView != null) {
            View view = getLayoutInflater().inflate(R.layout.activity_settings, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


    public static class PrefsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.pref_general);

            final SwitchPreference zeroAmountTx = (SwitchPreference) findPreference("zeroAmountSwitch");
            zeroAmountTx.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Settings.showTransactionsWithZero = !zeroAmountTx.isChecked();
                    return true;
                }
            });
            if (((AnalyticsApplication) getActivity().getApplication()).isGooglePlayBuild()) {
                final SwitchPreference adSwitch = (SwitchPreference) findPreference("showAd");
                adSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        if (adSwitch.isChecked()) {
                            Dialogs.adDisable(getActivity(), new AdDialogResponseHandler() {
                                @Override
                                public void continueSettingChange(boolean mContinue) {
                                    if (mContinue) {
                                        adSwitch.setChecked(false);
                                        Settings.displayAds = false;
                                    }
                                }
                            });
                            return false;
                        } else {
                            Settings.displayAds = true;
                            return true;
                        }
                    }
                });
            }
        }
    }

}