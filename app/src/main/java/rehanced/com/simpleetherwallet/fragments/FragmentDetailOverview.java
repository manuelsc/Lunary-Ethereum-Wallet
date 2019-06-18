package rehanced.com.simpleetherwallet.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AddressDetailActivity;
import rehanced.com.simpleetherwallet.activities.SendActivity;
import rehanced.com.simpleetherwallet.data.CurrencyEntry;
import rehanced.com.simpleetherwallet.data.TokenDisplay;
import rehanced.com.simpleetherwallet.data.WatchWallet;
import rehanced.com.simpleetherwallet.interfaces.LastIconLoaded;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.AppBarStateChangeListener;
import rehanced.com.simpleetherwallet.utils.Blockies;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.RequestCache;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.TokenAdapter;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

public class FragmentDetailOverview extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener, LastIconLoaded {

    private AddressDetailActivity ac;
    private String ethaddress = "";
    private byte type;
    private TextView balance, address, currency;
    private ImageView icon;
    private LinearLayout header;
    BigDecimal balanceDouble = new BigDecimal("0");
    private FloatingActionMenu fabmenu;
    private RecyclerView recyclerView;
    private TokenAdapter walletAdapter;
    private List<TokenDisplay> token = new ArrayList<>();
    private SwipeRefreshLayout swipeLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_ov, container, false);

        ac = (AddressDetailActivity) this.getActivity();
        ethaddress = getArguments().getString("ADDRESS");
        type = getArguments().getByte("TYPE");

        icon = (ImageView) rootView.findViewById(R.id.addressimage);
        address = (TextView) rootView.findViewById(R.id.ethaddress);
        balance = (TextView) rootView.findViewById(R.id.balance);
        currency = (TextView) rootView.findViewById(R.id.currency);
        header = (LinearLayout) rootView.findViewById(R.id.header);
        fabmenu = (FloatingActionMenu) rootView.findViewById(R.id.fabmenu);

        CurrencyEntry cur = ExchangeCalculator.getInstance().getCurrent();
        balanceDouble = new BigDecimal(getArguments().getDouble("BALANCE"));
        balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate()) + "");
        currency.setText(cur.getName());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        walletAdapter = new TokenAdapter(token, ac, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(ac.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout2);
        swipeLayout.setColorSchemeColors(ac.getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    update(true);
                } catch (IOException e) {
                    if (ac != null)
                        ac.snackError("Connection problem");
                    e.printStackTrace();
                }
            }
        });

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrencyEntry cur = ExchangeCalculator.getInstance().next();
                balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate()) + "");
                currency.setText(cur.getName());
                walletAdapter.notifyDataSetChanged();
                if (ac != null)
                    ac.broadCastDataSetChanged();
            }
        });

        icon.setImageBitmap(Blockies.createIcon(ethaddress, 24));
        address.setText(ethaddress);

        FloatingActionButton fab_setName = (FloatingActionButton) rootView.findViewById(R.id.set_name);
        fab_setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setName();
            }
        });

        FloatingActionButton send_ether = (FloatingActionButton) rootView.findViewById(R.id.send_ether); // Send Ether to
        send_ether.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WalletStorage.getInstance(ac).getFullOnly().size() == 0) {
                    Dialogs.noFullWallet(ac);
                } else {
                    Intent tx = new Intent(ac, SendActivity.class);
                    tx.putExtra("TO_ADDRESS", ethaddress);
                    ac.startActivityForResult(tx, SendActivity.REQUEST_CODE);
                }
            }
        });

        FloatingActionButton send_ether_from = (FloatingActionButton) rootView.findViewById(R.id.send_ether_from);
        send_ether_from.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (WalletStorage.getInstance(ac).getFullOnly().size() == 0) {
                    Dialogs.noFullWallet(ac);
                } else {
                    Intent tx = new Intent(ac, SendActivity.class);
                    tx.putExtra("FROM_ADDRESS", ethaddress);
                    ac.startActivityForResult(tx, SendActivity.REQUEST_CODE);
                }
            }
        });

        FloatingActionButton fab_add = (FloatingActionButton) rootView.findViewById(R.id.add_as_watch);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean suc = WalletStorage.getInstance(ac).add(new WatchWallet(ethaddress), ac);
                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                ac.snackError(ac.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er));
                            }
                        }, 100);
            }
        });

        if (type == AddressDetailActivity.OWN_WALLET) {
            fab_add.setVisibility(View.GONE);
        }
        if (!WalletStorage.getInstance(ac).isFullWallet(ethaddress)) {
            send_ether_from.setVisibility(View.GONE);
        }

        if (ac.getAppBar() != null) {
            ac.getAppBar().addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        fabmenu.hideMenu(true);
                    } else {
                        fabmenu.showMenu(true);
                    }
                }
            });
        }
        try {
            update(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }


    public void update(boolean force) throws IOException {
        token.clear();
        balanceDouble = new BigDecimal("0");
        EtherscanAPI.getInstance().getBalance(ethaddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ac.snackError("Can't connect to network");
                        onItemsLoadComplete();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                BigDecimal ethbal;
                try {
                    ethbal = new BigDecimal(ResponseParser.parseBalance(response.body().string()));
                    token.add(0, new TokenDisplay("Ether", "ETH", ethbal.multiply(new BigDecimal(1000d)), 3, 1, "", "", 0, 0));
                    balanceDouble = balanceDouble.add(ethbal);
                } catch (JSONException e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete();
                        }
                    });
                    e.printStackTrace();
                }
                final CurrencyEntry cur = ExchangeCalculator.getInstance().getCurrent();
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, ExchangeCalculator.getInstance().get));
                        balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate()) + "");
                        currency.setText(cur.getName());
                        walletAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        EtherscanAPI.getInstance().getTokenBalances(ethaddress, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ac.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ac.snackError("Can't connect to network");
                        onItemsLoadComplete();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String restring = response.body().string();
                    if (restring != null && restring.length() > 2)
                        RequestCache.getInstance().put(RequestCache.TYPE_TOKEN, ethaddress, restring);
                    token.addAll(ResponseParser.parseTokens(ac, restring, FragmentDetailOverview.this));

                    balanceDouble = balanceDouble.add(new BigDecimal(ExchangeCalculator.getInstance().sumUpTokenEther(token)));

                    final CurrencyEntry cur = ExchangeCalculator.getInstance().getCurrent();
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate()) + "");
                            currency.setText(cur.getName());
                            walletAdapter.notifyDataSetChanged();
                            onItemsLoadComplete();
                        }
                    });
                } catch (Exception e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete();
                        }
                    });
                    //ac.snackError("Invalid server response");
                }
            }
        }, force);
    }

    public void setName() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(ac);
        if (type == AddressDetailActivity.OWN_WALLET)
            builder.setTitle(R.string.name_your_address);
        else
            builder.setTitle(R.string.name_this_address);

        final EditText input = new EditText(ac);
        input.setText(AddressNameConverter.getInstance(ac).get(ethaddress));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(ac);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        input.setSelection(input.getText().length());

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
                InputMethodManager inputMgr = (InputMethodManager) input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                AddressNameConverter.getInstance(ac).put(ethaddress, input.getText().toString(), ac);
                ac.setTitle(input.getText().toString());

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
    }

    @Override
    public void onClick(View view) {
        if (ac == null) return;
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        if (itemPosition == 0 || itemPosition >= token.size()) return;  // if clicked on Ether
        Dialogs.showTokenetails(ac, token.get(itemPosition));
    }

    @Override
    public void onLastIconDownloaded() {
        if (walletAdapter != null && ac != null) {
            ac.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    walletAdapter.notifyDataSetChanged();
                }
            });
        }
    }
}