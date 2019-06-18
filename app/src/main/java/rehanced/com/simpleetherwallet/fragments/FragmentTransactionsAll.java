package rehanced.com.simpleetherwallet.fragments;

import android.os.Bundle;
import com.google.android.material.appbar.AppBarLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.AppBarStateChangeListener;
import rehanced.com.simpleetherwallet.utils.RequestCache;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

import static android.view.View.GONE;


public class FragmentTransactionsAll extends FragmentTransactionsAbstract {

    protected TransactionDisplay unconfirmed;
    private long unconfirmed_addedTime;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        MainActivity ac = (MainActivity) this.ac;
        if (ac != null && ac.getAppBar() != null) {
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
        return rootView;
    }


    public void update(boolean force) {
        if (ac == null) return;
        getWallets().clear();
        if (swipeLayout != null)
            swipeLayout.setRefreshing(true);
        resetRequestCount();
        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(ac).get());
        if (storedwallets.size() == 0) {
            nothingToShow.setVisibility(View.VISIBLE);
            onItemsLoadComplete();
        } else {
            nothingToShow.setVisibility(GONE);
            for (int i = 0; i < storedwallets.size(); i++) {
                try {
                    final StorableWallet currentWallet = storedwallets.get(i);

                    EtherscanAPI.getInstance().getNormalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onItemsLoadComplete();
                                        ((MainActivity) ac).snackError("No internet connection");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_NORMAL, currentWallet.getPubKey(), restring);
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.NORMAL));
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onComplete(w, storedwallets);
                                    }
                                });
                            }
                        }
                    }, force);
                    EtherscanAPI.getInstance().getInternalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onItemsLoadComplete();
                                        ((MainActivity) ac).snackError("No internet connection");
                                    }
                                });
                            }
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String restring = response.body().string();
                            if (restring != null && restring.length() > 2)
                                RequestCache.getInstance().put(RequestCache.TYPE_TXS_INTERNAL, currentWallet.getPubKey(), restring);
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(restring, "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.CONTRACT));
                            if (isAdded()) {
                                ac.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        onComplete(w, storedwallets);
                                    }
                                });
                            }
                        }
                    }, force);
                } catch (IOException e) {
                    if (isAdded()) {
                        if (ac != null)
                            ((MainActivity) ac).snackError("Can't fetch account balances. No connection?");

                        // So "if(getRequestCount() >= storedwallets.size()*2)" limit can be reached even if there are expetions for certain addresses (2x because of internal and normal)
                        addRequestCount();
                        addRequestCount();
                        onItemsLoadComplete();
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private void onComplete(ArrayList<TransactionDisplay> w, ArrayList<StorableWallet> storedwallets) {
        addToWallets(w);
        addRequestCount();
        if (getRequestCount() >= storedwallets.size() * 2) {
            onItemsLoadComplete();

            // If transaction was send via App and has no confirmations yet (Still show it when users refreshes for 10 minutes)
            if (unconfirmed_addedTime + 10 * 60 * 1000 < System.currentTimeMillis()) // After 10 minutes remove unconfirmed (should now have at least 1 confirmation anyway)
                unconfirmed = null;
            if (unconfirmed != null && wallets.size() > 0) {
                if (wallets.get(0).getAmount() == unconfirmed.getAmount()) {
                    unconfirmed = null;
                } else {
                    wallets.add(0, unconfirmed);
                }
            }

            nothingToShow.setVisibility(wallets.size() == 0 ? View.VISIBLE : GONE);
            walletAdapter.notifyDataSetChanged();
        }
    }


    public void addUnconfirmedTransaction(String from, String to, BigInteger amount) {
        unconfirmed = new TransactionDisplay(from, to, amount, 0, System.currentTimeMillis(), "", TransactionDisplay.NORMAL, "", "0", 0, 1, 1, false);
        unconfirmed_addedTime = System.currentTimeMillis();
        wallets.add(0, unconfirmed);
        notifyDataSetChanged();
    }

}