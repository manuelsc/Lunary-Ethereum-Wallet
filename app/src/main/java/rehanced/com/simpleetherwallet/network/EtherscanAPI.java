package rehanced.com.simpleetherwallet.network;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import rehanced.com.simpleetherwallet.APIKey;
import rehanced.com.simpleetherwallet.interfaces.StorableWallet;
import rehanced.com.simpleetherwallet.utils.Key;


public class EtherscanAPI {

    private final OkHttpClient client;
    private String token;

    private static EtherscanAPI instance;

    public static EtherscanAPI getInstance(){
        if(instance == null)
            instance = new EtherscanAPI();
        return instance;
    }

    public void getPriceChart(long starttime, int period, boolean usd, Callback b) throws IOException {
        get("http://poloniex.com/public?command=returnChartData&currencyPair="+(usd ? "USDT_ETH" : "BTC_ETH")+"&start="+starttime+"&end=9999999999&period="+period, b);
    }

    public void getInternalTransactions(String address, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=account&action=txlistinternal&address="+address+"&startblock=0&endblock=99999999&sort=asc&apikey="+token, b);
    }

    public void getNormalTransactions(String address, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=account&action=txlist&address="+address+"&startblock=0&endblock=99999999&sort=asc&apikey="+token, b);
    }

    public void getEtherPrice(Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=stats&action=ethprice&apikey="+token, b);
    }

    public void getGasPrice(Callback b) throws IOException{
        get("https://api.etherscan.io/api?module=proxy&action=eth_gasPrice&apikey="+token, b);
    }

    public void getTokenBalances(String address, Callback b) throws IOException {
        get("https://api.ethplorer.io/getAddressInfo/"+address+"?apiKey=freekey", b);
    }

    public void getGasLimitEstimate(String to, Callback b) throws IOException{
        get("https://api.etherscan.io/api?module=proxy&action=eth_estimateGas&to="+to+"&value=0xff22&gasPrice=0x051da038cc&gas=0xffffff&apikey="+token, b);
    }

    public void getBalance(String address, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=account&action=balance&address="+address+"&apikey="+token, b);
    }

    public void getNonceForAddress(String address, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_getTransactionCount&address="+address+"&tag=latest&apikey="+token, b);
    }

    public void getPriceConversionRates(String currencyConversion, Callback b) throws IOException {
        get("http://download.finance.yahoo.com/d/quotes.csv?s="+currencyConversion+"=X&f=snl1", b);
    }

    public void getBalances(ArrayList<StorableWallet> addresses, Callback b) throws IOException {
        String url = "https://api.etherscan.io/api?module=account&action=balancemulti&address=";
        for(StorableWallet address : addresses)
            url += address.getPubKey() + ",";
        url = url.substring(0, url.length()-1) + "&tag=latest&apikey="+token; // remove last , AND add token
        get(url, b);
    }

    public void forwardTransaction(String raw, Callback b) throws IOException {
        get("http://api.etherscan.io/api?module=proxy&action=eth_sendRawTransaction&hex="+raw+"&apikey="+token, b);
    }

    public void get(String url, Callback b) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(b);
    }

    private EtherscanAPI(){
        client = new OkHttpClient();
        client.setConnectTimeout(20, TimeUnit.SECONDS); // connect timeout
        client.setReadTimeout(20, TimeUnit.SECONDS);
        token = new Key(APIKey.API_KEY).toString();
    }
}
