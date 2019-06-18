package rehanced.com.simpleetherwallet.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AddressDetailActivity;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.activities.RequestEtherActivity;
import rehanced.com.simpleetherwallet.activities.SendActivity;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.TransactionAdapter;
import rehanced.com.simpleetherwallet.utils.WalletStorage;


public abstract class FragmentTransactionsAbstract extends Fragment implements View.OnClickListener, View.OnCreateContextMenuListener {

    protected RecyclerView recyclerView;
    protected TransactionAdapter walletAdapter;
    protected List<TransactionDisplay> wallets = new ArrayList<>();

    protected Activity ac;
    protected String address;
    protected SwipeRefreshLayout swipeLayout;
    protected int requestCount = 0;  // used to count to two (since internal and normal transactions are each one request). Gets icnreased once one request is finished. If it is two, notifyDataChange is called (display transactions)
    protected FloatingActionButton requestTx;
    protected FloatingActionButton send;
    protected FrameLayout nothingToShow;
    protected FloatingActionMenu fabmenu;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_transaction, container, false);

        ac = this.getActivity();
        if (getArguments() != null) {
            address = getArguments().getString("ADDRESS");
            ((TextView) rootView.findViewById(R.id.infoText)).setText(R.string.trans_no_trans_found);
        }

        nothingToShow = (FrameLayout) rootView.findViewById(R.id.nothing_found);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        walletAdapter = new TransactionAdapter(wallets, ac, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(ac.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        swipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout2);
        swipeLayout.setColorSchemeColors(ac.getResources().getColor(R.color.colorPrimary));
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                update(true);
            }
        });

        send = (FloatingActionButton) rootView.findViewById(R.id.newTransaction);
        requestTx = (FloatingActionButton) rootView.findViewById(R.id.requestTx);
        fabmenu = (FloatingActionMenu) rootView.findViewById(R.id.fabmenu);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (address != null) return;
                if (dy > 0)
                    fabmenu.hideMenu(true);
                else if (dy < 0)
                    fabmenu.showMenu(true);
            }
        });


        requestTx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRequestActivity();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSendActivity();
            }
        });

        update(false);
        walletAdapter.notifyDataSetChanged();

        if (((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).track("Transaction Fragment");
        }
        return rootView;
    }

    private void openSendActivity() {
        if (WalletStorage.getInstance(ac).getFullOnly().size() == 0) {
            Dialogs.noFullWallet(ac);
        } else {
            Intent newTrans = new Intent(ac, SendActivity.class);
            if (address != null)
                newTrans.putExtra("FROM_ADDRESS", address);
            ac.startActivityForResult(newTrans, SendActivity.REQUEST_CODE);
        }
    }

    private void openRequestActivity() {
        if (WalletStorage.getInstance(ac).get().size() == 0) {
            Dialogs.noWallet(ac);
        } else {
            Intent newTrans = new Intent(ac, RequestEtherActivity.class);
            ac.startActivity(newTrans);
        }
    }

    public void notifyDataSetChanged() {
        if (walletAdapter != null)
            walletAdapter.notifyDataSetChanged();
    }

    public abstract void update(boolean force);

    public synchronized void addRequestCount() {
        requestCount++;
    }

    public synchronized int getRequestCount() {
        return requestCount;
    }

    public synchronized void resetRequestCount() {
        requestCount = 0;
    }

    void onItemsLoadComplete() {
        if (swipeLayout == null) return;
        swipeLayout.setRefreshing(false);
    }

    public synchronized List<TransactionDisplay> getWallets() {
        return wallets;
    }

    public synchronized void addToWallets(List<TransactionDisplay> w) {
        wallets.addAll(w);
        Collections.sort(getWallets(), new Comparator<TransactionDisplay>() {
            @Override
            public int compare(TransactionDisplay o1, TransactionDisplay o2) {
                return o1.compareTo(o2);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(R.string.trans_menu_title);
        menu.add(0, 100, 0, R.string.trans_menu_changename);//groupId, itemId, order, title
        menu.add(0, 101, 0, R.string.trans_menu_viewreceiver);
        menu.add(0, 102, 0, R.string.trans_menu_openinb);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = -1;
        try {
            position = TransactionAdapter.calculateBoxPosition(walletAdapter.getPosition());
        } catch (Exception e) {
            e.printStackTrace();
            return super.onContextItemSelected(item);
        }
        //Log.d("wubalabadubdub","Wuuu: "+item.getItemId());
        switch (item.getItemId()) {
            case 100: { // Change Address Name
                setName(wallets.get(position).getToAddress());
                break;
            }
            case 101: { // Open in AddressDetailActivity
                Intent i = new Intent(ac, AddressDetailActivity.class);
                i.putExtra("ADDRESS", wallets.get(position).getToAddress());
                startActivity(i);
                break;
            }
            case 102: { // Open in Browser
                String url = "https://etherscan.io/tx/" + wallets.get(position).getTxHash();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;
            }
        }
        return super.onContextItemSelected(item);
    }


    public void setName(final String address) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(ac);
        builder.setTitle(R.string.name_other_address);

        final EditText input = new EditText(ac);
        input.setText(AddressNameConverter.getInstance(ac).get(address));
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
                dialog.cancel();
            }
        });

        builder.show();
    }

    public synchronized void setWallets(List<TransactionDisplay> wal) {
        wallets = wal;
    }

    @Override
    public void onClick(View view) {
        if (ac == null) return;
        int itemPosition = TransactionAdapter.calculateBoxPosition(recyclerView.getChildLayoutPosition(view));
        if (itemPosition >= wallets.size()) return;
        Dialogs.showTXDetails(ac, wallets.get(itemPosition));
    }
}