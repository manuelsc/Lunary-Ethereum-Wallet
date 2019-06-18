package rehanced.com.simpleetherwallet.fragments;

/**
 * Created by Manuel on 01.11.2017.
 */

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import com.google.android.material.appbar.AppBarLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AddressDetailActivity;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.activities.PrivateKeyActivity;
import rehanced.com.simpleetherwallet.activities.QRScanActivity;
import rehanced.com.simpleetherwallet.activities.WalletGenActivity;
import rehanced.com.simpleetherwallet.data.CurrencyEntry;
import rehanced.com.simpleetherwallet.data.WalletDisplay;
import rehanced.com.simpleetherwallet.interfaces.PasswordDialogCallback;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.AppBarStateChangeListener;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.Settings;
import rehanced.com.simpleetherwallet.utils.WalletAdapter;
import rehanced.com.simpleetherwallet.utils.WalletStorage;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.PrefsManager;
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;

import static uk.co.deanwild.materialshowcaseview.PrefsManager.SEQUENCE_NEVER_STARTED;

public abstract class FragmentWalletsAbstract extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener {

    protected RecyclerView recyclerView;
    protected WalletAdapter walletAdapter;
    protected List<WalletDisplay> wallets = new ArrayList<>();
    protected MainActivity ac;
    double balance = 0;
    protected TextView balanceView;
    protected SwipeRefreshLayout swipeLayout;
    protected FrameLayout nothingToShow;
    protected FloatingActionMenu fabmenu;
    protected FloatingActionButton gen_fab;

