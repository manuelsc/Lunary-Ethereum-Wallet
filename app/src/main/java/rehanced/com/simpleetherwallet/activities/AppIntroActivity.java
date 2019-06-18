package rehanced.com.simpleetherwallet.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.Toast;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntroFragment;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.fragments.ToSFragment;


public class AppIntroActivity extends AppIntro2 {

    public static final int REQUEST_CODE = 602;
    private ToSFragment tosFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addSlide(AppIntroFragment.newInstance(getString(R.string.app_intro_title), getString(R.string.app_intro_text), R.drawable.ether_intro, Color.parseColor("#49627e")));
        tosFragment = new ToSFragment();
        addSlide(tosFragment);

        showSkipButton(false);
        setProgressButtonEnabled(true);
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        if (tosFragment.isToSChecked()) {
            Intent data = new Intent();
            data.putExtra("TOS", true);
            setResult(RESULT_OK, data);
            finish();
        } else
            Toast.makeText(this, R.string.app_intro_please_agree, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
        super.onSlideChanged(oldFragment, newFragment);
    }
}