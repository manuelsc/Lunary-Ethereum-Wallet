package rehanced.com.simpleetherwallet.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import rehanced.com.simpleetherwallet.BuildConfig;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.activities.SendActivity;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.services.TransactionService;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.Blockies;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

import static android.app.Activity.RESULT_OK;

public class FragmentSend extends Fragment {


    private SendActivity ac;
    private Button send;
    private EditText amount;
    private TextView toAddress, toName, usdPrice, ethAvailable, gasText, txCost, fromName, totalCost;
    private SeekBar gas;
    private ImageView toicon, fromicon;
    private Spinner spinner;
    private BigInteger gaslimit = new BigInteger("21000");

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send, container, false);

        ac = (SendActivity) this.getActivity();

        send = (Button) rootView.findViewById(R.id.send);
        amount = (EditText) rootView.findViewById(R.id.amount);
        gas = (SeekBar) rootView.findViewById(R.id.seekBar);
        toAddress = (TextView) rootView.findViewById(R.id.toAddress);
        toName = (TextView) rootView.findViewById(R.id.toName);
        fromName = (TextView) rootView.findViewById(R.id.fromName);
        usdPrice = (TextView) rootView.findViewById(R.id.usdPrice);
        ethAvailable = (TextView) rootView.findViewById(R.id.ethAvailable);
        totalCost = (TextView) rootView.findViewById(R.id.totalCost);
        gasText = (TextView) rootView.findViewById(R.id.gasText);
        toicon = (ImageView) rootView.findViewById(R.id.toicon);
        fromicon = (ImageView) rootView.findViewById(R.id.fromicon);

        txCost = (TextView) rootView.findViewById(R.id.txCost);

        if(getArguments().containsKey("TO_ADDRESS")){
            setToAddress(getArguments().getString("TO_ADDRESS"), ac);
        }
        if(getArguments().containsKey("AMOUNT")){
            amount.setText(getArguments().getString("AMOUNT"));
            if(amount.getText().toString().length() != 0) {
                try {
                    double amountd = Double.parseDouble(amount.getText().toString());
                    usdPrice.setText(ExchangeCalculator.getInstance().displayUsdNicely(ExchangeCalculator.getInstance().convertToUsd(amountd))+" "+ExchangeCalculator.getInstance().getMainCurreny().getName());

                    totalCost.setText(new BigDecimal(txCost.getText().toString()).add(new BigDecimal(amount.getText().toString())).toPlainString());
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        gas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                gasText.setText(i+1+"");
                BigDecimal gasPrice = (new BigDecimal("21000").multiply(new BigDecimal((i+1)+""))).divide(new BigDecimal("1000000000"), 6, BigDecimal.ROUND_DOWN);
                String cost =  gasPrice.toPlainString();

                txCost.setText(cost);
                if(amount.getText().length() > 0)
                    totalCost.setText(gasPrice.add(new BigDecimal(amount.getText().toString())).toPlainString());
                else
                    totalCost.setText(cost);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ac, android.R.layout.simple_spinner_item, WalletStorage.getInstance(ac).getFullOnly()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                view.setPadding(0, view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom());
                return view;
            }
        };

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                try {
                    EtherscanAPI.getInstance().getBalance(spinner.getSelectedItem().toString(), new Callback() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            ac.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ac.snackError("Cant fetch your account balance", Snackbar.LENGTH_LONG);
                                }
                            });

                        }

                        @Override
                        public void onResponse(final Response response) throws IOException {
                            ac.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        ethAvailable.setText(ResponseParser.parseBalance(response.body().string(), 6));
                                    } catch (Exception e) {
                                        ac.snackError("Cant fetch your account balance");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fromicon.setImageBitmap(Blockies.createIcon(spinner.getSelectedItem().toString().toLowerCase()));
                fromName.setText(AddressNameConverter.getInstance(ac).get(spinner.getSelectedItem().toString().toLowerCase()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0) {
                    try {
                        double amountd = Double.parseDouble(amount.getText().toString());
                        usdPrice.setText(ExchangeCalculator.getInstance().convertToUsd(amountd)+" "+ExchangeCalculator.getInstance().getMainCurreny().getName());
                        totalCost.setText(new BigDecimal(txCost.getText().toString()).add(new BigDecimal(amount.getText().toString())).toPlainString());
                    } catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(amount.getText().length() <= 0 || new BigDecimal(amount.getText().toString()).compareTo(new BigDecimal("0")) <= 0) {
                    ac.snackError("No amount specified");
                    return;
                }
                if(toAddress == null || toAddress.getText().length() == 0){
                    ac.snackError("No receiver specified");
                    return;
                }
                if(spinner == null || spinner.getSelectedItem() == null) return;
                try{
                    BigDecimal enteredAmount = new BigDecimal(totalCost.getText().toString());
                    BigDecimal available = new BigDecimal(ethAvailable.getText().toString());

                    if(enteredAmount.compareTo(available) < 0 || BuildConfig.DEBUG){
                        askForPasswordAndDecode(spinner.getSelectedItem().toString());
                    } else {
                        ac.snackError("Not enough Ether on that Address");
                    }
                } catch(Exception e){
                    ac.snackError("Invalid Amount");
                }

            }
        });

        spinner.setSelection(0);

        if(((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).track("Send Fragment");
        }

        return rootView;
    }

    private void getEstimatedGasPriceLimit(){
        try {
            EtherscanAPI.getInstance().getGasLimitEstimate(toAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {}

                @Override
                public void onResponse(Response response) throws IOException {
                    try {
                        gaslimit = ResponseParser.parseGasPrice(response.body().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void askForPasswordAndDecode(final String fromAddress){
        AlertDialog.Builder builder = new AlertDialog.Builder(ac,  R.style.AlertDialogTheme);
        builder.setTitle("Wallet Password");

        final EditText input = new EditText(ac);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());

        FrameLayout container = new FrameLayout(ac);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);

        container.addView(input);
        builder.setView(container);

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
                sendEther(input.getText().toString(), fromAddress);
                dialog.dismiss();
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

    private void sendEther(String password, String fromAddress) {
        Intent txService = new Intent(ac, TransactionService.class);
        txService.putExtra("FROM_ADDRESS", fromAddress);
        txService.putExtra("TO_ADDRESS", toAddress.getText().toString());
        txService.putExtra("AMOUNT", amount.getText().toString()); // In ether, gets converted by the service itself
        txService.putExtra("GAS_PRICE", new BigDecimal((gas.getProgress()+1)+"").multiply(new BigDecimal("1000000000")).toPlainString());// "21000000000");
        txService.putExtra("GAS_LIMIT", gaslimit.toString());
        txService.putExtra("PASSWORD", password);
        ac.startService(txService);

        // For statistics
        if(((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).event("Send Ether");
        }

        Intent data = new Intent();
        data.putExtra("FROM_ADDRESS", fromAddress);
        data.putExtra("TO_ADDRESS", toAddress.getText().toString());
        data.putExtra("AMOUNT", amount.getText().toString());
        ac.setResult(RESULT_OK, data);
        ac.finish();
    }


    public void setToAddress(String to, Context c){
        if(toAddress == null) return;
        toAddress.setText(to);
        String name = AddressNameConverter.getInstance(c).get(to);
        toName.setText(name == null ? to.substring(0, 10) : name);
        toicon.setImageBitmap(Blockies.createIcon(to.toLowerCase()));
        getEstimatedGasPriceLimit();
    }




}