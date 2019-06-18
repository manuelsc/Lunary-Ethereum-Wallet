package rehanced.com.simpleetherwallet.activities;

import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.widget.TextView;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.fragments.FragmentDetailOverview;
import rehanced.com.simpleetherwallet.fragments.FragmentDetailShare;
import rehanced.com.simpleetherwallet.fragments.FragmentTransactions;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;

public class AddressDetailActivity extends SecureAppCompatActivity {

    public static final byte OWN_WALLET = 0;
    public static final byte SCANNED_WALLET = 1;

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private Fragment[] fragments;
    private String address;
    private byte type;
    private TextView title;
    private CoordinatorLayout coord;
    private AppBarLayout appbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        address = getIntent().getStringExtra("ADDRESS");
        type = getIntent().getByteExtra("TYPE", SCANNED_WALLET);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        title = (TextView) findViewById(R.id.toolbar_title);
        String walletname = AddressNameConverter.getInstance(this).get(address);
        title.setText(type == OWN_WALLET ? (walletname == null ? "Unnamed Wallet" : walletname) : "Address");

        coord = (CoordinatorLayout) findViewById(R.id.main_content);
        appbar = (AppBarLayout) findViewById(R.id.appbar);

        fragments = new Fragment[3];
        fragments[0] = new FragmentDetailShare();
        fragments[1] = new FragmentDetailOverview();
        fragments[2] = new FragmentTransactions();
        Bundle bundle = new Bundle();
        bundle.putString("ADDRESS", address);
        bundle.putDouble("BALANCE", getIntent().getDoubleExtra("BALANCE", 0));
        bundle.putByte("TYPE", type);
        fragments[0].setArguments(bundle);
        fragments[1].setArguments(bundle);
        fragments[2].setArguments(bundle);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_action_share);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_wallet);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_transactions);
        mViewPager.setCurrentItem(1);

        mViewPager.setOffscreenPageLimit(3);
    }

    public void setTitle(String s) {
        if (title != null) {
            title.setText(s);
            Snackbar mySnackbar = Snackbar.make(coord,
                    AddressDetailActivity.this.getResources().getString(R.string.detail_acc_name_changed_suc), Snackbar.LENGTH_SHORT);
            mySnackbar.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void snackError(String s) {
        if (coord == null) return;
        Snackbar mySnackbar = Snackbar.make(coord, s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    public void broadCastDataSetChanged() {
        if (fragments != null && fragments[2] != null) {
            ((FragmentTransactions) fragments[2]).notifyDataSetChanged();
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    public AppBarLayout getAppBar() {
        return appbar;
    }

}
