package rehanced.com.simpleetherwallet.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.List;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.activities.QRScanActivity;
import rehanced.com.simpleetherwallet.activities.SendActivity;
import rehanced.com.simpleetherwallet.data.WalletDisplay;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.WalletAdapter;

public class FragmentChooseRecipient extends Fragment implements View.OnClickListener{

    private RecyclerView recyclerView;
    private WalletAdapter walletAdapter;
    private List<WalletDisplay> wallets = new ArrayList<>();
    private SendActivity ac;
    private ImageButton qr;
    private Button send;
    private EditText addressBox;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipient, container, false);

        ac = (SendActivity) this.getActivity();

        qr = (ImageButton) rootView.findViewById(R.id.scan_button);
        send = (Button) rootView.findViewById(R.id.send);
        addressBox = (EditText) rootView.findViewById(R.id.receiver);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        walletAdapter = new WalletAdapter(wallets, ac, this, this);
        LinearLayoutManager mgr  = new LinearLayoutManager(ac.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);


        qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent qr = new Intent(ac, QRScanActivity.class);
                qr.putExtra("TYPE", QRScanActivity.SCAN_ONLY);
                ac.startActivityForResult(qr, QRScanActivity.REQUEST_CODE);
            }
        });


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addressBox.getText().toString().length() > 15 && addressBox.getText().toString().startsWith("0x"))
                    ac.nextStage(addressBox.getText().toString());
                else
                    ac.snackError("Invalid Recipient");
            }
        });

        update();

        Tracker t = ((AnalyticsApplication) ac.getApplication()).getDefaultTracker();
        t.setScreenName("Recipient Fragment");
        t.send(new HitBuilders.ScreenViewBuilder().build());

        return rootView;
    }

    public void setRecipientAddress(String address){
        if(addressBox == null) return;
        addressBox.setText(address);
    }

    public void update(){
        if(ac == null) return;
        wallets.clear();

        wallets.addAll(new ArrayList<WalletDisplay>(AddressNameConverter.getInstance(ac).getAsAddressbook()));
        walletAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        addressBox.setText(wallets.get(itemPosition).getPublicKey());
    }
}