    public abstract void adHandling(View rootView);

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallets, container, false);

        ac = (MainActivity) this.getActivity();

        adHandling(rootView);

        nothingToShow = (FrameLayout) rootView.findViewById(R.id.nothing_found);
        ImageView leftPress = (ImageView) rootView.findViewById(R.id.wleft);
        ImageView rightPress = (ImageView) rootView.findViewById(R.id.wright);
        balanceView = (TextView) rootView.findViewById(R.id.balance);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setColorSchemeColors(ac.getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                balance = 0;
                try {
                    update();
                } catch (IOException e) {
                    if (ac != null)
                        ac.snackError("Can't fetch account balances. No connection?");
                    e.printStackTrace();
                }
            }
        });

        ExchangeCalculator.getInstance().setIndex(ac.getPreferences().getInt("main_index", 0));

        leftPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrencyEntry cur = ExchangeCalculator.getInstance().previous();
                balanceView.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(balance, cur.getRate())) + " " + cur.getName());
                ac.broadCastDataSetChanged();
                walletAdapter.notifyDataSetChanged();
                SharedPreferences.Editor editor = ac.getPreferences().edit();
                editor.putInt("main_index", ExchangeCalculator.getInstance().getIndex());
                editor.apply();
            }
        });

        rightPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrencyEntry cur = ExchangeCalculator.getInstance().next();
                balanceView.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(balance, cur.getRate())) + " " + cur.getName());
                ac.broadCastDataSetChanged();
                walletAdapter.notifyDataSetChanged();
                SharedPreferences.Editor editor = ac.getPreferences().edit();
                editor.putInt("main_index", ExchangeCalculator.getInstance().getIndex());
                editor.apply();
            }
        });


        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        walletAdapter = new WalletAdapter(wallets, ac, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(ac.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        walletAdapter.notifyDataSetChanged();

        fabmenu = (FloatingActionMenu) rootView.findViewById(R.id.fabmenu);
        FloatingActionButton scan_fab = (FloatingActionButton) rootView.findViewById(R.id.scan_fab);
        FloatingActionButton add_fab = (FloatingActionButton) rootView.findViewById(R.id.add_fab);
        gen_fab = (FloatingActionButton) rootView.findViewById(R.id.gen_fab);

        gen_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateDialog();
            }
        });

        scan_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent scanQR = new Intent(ac, QRScanActivity.class);
                scanQR.putExtra("TYPE", QRScanActivity.SCAN_ONLY);
                ac.startActivityForResult(scanQR, QRScanActivity.REQUEST_CODE);
            }
        });
        add_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialogs.addWatchOnly(ac);
            }
        });

        if (ac != null && ac.getAppBar() != null) {
            ac.getAppBar().addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, AppBarStateChangeListener.State state) {
                    if (state == State.COLLAPSED) {
                        fabmenu.hideMenu(true);
                    } else {
                        fabmenu.showMenu(true);
                    }
                }
            });
        }

        try {
            update();
        } catch (IOException e) {
            if (ac != null)
                ac.snackError("Can't fetch account balances. No connection?");
        }

        if (((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).track("Wallet Fragment");
        }

        return rootView;
    }

    public void update() throws IOException {
        if (ac == null) return;
        wallets.clear();
        balance = 0;
        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(ac).get());

        if (storedwallets.size() == 0) {
            nothingToShow.setVisibility(View.VISIBLE);
            onItemsLoadComplete();
        } else {
            nothingToShow.setVisibility(View.GONE);
            EtherscanAPI.getInstance().getBalances(storedwallets, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (ac != null)
                        ac.snackError("Can't fetch account balances. Invalid response.");
                    final List<WalletDisplay> w = new ArrayList<WalletDisplay>();
                    for (StorableWallet cur : storedwallets)
                        w.add(new WalletDisplay(AddressNameConverter.getInstance(ac).get(cur.getPubKey()), cur.getPubKey(), new BigInteger("-1"), WalletDisplay.NORMAL));

                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wallets.addAll(w);
                            walletAdapter.notifyDataSetChanged();
                            onItemsLoadComplete();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final List<WalletDisplay> w;
                    try {
                        w = ResponseParser.parseWallets(response.body().string(), storedwallets, ac);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            wallets.addAll(w);
                            walletAdapter.notifyDataSetChanged();
                            for (int i = 0; i < wallets.size(); i++) {
                                balance += wallets.get(i).getBalance();
                            }
                            balanceView.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(balance, ExchangeCalculator.getInstance().getCurrent().getRate())) + " " + ExchangeCalculator.getInstance().getCurrent().getName());
                            onItemsLoadComplete();
                        }
                    });
                }
            });
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.wallet_menu_title);
        menu.add(0, 200, 0, R.string.wallet_menu_changename);
        menu.add(0, 201, 0, R.string.wallet_menu_copyadd);
        menu.add(0, 202, 0, R.string.wallet_menu_share);
        menu.add(0, 203, 0, R.string.wallet_menu_export);
        menu.add(0, 204, 0, R.string.wallet_menu_private_key);
        menu.add(0, 205, 0, R.string.wallet_menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = walletAdapter.getPosition();
        } catch (Exception e) {
            e.printStackTrace();
            return super.onContextItemSelected(item);
        }

        switch (item.getItemId()) {
            case 200: // Change Address Name
                setName(wallets.get(position).getPublicKey());
                break;
            case 201:
                if (ac == null) return true;
                ClipboardManager clipboard = (ClipboardManager) ac.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", wallets.get(position).getPublicKey());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(ac, R.string.wallet_menu_action_copied_to_clipboard, Toast.LENGTH_SHORT).show();
                break;
            case 202:
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, wallets.get(position).getPublicKey());
                startActivity(Intent.createChooser(i, "Share via"));
                break;
            case 203:
                final int finalPosition = position;
                if (wallets.get(finalPosition).getType() == WalletDisplay.NORMAL) {
                    Dialogs.exportWallet(ac, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WalletStorage.getInstance(ac).setWalletForExport(wallets.get(finalPosition).getPublicKey());
                            export();
                            dialog.dismiss();
                        }
                    });
                } else {
                    Dialogs.cantExportNonWallet(ac);
                }
                break;
            case 204:
                final int finalPosition2 = position;
                if (wallets.get(finalPosition2).getType() == WalletDisplay.NORMAL) {
                    Dialogs.askForPasswordAndDecode(ac, wallets.get(finalPosition2).getPublicKey(), new PasswordDialogCallback(){
                        @Override
                        public void success(String password) {
                            Intent i = new Intent(ac, PrivateKeyActivity.class);
                            i.putExtra(PrivateKeyActivity.PASSWORD, password);
                            i.putExtra(PrivateKeyActivity.ADDRESS,  wallets.get(finalPosition2).getPublicKey());
                            startActivity(i);
                        }

                        @Override
                        public void canceled() {}
                    });
                } else {
                    Dialogs.cantExportNonWallet(ac);
                }
                break;
            case 205:
                confirmDelete(wallets.get(position).getPublicKey(), wallets.get(position).getType());
                break;
        }
        return super.onContextItemSelected(item);
    }

    public void export() {
        boolean suc = WalletStorage.getInstance(ac).exportWallet(ac);
        if (suc) ac.snackError(getString(R.string.wallet_suc_exported));
        else ac.snackError(getString(R.string.wallet_no_permission));
    }

    public void generateDialog() {
        if (!Settings.walletBeingGenerated) {
            Intent genI = new Intent(ac, WalletGenActivity.class);
            ac.startActivityForResult(genI, WalletGenActivity.REQUEST_CODE);
        } else {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
                builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
            else
                builder = new AlertDialog.Builder(ac);
            builder.setTitle(R.string.wallet_one_at_a_time);
            builder.setMessage(R.string.wallet_one_at_a_time_text);
            builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void confirmDelete(final String address, final byte type) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(ac);
        builder.setTitle(R.string.wallet_removal_title);

        if (type == WalletDisplay.WATCH_ONLY)
            builder.setMessage(R.string.wallet_removal_sure);
        else if (type == WalletDisplay.NORMAL)
            builder.setMessage(getString(R.string.wallet_removal_privkey) + address);
        else if (type == Byte.MAX_VALUE) {
            builder.setMessage(getString(R.string.wallet_removal_last_warning) + address);
        }
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (type == WalletDisplay.WATCH_ONLY || type == Byte.MAX_VALUE) {
                    WalletStorage.getInstance(ac).removeWallet(address, ac);
                    dialog.dismiss();
                    try {
                        update();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    confirmDelete(address, Byte.MAX_VALUE);
                }
            }
        });
        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();

    }

    public void setName(final String address) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(ac);
        builder.setTitle(R.string.name_your_wallet);

        final EditText input = new EditText(ac);
        input.setText(AddressNameConverter.getInstance(ac).get(address));
        input.setSingleLine();
        FrameLayout container = new FrameLayout(ac);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        input.setSelection(input.getText().length());

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        container.addView(input);
        builder.setView(container);
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    InputMethodManager inputMgr = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AddressNameConverter.getInstance(ac).put(address, input.getText().toString(), ac);
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        builder.show();

    }

    void onItemsLoadComplete() {
        if (swipeLayout == null) return;
        swipeLayout.setRefreshing(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                handleShowCase();
            }
        }, 1000);
    }

    private void handleShowCase(){
        if(ac.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_BACKUP_WALLET", SEQUENCE_NEVER_STARTED) != PrefsManager.SEQUENCE_FINISHED
                && ac.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_GENERATE_WALLET", SEQUENCE_NEVER_STARTED) == PrefsManager.SEQUENCE_FINISHED
                && recyclerView != null && recyclerView.getChildCount() > 0) {
            ac.setSelectedPage(1);
            new MaterialShowcaseView.Builder(ac)
                    .setTarget(recyclerView.getChildAt(0))
                    .setDismissText(getString(R.string.showcase_got_it))
                    .setContentText(getString(R.string.show_case_backup))
                    .setDelay(150)
                    .setDismissOnTargetTouch(true)
                    .setDismissOnTouch(true)
                    .setShape(new RectangleShape(new Rect(), true))
                    .singleUse("BACKUP_WALLET")
                    .show();
        }

        if(ac.getSharedPreferences("material_showcaseview_prefs", 0).getInt("status_GENERATE_WALLET", SEQUENCE_NEVER_STARTED) != PrefsManager.SEQUENCE_FINISHED) {
            ac.setSelectedPage(1);
            fabmenu.open(true);
            new MaterialShowcaseView.Builder(ac)
                    .setTarget(gen_fab)
                    .setDismissText(getString(R.string.showcase_got_it))
                    .setContentText(getString(R.string.show_case_wallet_gen_text))
                    .setDelay(150)
                    .setDismissOnTargetTouch(true)
                    .setDismissOnTouch(true)
                    .singleUse("GENERATE_WALLET")
                    .show();
        }
    }

    public void notifyDataSetChanged() {
        if (walletAdapter != null)
            walletAdapter.notifyDataSetChanged();
        updateBalanceText();
    }

    public void updateBalanceText() {
        if (balanceView != null)
            balanceView.setText(ExchangeCalculator.getInstance().displayBalanceNicely(ExchangeCalculator.getInstance().convertRate(balance, ExchangeCalculator.getInstance().getCurrent().getRate())) + " " + ExchangeCalculator.getInstance().getCurrent().getName());
    }

    public int getDisplayedWalletCount() {
        return wallets.size();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        if (itemPosition >= wallets.size()) return;
        Intent detail = new Intent(ac, AddressDetailActivity.class);
        detail.putExtra("ADDRESS", wallets.get(itemPosition).getPublicKey());
        detail.putExtra("BALANCE", wallets.get(itemPosition).getBalance());
        detail.putExtra("TYPE", AddressDetailActivity.OWN_WALLET);
        startActivity(detail);
    }
}