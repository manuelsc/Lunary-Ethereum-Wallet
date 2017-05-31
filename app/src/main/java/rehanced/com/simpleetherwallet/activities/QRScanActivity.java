package rehanced.com.simpleetherwallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.widget.TextView;

import com.google.android.gms.samples.vision.barcodereader.BarcodeCapture;
import com.google.android.gms.samples.vision.barcodereader.BarcodeGraphic;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import rehanced.com.simpleetherwallet.R;
import xyz.belvi.mobilevisionbarcodescanner.BarcodeRetriever;


public class QRScanActivity extends AppCompatActivity implements BarcodeRetriever {

    public static final int REQUEST_CODE = 100;

    public static final byte SCAN_ONLY = 0;
    public static final byte ADD_TO_WALLETS = 1;
    public static final byte REQUEST_PAYMENT = 2;
    public static final byte PRIVATE_KEY = 3;

    private byte type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView title = (TextView) findViewById(R.id.toolbar_title);
        type = getIntent().getByteExtra("TYPE", SCAN_ONLY);

        title.setText(type == SCAN_ONLY ? "Scan Address" : "ADD WALLET");

        BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        barcodeCapture.setRetrieval(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRetrieved(final Barcode barcode) {
        String address = barcode.displayValue;
        String amount = "";
        if(address.startsWith("iban:")){
            String temp = address.substring(5);
            if(temp.indexOf("?") > 0) {
                if(temp.indexOf("amount=") > 0 && temp.indexOf("amount=") < temp.length()){
                    amount = temp.substring(temp.indexOf("amount=")+7);
                }

                temp = temp.substring(0, temp.indexOf("?"));

            }
            address = temp;
        }

        Intent data = new Intent();
        data.putExtra("ADDRESS", address);
        if(address.length() > 42 && !address.startsWith("0x") && amount.length() <= 0){
            type = PRIVATE_KEY;
        }

        if(amount.length() > 0) {
            data.putExtra("AMOUNT", amount);
            type = REQUEST_PAYMENT;
        }
        data.putExtra("TYPE", type);

        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onRetrievedMultiple(final Barcode closetToClick, final List<BarcodeGraphic> barcodeGraphics) {
    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {
    }

    @Override
    public void onRetrievedFailed(String reason) {
    }
}