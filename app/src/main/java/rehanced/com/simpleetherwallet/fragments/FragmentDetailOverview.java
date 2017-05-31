package rehanced.com.simpleetherwallet.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.math.BigDecimal;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AddressDetailActivity;
import rehanced.com.simpleetherwallet.activities.SendActivity;
import rehanced.com.simpleetherwallet.data.CurrencyEntry;
import rehanced.com.simpleetherwallet.data.WatchWallet;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.Blockies;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.WalletStorage;
import rehanced.com.simpleetherwallet.utils.qr.Contents;
import rehanced.com.simpleetherwallet.utils.qr.QREncoder;

public class FragmentDetailOverview extends Fragment {

    private AddressDetailActivity ac;
    private String ethaddress = "";
    private byte type;
    private TextView balance, address, currency;
    private ImageView icon, qrcode;
    private LinearLayout header;
    BigDecimal balanceDouble = new BigDecimal("1");
    private FloatingActionMenu fabmenu;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_ov, container, false);

        ac = (AddressDetailActivity) this.getActivity();
        ethaddress = getArguments().getString("ADDRESS");
        type = getArguments().getByte("TYPE");

        ImageView icon = (ImageView) rootView.findViewById(R.id.addressimage);
        TextView address = (TextView) rootView.findViewById(R.id.ethaddress);
        final TextView balance = (TextView) rootView.findViewById(R.id.balance);
        currency = (TextView) rootView.findViewById(R.id.currency) ;
        header = (LinearLayout) rootView.findViewById(R.id.header);
        fabmenu =(FloatingActionMenu) rootView.findViewById(R.id.fabmenu);

        Button clipboard = (Button) rootView.findViewById(R.id.copytoclip);
        clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, ethaddress);
                startActivity(Intent.createChooser(i,"Share via"));
            }
        });

        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CurrencyEntry cur = ExchangeCalculator.getInstance().next();
                balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate())+"");
                currency.setText(cur.getName());
                if(ac != null)
                    ac.broadCastDataSetChanged();
            }
        });

        icon.setImageBitmap(Blockies.createIcon(ethaddress, 24));
        address.setText(ethaddress);

        int qrCodeDimention = 600;

        qrcode = (ImageView) rootView.findViewById(R.id.qrcode);

        QREncoder qrCodeEncoder = new QREncoder(ethaddress, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);

        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!fabmenu.isMenuHidden())
                    fabmenu.hideMenu(true);
                else
                    fabmenu.showMenu(true);
            }
        });

        FloatingActionButton fab_setName = (FloatingActionButton) rootView.findViewById(R.id.set_name);
        fab_setName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setName();
            }
        });

        FloatingActionButton send_ether = (FloatingActionButton) rootView.findViewById(R.id.send_ether);
        send_ether.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(WalletStorage.getInstance(ac).getFullOnly().size() == 0){
                    Dialogs.noFullWallet(ac);
                } else {
                    Intent tx = new Intent(ac, SendActivity.class);
                    tx.putExtra("TO_ADDRESS", ethaddress);
                    ac.startActivityForResult(tx, SendActivity.REQUEST_CODE);
                }
            }
        });

        FloatingActionButton fab_add = (FloatingActionButton) rootView.findViewById(R.id.add_as_watch);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean suc = WalletStorage.getInstance(ac).add(new WatchWallet( ethaddress), ac);
                new Handler().postDelayed(
                        new Runnable() {
                            @Override public void run() {
                                ac.snackError(ac.getResources().getString(suc ? R.string.main_ac_wallet_added_suc :  R.string.main_ac_wallet_added_er));
                            }
                        }, 100);
            }
        });

        if(type == AddressDetailActivity.OWN_WALLET){
            fab_add.setVisibility(View.GONE);
        }

        try {
            EtherscanAPI.getInstance().getBalance(ethaddress, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    ac.snackError("Can't connect to network");
                }

                @Override
                public void onResponse(final Response response) throws IOException {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                balanceDouble = new BigDecimal(ResponseParser.parseBalance(response.body().string()));
                               // balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, ExchangeCalculator.getInstance().get));
                                CurrencyEntry cur = ExchangeCalculator.getInstance().getCurrent();
                                balance.setText(ExchangeCalculator.getInstance().convertRateExact(balanceDouble, cur.getRate())+"");
                                currency.setText(cur.getName());
                            } catch (Exception e) {
                                ac.snackError("Invalid server response");
                            }
                        }
                    });

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rootView;
    }


    public void setName(){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(ac, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(ac);
        if(type == AddressDetailActivity.OWN_WALLET)
            builder.setTitle("Name Your Address");
        else
            builder.setTitle("Name This Address");

        final EditText input = new EditText(ac);
        input.setText(AddressNameConverter.getInstance(ac).get(ethaddress));
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(ac);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);
        input.setSelection(input.getText().length());

        container.addView(input);
        builder.setView(container);
        input.setOnFocusChangeListener(new View.OnFocusChangeListener()  {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus) {
                    InputMethodManager inputMgr = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
                }
            }
        });
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                AddressNameConverter.getInstance(ac).put(ethaddress, input.getText().toString(), ac);
                ac.setTitle(input.getText().toString());

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        builder.show();
    }

}