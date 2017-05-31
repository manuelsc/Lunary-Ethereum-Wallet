package rehanced.com.simpleetherwallet.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rehanced.com.simpleetherwallet.activities.AddressDetailActivity;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.ResponseParser;

import static android.view.View.GONE;

public class FragmentTransactions extends FragmentTransactionsAbstract {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView  = super.onCreateView(inflater, container, savedInstanceState);
        send.setVisibility(GONE);
        requestTx.setVisibility(GONE);
        fabmenu.setVisibility(View.GONE);
        return rootView;
    }

    public void update(){
        if(ac == null) return;
        resetRequestCount();
        getWallets().clear();
        if(swipeLayout != null)
            swipeLayout.setRefreshing(true);

        try {
            EtherscanAPI.getInstance().getNormalTransactions(address, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete();
                            ((AddressDetailActivity)ac).snackError("No internet connection");
                        }
                    });
                }
                @Override
                public void onResponse(Response response) throws IOException {
                    final List<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(response.body().string(), "Unnamed Address", address, TransactionDisplay.NORMAL));

                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onComplete(w);
                        }
                    });
                }
            });
            EtherscanAPI.getInstance().getInternalTransactions(address, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onItemsLoadComplete();
                            ((AddressDetailActivity)ac).snackError("No internet connection");
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    final List<TransactionDisplay> w = new ArrayList<TransactionDisplay>(ResponseParser.parseTransactions(response.body().string(), "Unnamed Address", address, TransactionDisplay.CONTRACT));

                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onComplete(w);
                        }
                    });
                }
            });
        } catch (IOException e) {
            if(ac != null)
                ((AddressDetailActivity)ac).snackError("Can't fetch account balances. No connection?");
            onItemsLoadComplete();
            e.printStackTrace();
        };
    }

    private void onComplete(List<TransactionDisplay> w){
        addToWallets(w);
        addRequestCount();
        if (getRequestCount() >= 2) {
            onItemsLoadComplete();
            nothingToShow.setVisibility(wallets.size() == 0 ? View.VISIBLE : View.GONE);
            walletAdapter.notifyDataSetChanged();
        }
    }

}