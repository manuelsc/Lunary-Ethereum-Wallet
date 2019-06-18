package rehanced.com.simpleetherwallet.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.data.WalletDisplay;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.WalletAdapter;
import rehanced.com.simpleetherwallet.utils.WalletStorage;
import rehanced.com.simpleetherwallet.utils.qr.AddressEncoder;
import rehanced.com.simpleetherwallet.utils.qr.Contents;
import rehanced.com.simpleetherwallet.utils.qr.QREncoder;

import static rehanced.com.simpleetherwallet.R.id.qrcode;

public class RequestEtherActivity extends SecureAppCompatActivity implements View.OnClickListener {

    private CoordinatorLayout coord;
    private ImageView qr;
    private RecyclerView recyclerView;
    private WalletAdapter walletAdapter;
    private List<WalletDisplay> wallets = new ArrayList<>();
    private String selectedEtherAddress;
    private TextView amount, usdPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requestether);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coord = (CoordinatorLayout) findViewById(R.id.main_content);
        qr = (ImageView) findViewById(qrcode);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        amount = (TextView) findViewById(R.id.amount);
        usdPrice = (TextView) findViewById(R.id.usdPrice);
        walletAdapter = new WalletAdapter(wallets, this, this, this);
        LinearLayoutManager mgr = new LinearLayoutManager(this.getApplicationContext());
        RecyclerView.LayoutManager mLayoutManager = mgr;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(walletAdapter);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                mgr.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);

        amount.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    try {
                        double amountd = Double.parseDouble(amount.getText().toString());
                        usdPrice.setText(ExchangeCalculator.getInstance().displayUsdNicely(ExchangeCalculator.getInstance().convertToUsd(amountd)) + " " + ExchangeCalculator.getInstance().getMainCurreny().getName());
                        updateQR();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        update();
        updateQR();
        if (((AnalyticsApplication) this.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) this.getApplication()).track("Request Activity");
        }
    }


    public void update() {
        wallets.clear();
        ArrayList<WalletDisplay> myAddresses = new ArrayList<WalletDisplay>();
        ArrayList<StorableWallet> storedAddresses = new ArrayList<StorableWallet>(WalletStorage.getInstance(this).get());
        for (int i = 0; i < storedAddresses.size(); i++) {
            if (i == 0) selectedEtherAddress = storedAddresses.get(i).getPubKey();
            myAddresses.add(new WalletDisplay(
                    AddressNameConverter.getInstance(this).get(storedAddresses.get(i).getPubKey()),
                    storedAddresses.get(i).getPubKey()
            ));
        }

        wallets.addAll(myAddresses);
        walletAdapter.notifyDataSetChanged();
    }

    public void snackError(String s) {
        if (coord == null) return;
        Snackbar mySnackbar = Snackbar.make(coord, s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    public void updateQR() {
        int qrCodeDimention = 400;
        String iban = "iban:" + selectedEtherAddress;
        if (amount.getText().toString().length() > 0 && new BigDecimal(amount.getText().toString()).compareTo(new BigDecimal("0")) > 0) {
            iban += "?amount=" + amount.getText().toString();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        QREncoder qrCodeEncoder;
        if (prefs.getBoolean("qr_encoding_erc", true)) {
            AddressEncoder temp = new AddressEncoder(selectedEtherAddress);
            if (amount.getText().toString().length() > 0 && new BigDecimal(amount.getText().toString()).compareTo(new BigDecimal("0")) > 0)
                temp.setAmount(amount.getText().toString());
            qrCodeEncoder = new QREncoder(AddressEncoder.encodeERC(temp), null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        } else {
            qrCodeEncoder = new QREncoder(iban, null,
                    Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        }

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onClick(View view) {
        int itemPosition = recyclerView.getChildLayoutPosition(view);
        selectedEtherAddress = wallets.get(itemPosition).getPublicKey();
        updateQR();
    }
}

