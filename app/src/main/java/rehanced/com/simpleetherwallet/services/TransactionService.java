package rehanced.com.simpleetherwallet.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.request.RawTransaction;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;
import rehanced.com.simpleetherwallet.utils.ExchangeCalculator;
import rehanced.com.simpleetherwallet.utils.WalletStorage;

public class TransactionService extends IntentService {

    private NotificationCompat.Builder builder;
    final int mNotificationId = 153;

    public TransactionService() {
        super("Transaction Service");
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sendNotification();
        try {
            String fromAddress = intent.getStringExtra("FROM_ADDRESS");
            final String toAddress = intent.getStringExtra("TO_ADDRESS");
            final String amount = intent.getStringExtra("AMOUNT");
            final String gas_price = intent.getStringExtra("GAS_PRICE");
            final String gas_limit = intent.getStringExtra("GAS_LIMIT");
            final String data = intent.getStringExtra("DATA");
            String password = intent.getStringExtra("PASSWORD");

            final Credentials keys = WalletStorage.getInstance(getApplicationContext()).getFullWallet(getApplicationContext(), password, fromAddress);

            EtherscanAPI.getInstance().getNonceForAddress(fromAddress, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    error("Can't connect to network, retry it later");
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    try {
                        JSONObject o = new JSONObject(response.body().string());
                        BigInteger nonce = new BigInteger(o.getString("result").substring(2), 16);

                        RawTransaction tx = RawTransaction.createTransaction(
                                nonce,
                                new BigInteger(gas_price),
                                new BigInteger(gas_limit),
                                toAddress,
                                new BigDecimal(amount).multiply(ExchangeCalculator.ONE_ETHER).toBigInteger(),
                                data
                        );

                        Log.d("txx",
                                "Nonce: " + tx.getNonce() + "\n" +
                                        "gasPrice: " + tx.getGasPrice() + "\n" +
                                        "gasLimit: " + tx.getGasLimit() + "\n" +
                                        "To: " + tx.getTo() + "\n" +
                                        "Amount: " + tx.getValue() + "\n" +
                                        "Data: " + tx.getData()
                        );

                        byte[] signed = TransactionEncoder.signMessage(tx, keys);

                        Log.d("txx", "TX: "+bytesToHex(signed));

                        forwardTX(signed);
                    } catch (Exception e) {
                        e.printStackTrace();
                        error("Can't connect to network, retry it later");
                    }
                }
            });

        } catch (Exception e) {
            error("Invalid Wallet Password!");
            e.printStackTrace();
        }
    }

    private void forwardTX(byte[] signed) throws IOException {
        EtherscanAPI.getInstance().forwardTransaction("0x" + Hex.toHexString(signed), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                error("Can't connect to network, retry it later");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String received = response.body().string();
                try {
                    suc(new JSONObject(received).getString("result"));
                } catch (Exception e) {
                    // Advanced error handling. If etherscan returns error message show the shortened version in notification. Else abbort with unknown error
                    try {
                        String errormsg = new JSONObject(received).getJSONObject("error").getString("message");
                        if (errormsg.indexOf(".") > 0)
                            errormsg = errormsg.substring(0, errormsg.indexOf("."));
                        error(errormsg); // f.E Insufficient funds
                    } catch (JSONException e1) {
                        error("Unknown error occured");
                    }
                }
            }
        });
    }

    private void suc(String hash) {
        builder
                .setContentTitle(getString(R.string.notification_transfersuc))
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText("");

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("STARTAT", 2);
        main.putExtra("TXHASH", hash);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void error(String err) {
        builder
                .setContentTitle(getString(R.string.notification_transferfail))
                .setProgress(100, 100, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentText(err);

        Intent main = new Intent(this, MainActivity.class);
        main.putExtra("STARTAT", 2);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                main, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }

    private void sendNotification() {
        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(0x2d435c)
                .setTicker(getString(R.string.notification_transferingticker))
                .setContentTitle(getString(R.string.notification_transfering_title))
                .setContentText(getString(R.string.notification_might_take_a_minute))
                .setOngoing(true)
                .setProgress(0, 0, true);
        final NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(mNotificationId, builder.build());
    }


}
