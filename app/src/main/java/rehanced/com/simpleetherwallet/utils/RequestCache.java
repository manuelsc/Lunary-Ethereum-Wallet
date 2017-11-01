package rehanced.com.simpleetherwallet.utils;

import java.util.HashMap;

/**
 * Used for temporary caching of responses. Clears once android garbage collects
 */
public class RequestCache {

    public static final String TYPE_TOKEN = "TOKEN_";
    public static final String TYPE_TXS_NORMAL = "TXS_NORMAL_";
    public static final String TYPE_TXS_INTERNAL = "TXS_INTERNAL_";
    public static final String TYPE_BALANCES = "BALANCES_";

    private HashMap<String, String> map = new HashMap<String, String>();
    private static RequestCache instance;

    public static RequestCache getInstance() {
        if (instance == null)
            instance = new RequestCache();
        return instance;
    }

    public void put(String type, String address, String response) {
        map.put(type + address, response);
    }

    public String get(String type, String address) {
        return map.get(type + address);
    }

    public boolean contains(String type, String address) {
        return map.containsKey(type + address);
    }

}
