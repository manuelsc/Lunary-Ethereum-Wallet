package rehanced.com.simpleetherwallet.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.BuildConfig;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.AnalyticsApplication;
import rehanced.com.simpleetherwallet.activities.SendActivity;
import rehanced.com.simpleetherwallet.interfaces.PasswordDialogCallback;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.services.TransactionService;
import rehanced.com.simpleetherwallet.utils.AddressNameConverter;
import rehanced.com.simpleetherwallet.utils.Blockies;
import rehanced.com.simpleetherwallet.utils.CollapseAnimator;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.ResponseParser;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

import static android.app.Activity.RESULT_OK;
import static rehanced.com.simpleetherwallet.R.id.seekBar;

public class FragmentSend extends Fragment {

    private final int DEFAULT_GAS_PRICE = 12;

    private SendActivity ac;
    private Button send;
    private EditText amount;
    private TextView toAddress, toName, usdPrice, gasText, fromName;
    private TextView availableEth, availableFiat, availableFiatSymbol;
    private TextView txCost, txCostFiat, txCostFiatSymbol;
    private TextView totalCost, totalCostFiat, totalCostFiatSymbol;
    private SeekBar gas;
    private ImageView toicon, fromicon;
    private Spinner spinner;
    private Spinner currencySpinner;
    private boolean amountInEther = true;
    private BigInteger gaslimit = new BigInteger("21000");
    private BigDecimal curAvailable = BigDecimal.ZERO;
    private BigDecimal curTxCost = new BigDecimal("0.000252");
    private BigDecimal curAmount = BigDecimal.ZERO;
    private ExchangeCalculator exchange = ExchangeCalculator.getInstance();
    private LinearLayout expertMode;
    private EditText data, userGasLimit;
    private double realGas;

    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_send, container, false);

        ac = (SendActivity) this.getActivity();

        send = (Button) rootView.findViewById(R.id.send);
        amount = (EditText) rootView.findViewById(R.id.amount);
        gas = (SeekBar) rootView.findViewById(seekBar);
        toAddress = (TextView) rootView.findViewById(R.id.toAddress);
        toName = (TextView) rootView.findViewById(R.id.toName);
        fromName = (TextView) rootView.findViewById(R.id.fromName);
        usdPrice = (TextView) rootView.findViewById(R.id.usdPrice);

        availableEth = (TextView) rootView.findViewById(R.id.ethAvailable);
        availableFiat = (TextView) rootView.findViewById(R.id.ethAvailableFiat);
        availableFiatSymbol = (TextView) rootView.findViewById(R.id.ethAvailableFiatSymbol);

        txCost = (TextView) rootView.findViewById(R.id.txCost);
        txCostFiat = (TextView) rootView.findViewById(R.id.txCostFiat);
        txCostFiatSymbol = (TextView) rootView.findViewById(R.id.txCostFiatSymbol);

        totalCost = (TextView) rootView.findViewById(R.id.totalCost);
        totalCostFiat = (TextView) rootView.findViewById(R.id.totalCostFiat);
        totalCostFiatSymbol = (TextView) rootView.findViewById(R.id.totalCostFiatSymbol);

        gasText = (TextView) rootView.findViewById(R.id.gasText);
        toicon = (ImageView) rootView.findViewById(R.id.toicon);
        fromicon = (ImageView) rootView.findViewById(R.id.fromicon);
        expertMode = (LinearLayout) rootView.findViewById(R.id.expertmode);
        data = (EditText) rootView.findViewById(R.id.data);
        userGasLimit = (EditText) rootView.findViewById(R.id.gaslimit);

        ((LinearLayout) rootView.findViewById(R.id.expertmodetrigger)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (expertMode.getVisibility() == View.GONE) {
                    CollapseAnimator.expand(expertMode);
                } else {
                    CollapseAnimator.collapse(expertMode);
                }
            }
        });

        if (getArguments().containsKey("TO_ADDRESS")) {
            setToAddress(getArguments().getString("TO_ADDRESS"), ac);
        }

        if (getArguments().containsKey("AMOUNT")) {
            curAmount = new BigDecimal(getArguments().getString("AMOUNT"));
            amount.setText(getArguments().getString("AMOUNT"));
        }

        gas.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                realGas = (i - 8);
                if (i < 10)
                    realGas = (double) (i + 1) / 10d;

                gasText.setText((realGas + "").replaceAll(".0", ""));
                curTxCost = (new BigDecimal(gaslimit).multiply(new BigDecimal(realGas + ""))).divide(new BigDecimal("1000000000"), 6, BigDecimal.ROUND_DOWN);
                updateDisplays();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        gas.setProgress(DEFAULT_GAS_PRICE);

        spinner = (Spinner) rootView.findViewById(R.id.spinner);
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ac, R.layout.address_spinner, WalletStorage.getInstance(ac).getFullOnly()) {
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
                updateAccountBalance();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAmount(s.toString());
                updateDisplays();
            }
        });

        currencySpinner = (Spinner) rootView.findViewById(R.id.currency_spinner);
        List<String> currencyList = new ArrayList<>();
        currencyList.add("ETH");
        currencyList.add(ExchangeCalculator.getInstance().getMainCurreny().getName());
        ArrayAdapter<String> curAdapter = new ArrayAdapter<>(ac, android.R.layout.simple_spinner_item, currencyList);
        curAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currencySpinner.setAdapter(curAdapter);
        currencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                amountInEther = i == 0;

                updateAmount(amount.getText().toString());
                updateDisplays();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((amount.getText().length() <= 0 || new BigDecimal(amount.getText().toString()).compareTo(new BigDecimal("0")) <= 0) && data.getText().length() <= 0) {
                    ac.snackError(getString(R.string.err_send_noamount));
                    return;
                }
                if (toAddress == null || toAddress.getText().length() == 0) {
                    ac.snackError(getString(R.string.err_send_noreceiver));
                    return;
                }
                if (spinner == null || spinner.getSelectedItem() == null) return;
                try {
                    if (BuildConfig.DEBUG)
                        Log.d("etherbalance", (getCurTotalCost().compareTo(curAvailable) < 0) + " | " + getCurTotalCost() + " | " + curAvailable + " | " + data.getText() + " | " + curAmount);
                    if (getCurTotalCost().compareTo(curAvailable) < 0 || BuildConfig.DEBUG || data.getText().length() > 0) {
                        Dialogs.askForPasswordAndDecode(ac, spinner.getSelectedItem().toString(), new PasswordDialogCallback(){

                            @Override
                            public void success(String password) {
                                sendEther(password, spinner.getSelectedItem().toString());
                            }

                            @Override
                            public void canceled() {}
                        });
                    } else {
                        ac.snackError(getString(R.string.err_send_not_enough_ether));
                    }
                } catch (Exception e) {
                    ac.snackError(getString(R.string.err_send_invalidamount));
                }

            }
        });

        if (getArguments().containsKey("FROM_ADDRESS")) {
            setFromAddress(getArguments().getString("FROM_ADDRESS"));
        }

        updateAccountBalance();
        updateDisplays();

        if (((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).track("Send Fragment");
        }

        return rootView;
    }

    private void updateAccountBalance() {
        try {
            EtherscanAPI.getInstance().getBalance(spinner.getSelectedItem().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ac.snackError("Cant fetch your account balance", Snackbar.LENGTH_LONG);
                        }
                    });

                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    ac.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                curAvailable = new BigDecimal(ResponseParser.parseBalance(response.body().string(), 6));
                                updateDisplays();
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

    private void setFromAddress(String from) {
        ArrayList<String> fullwallets = WalletStorage.getInstance(ac).getFullOnly();
        for (int i = 0; i < fullwallets.size(); i++) {
            if (fullwallets.get(i).equalsIgnoreCase(from)) {
                spinner.setSelection(i);
            }
        }
    }

    private void updateDisplays() {
        updateAvailableDisplay();
        updateAmountDisplay();
        updateTxCostDisplay();
        updateTotalCostDisplay();
    }

    private void updateAvailableDisplay() {
        exchange.setIndex(2);

        availableEth.setText(curAvailable.toString());
        availableFiat.setText(exchange.convertRateExact(curAvailable, exchange.getUSDPrice()));
        availableFiatSymbol.setText(exchange.getCurrent().getShorty());
    }

    private void updateAmount(String str) {
        try {
            final BigDecimal origA = new BigDecimal(str);

            if (amountInEther) {
                curAmount = origA;
            } else {
                curAmount = origA.divide(new BigDecimal(exchange.getUSDPrice()), 7, RoundingMode.FLOOR);
            }
        } catch (NumberFormatException e) {
            curAmount = BigDecimal.ZERO;
        }
    }

    private void updateAmountDisplay() {
        String price;
        if (amountInEther) {
            price = exchange.convertRateExact(curAmount, exchange.getUSDPrice()) +
                    " " + exchange.getMainCurreny().getName();
        } else {
            exchange.setIndex(0);
            price = curAmount.toPlainString() + " " + exchange.getCurrent().getShorty();
        }

        usdPrice.setText(price);
    }

    private void updateTxCostDisplay() {
        exchange.setIndex(2);

        txCost.setText(curTxCost.toString());
        txCostFiat.setText(exchange.convertRateExact(curTxCost, exchange.getUSDPrice()));
        txCostFiatSymbol.setText(exchange.getCurrent().getShorty());
    }

    private BigDecimal getCurTotalCost() {
        return curAmount.add(curTxCost, MathContext.DECIMAL64);
    }

    private void updateTotalCostDisplay() {
        exchange.setIndex(2);

        final BigDecimal curTotalCost = getCurTotalCost();

        totalCost.setText(curTotalCost.toString());
        totalCostFiat.setText(exchange.convertRateExact(curTotalCost, exchange.getUSDPrice()));
        totalCostFiatSymbol.setText(exchange.getCurrent().getShorty());
    }

    private void getEstimatedGasPriceLimit() {
        try {
            EtherscanAPI.getInstance().getGasLimitEstimate(toAddress.getText().toString(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        gaslimit = ResponseParser.parseGasPrice(response.body().string());
                        ac.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                userGasLimit.setText(gaslimit + "");
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendEther(String password, String fromAddress) {
        Intent txService = new Intent(ac, TransactionService.class);
        txService.putExtra("FROM_ADDRESS", fromAddress);
        txService.putExtra("TO_ADDRESS", toAddress.getText().toString());
        txService.putExtra("AMOUNT", curAmount.toPlainString()); // In ether, gets converted by the service itself
        txService.putExtra("GAS_PRICE", (new BigDecimal(realGas + "").multiply(new BigDecimal("1000000000")).toBigInteger()).toString());// "21000000000");
        txService.putExtra("GAS_LIMIT", userGasLimit.getText().length() <= 0 ? gaslimit.toString() : userGasLimit.getText().toString());
        txService.putExtra("PASSWORD", password);
        txService.putExtra("DATA", data.getText().toString());
        ac.startService(txService);

        // For statistics
        if (((AnalyticsApplication) ac.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) ac.getApplication()).event("Send Ether");
        }

        Intent data = new Intent();
        data.putExtra("FROM_ADDRESS", fromAddress);
        data.putExtra("TO_ADDRESS", toAddress.getText().toString());
        data.putExtra("AMOUNT", curAmount.toPlainString());
        ac.setResult(RESULT_OK, data);
        ac.finish();
    }

    public void setToAddress(String to, Context c) {
        if (toAddress == null) return;
        toAddress.setText(to);
        String name = AddressNameConverter.getInstance(c).get(to);
        toName.setText(name == null ? to.substring(0, 10) : name);
        toicon.setImageBitmap(Blockies.createIcon(to.toLowerCase()));
        getEstimatedGasPriceLimit();
    }
}