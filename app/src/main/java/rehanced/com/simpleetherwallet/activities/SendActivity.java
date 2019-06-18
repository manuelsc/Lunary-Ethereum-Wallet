package rehanced.com.simpleetherwallet.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.fragments.FragmentChooseRecipient;
import rehanced.com.simpleetherwallet.fragments.FragmentSend;
import rehanced.com.simpleetherwallet.views.NonSwipeViewPager;

public class SendActivity extends SecureAppCompatActivity {

    public static final int REQUEST_CODE = 200;

    private NonSwipeViewPager mViewPager;
    private Fragment[] fragments;

    private TextView title;
    private CoordinatorLayout coord;
    FragmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chooserecepient);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (TextView) findViewById(R.id.toolbar_title);

        coord = (CoordinatorLayout) findViewById(R.id.main_content);

        fragments = new Fragment[2];
        fragments[0] = new FragmentChooseRecipient();
        fragments[1] = new FragmentSend();
        Bundle bundle = new Bundle();

        if (getIntent().hasExtra("TO_ADDRESS"))
            bundle.putString("TO_ADDRESS", getIntent().getStringExtra("TO_ADDRESS"));
        if (getIntent().hasExtra("AMOUNT"))
            bundle.putString("AMOUNT", getIntent().getStringExtra("AMOUNT"));
        if (getIntent().hasExtra("FROM_ADDRESS"))
            bundle.putString("FROM_ADDRESS", getIntent().getStringExtra("FROM_ADDRESS"));

        fragments[1].setArguments(bundle);

        adapter = new FragmentAdapter(getSupportFragmentManager());

        mViewPager = (NonSwipeViewPager) findViewById(R.id.container);
        mViewPager.setPagingEnabled(false);
        mViewPager.setAdapter(adapter);

        if (getIntent().hasExtra("TO_ADDRESS"))
            mViewPager.setCurrentItem(1);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == QRScanActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (fragments == null || fragments[0] == null) return;
                ((FragmentChooseRecipient) fragments[0]).setRecipientAddress(data.getStringExtra("ADDRESS"));
            } else {
                Snackbar mySnackbar = Snackbar.make(coord,
                        this.getResources().getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        }
    }

    public void nextStage(String toAddress) {
        mViewPager.setCurrentItem(1);

        if (fragments == null || fragments[1] == null) return;
        ((FragmentSend) fragments[1]).setToAddress(toAddress, this);
    }


    class FragmentAdapter extends FragmentPagerAdapter {

        private FragmentManager mFragmentManager;

        public FragmentAdapter(FragmentManager fm) {
            super(fm);
            mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void setTitle(String s) {
        if (title != null) {
            title.setText(s);
            Snackbar mySnackbar = Snackbar.make(coord,
                    SendActivity.this.getResources().getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void snackError(String s, int length) {
        if (coord == null) return;
        Snackbar mySnackbar = Snackbar.make(coord, s, length);
        mySnackbar.show();
    }

    public void snackError(String s) {
        snackError(s, Snackbar.LENGTH_SHORT);
    }

}
