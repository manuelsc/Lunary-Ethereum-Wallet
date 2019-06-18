package rehanced.com.simpleetherwallet.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import rehanced.com.simpleetherwallet.data.TokenDisplay;
import rehanced.com.simpleetherwallet.data.TransactionDisplay;
import rehanced.com.simpleetherwallet.data.WalletDisplay;
import rehanced.com.simpleetherwallet.data.WatchWallet;
import rehanced.com.simpleetherwallet.interfaces.LastIconLoaded;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.network.EtherscanAPI;


public class ResponseParser {


    public static ArrayList<TransactionDisplay> parseTransactions(String response, String walletname, String address, byte type) {
        try {
            ArrayList<TransactionDisplay> erg = new ArrayList<TransactionDisplay>();

            JSONArray data = new JSONObject(response).getJSONArray("result");
            for (int i = 0; i < data.length(); i++) {
                String from = data.getJSONObject(i).getString("from");
                String to = data.getJSONObject(i).getString("to");
                String vorzeichen = "+";
                if (address.equalsIgnoreCase(data.getJSONObject(i).getString("from"))) {
                    vorzeichen = "-";
                } else {
                    String temp = from;
                    from = to;
                    to = temp;
                }
                if (data.getJSONObject(i).getString("value").equals("0") && !Settings.showTransactionsWithZero)
                    continue; // Skip contract calls or empty transactions
                erg.add(new TransactionDisplay(
                        from,
                        to,
                        new BigInteger(vorzeichen + data.getJSONObject(i).getString("value")),
                        data.getJSONObject(i).has("confirmations") ? data.getJSONObject(i).getInt("confirmations") : 13,
                        data.getJSONObject(i).getLong("timeStamp") * 1000,
                        walletname,
                        type,
                        data.getJSONObject(i).getString("hash"),
                        data.getJSONObject(i).has("nonce") ? data.getJSONObject(i).getString("nonce") : "0",
                        data.getJSONObject(i).getLong("blockNumber"),
                        data.getJSONObject(i).getInt("gasUsed"),
                        (data.getJSONObject(i).has("gasPrice") ? data.getJSONObject(i).getLong("gasPrice") : 0),
                        (data.getJSONObject(i).has("isError") && data.getJSONObject(i).getInt("isError") == 1)
                ));
            }


            return erg;
        } catch (JSONException e) {
            return new ArrayList<TransactionDisplay>();
        }
    }

    public static ArrayList<WalletDisplay> parseWallets(String response, ArrayList<StorableWallet> storedwallets, Context context) throws Exception {
        ArrayList<WalletDisplay> display = new ArrayList<WalletDisplay>();
        JSONArray data = new JSONObject(response).getJSONArray("result");
        for (int i = 0; i < storedwallets.size(); i++) {
            BigInteger balance = new BigInteger("0");
            for (int j = 0; j < data.length(); j++) {
                if (data.getJSONObject(j).getString("account").equalsIgnoreCase(storedwallets.get(i).getPubKey())) {
                    balance = new BigInteger(data.getJSONObject(i).getString("balance"));
                    break;
                }
            }
            String walletname = AddressNameConverter.getInstance(context).get(storedwallets.get(i).getPubKey());
            display.add(new WalletDisplay(
                    walletname == null ? "New Wallet" : walletname,
                    storedwallets.get(i).getPubKey(),
                    balance,
                    storedwallets.get(i) instanceof WatchWallet ? WalletDisplay.WATCH_ONLY : WalletDisplay.NORMAL
            ));
        }
        return display;
    }

    public static ArrayList<TokenDisplay> parseTokens(Context c, String response, LastIconLoaded callback) throws Exception {
        Log.d("tokentest", response);
        ArrayList<TokenDisplay> display = new ArrayList<TokenDisplay>();
        JSONArray data = new JSONObject(response).getJSONArray("tokens");
        for (int i = 0; i < data.length(); i++) {
            JSONObject currentToken = data.getJSONObject(i);
            try {
                display.add(new TokenDisplay(
                        currentToken.getJSONObject("tokenInfo").getString("name"),
                        currentToken.getJSONObject("tokenInfo").getString("symbol"),
                        new BigDecimal(currentToken.getString("balance")),
                        currentToken.getJSONObject("tokenInfo").getInt("decimals"),
                        currentToken.getJSONObject("tokenInfo").getJSONObject("price").getDouble("rate"),
                        currentToken.getJSONObject("tokenInfo").getString("address"),
                        currentToken.getJSONObject("tokenInfo").getString("totalSupply"),
                        currentToken.getJSONObject("tokenInfo").getLong("holdersCount"),
                        0
                ));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Download icon and cache it
            EtherscanAPI.getInstance().loadTokenIcon(c, currentToken.getJSONObject("tokenInfo").getString("name"), i == data.length() - 1, callback);

        }
        return display;
    }

    public static String parseBalance(String response) throws JSONException {
        return parseBalance(response, 7);
    }

    public static String parseBalance(String response, int comma) throws JSONException {
        String balance = new JSONObject(response).getString("result");
        if (balance.equals("0")) return "0";
        return new BigDecimal(balance).divide(new BigDecimal(1000000000000000000d), comma, BigDecimal.ROUND_UP).toPlainString();
    }

    public static BigInteger parseGasPrice(String response) throws Exception {
        String gasprice = new JSONObject(response).getString("result");
        return new BigInteger(gasprice.substring(2), 16);
    }

    // Only call for each address, not the combined one
    /*public static void saveNewestNoncesOfAddresses(Context c, ArrayList<TransactionDisplay> tx, String address){
        try{
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            long curNonce = -1;
            for(int i=tx.size()-1; i >= 0; i--){
                if(tx.get(i).getFromAddress().equals(address)) { // From address is always our address (thanks to @parseTransactions above for that)
                    curNonce = Long.parseLong(tx.get(tx.size() - 1).getNounce());
                    break;
                }
            }

            long oldNonce = preferences.getLong("NONCE"+address, 0);
            if(curNonce > oldNonce){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("NONCE"+address, curNonce);
                editor.commit();
            }
        }catch(Exception e){
        }
    }*/

    public static double parsePriceConversionRate(String key, String response) {
        try {
            JSONObject jo = new JSONObject(response).getJSONObject("rates");
            return jo.getDouble(key);
        } catch (Exception e) {
            return 1;
        }
    }

}
