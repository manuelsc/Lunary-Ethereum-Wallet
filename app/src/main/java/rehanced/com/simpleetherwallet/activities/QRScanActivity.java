package rehanced.com.simpleetherwallet.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.utils.qr.AddressEncoder;


public class QRScanActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    public static final int REQUEST_CODE = 100;
    public static final int REQUEST_CAMERA_PERMISSION = 106;

    public static final byte SCAN_ONLY = 0;
    public static final byte ADD_TO_WALLETS = 1;
    public static final byte REQUEST_PAYMENT = 2;
    public static final byte PRIVATE_KEY = 3;

    private byte type;

    private ZXingScannerView mScannerView;
    private FrameLayout barCode;

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

        barCode = (FrameLayout) findViewById(R.id.barcode);
        // BarcodeCapture barcodeCapture = (BarcodeCapture) getSupportFragmentManager().findFragmentById(R.id.barcode);
        // barcodeCapture.setRetrieval(this);

        if (hasPermission(this))
            initQRScan(barCode);
        else
            askForPermissionRead(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void initQRScan(FrameLayout frame) {
        mScannerView = new ZXingScannerView(this);
        frame.addView(mScannerView);
        mScannerView.setResultHandler(this);
        ArrayList<BarcodeFormat> supported = new ArrayList<BarcodeFormat>();
        supported.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(supported);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mScannerView != null)
            mScannerView.stopCamera();
    }

    public boolean hasPermission(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    public static void askForPermissionRead(Activity c) {
        if (Build.VERSION.SDK_INT < 23) return;
        ActivityCompat.requestPermissions(c, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initQRScan(barCode);
                } else {
                    Toast.makeText(this, "Please grant camera permission in order to read QR codes", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void handleResult(Result result) {
        if (result == null) return;
        String address = result.getText();
        try {
            AddressEncoder scanned = AddressEncoder.decode(address);
            Intent data = new Intent();
            data.putExtra("ADDRESS", scanned.getAddress().toLowerCase());

            if (scanned.getAddress().length() > 42 && !scanned.getAddress().startsWith("0x") && scanned.getAmount() == null)
                type = PRIVATE_KEY;

            if (scanned.getAmount() != null) {
                data.putExtra("AMOUNT", scanned.getAmount());
                type = REQUEST_PAYMENT;
            }

            data.putExtra("TYPE", type);
            setResult(RESULT_OK, data);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}