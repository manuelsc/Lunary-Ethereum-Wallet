package rehanced.com.simpleetherwallet.fragments;

import android.view.View;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;

import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.WalletStorage;


public class FragmentTransactionsAll extends FragmentTransactionsAbstract  {

    protected TransactionDisplay unconfirmed;
    private long unconfirmed_addedTime;

    public void update(){
        if(ac == null) return;
        getWallets().clear();
        if(swipeLayout != null)
            swipeLayout.setRefreshing(true);
        resetRequestCount();
        final ArrayList<StorableWallet> storedwallets = new ArrayList<StorableWallet>(WalletStorage.getInstance(ac).get());
        if(storedwallets.size() == 0){
            nothingToShow.setVisibility(View.VISIBLE);
            onItemsLoadComplete();
        } else {
            nothingToShow.setVisibility(View.GONE);
            for (int i = 0; i < storedwallets.size(); i++) {
                try {
                    final StorableWallet currentWallet = storedwallets.get(i);

                    EtherscanAPI.getInstance().getNormalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            ac.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onItemsLoadComplete();
                                    ((MainActivity)ac).snackError("No internet connection");
                                }
                            });
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(response.body().string(), "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.NORMAL));
                            ac.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onComplete(w, storedwallets);
                                }
                            });
                        }
                    });
                    EtherscanAPI.getInstance().getInternalTransactions(currentWallet.getPubKey(), new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            ac.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onItemsLoadComplete();
                                    ((MainActivity)ac).snackError("No internet connection");
                                }
                            });
                        }

                        @Override
                        public void onResponse(Response response) throws IOException {
                            final ArrayList<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(response.body().string(), "Unnamed Address", currentWallet.getPubKey(), TransactionDisplay.CONTRACT));
                            ac.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    onComplete(w, storedwallets);
                                }
                            });
                        }
                    });
                } catch (IOException e) {
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

    private void onComplete(ArrayList<TransactionDisplay> w, ArrayList<StorableWallet> storedwallets){
        addToWallets(w);
        addRequestCount();
        if (getRequestCount() >= storedwallets.size()*2) {
            onItemsLoadComplete();

            // If transaction was send via App and has no confirmations yet (Still show it when users refreshes for 10 minutes)
            if (unconfirmed_addedTime + 10 * 60 * 1000 < System.currentTimeMillis()) // After 10 minutes remove unconfirmed (should now have at least 1 confirmation anyway)
                unconfirmed = null;
            if (unconfirmed != null) {
                if(wallets.get(0).getAmount() == unconfirmed.getAmount()){
                    unconfirmed = null;
                } else {
                    wallets.add(0, unconfirmed);
                }
            }

            nothingToShow.setVisibility(wallets.size() == 0 ? View.VISIBLE : View.GONE);
            walletAdapter.notifyDataSetChanged();
        }
    }


    public void addUnconfirmedTransaction(String from, String to, BigInteger amount){
        unconfirmed = new TransactionDisplay(from, to, amount, 0, System.currentTimeMillis(), "", TransactionDisplay.NORMAL, "", "0" );
        unconfirmed_addedTime = System.currentTimeMillis();
        wallets.add(0, unconfirmed);
        notifyDataSetChanged();
    }

}