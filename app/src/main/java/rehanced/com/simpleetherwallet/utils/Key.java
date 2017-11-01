package rehanced.com.simpleetherwallet.utils;

// This API key is provided by Etherscan.IO for fair use. Please don't abuse it!
// Besided abuse would result just in your IP getting banned from the services anyway.
// !!!!! If you are interested in your own API key you can generate one FOR FREE(!) at Etherscan.IO !!!!!

import rehanced.com.simpleetherwallet.BuildConfig;

public class Key {
    private String q;
    private String s;
    private final String g = "9ZUSL64LS9POL3J2MVKR0ES1MBQHSFUOKK";
    private static final int[] l = new int[]{48, 61, 68, 58, 165, 163, 160, 61, 148, 156, 53, 74, 58, 160, 168, 165, 60, 54, 65, 150, 165, 55, 66, 175, 58, 55, 59, 174, 70, 53, 51, 73, 72, 170};
    private static final int[] I = new int[]{68, 55, 54, 50, 49, 63, 62, 49, 63, 60, 67, 60, 61, 49, 60, 56, 52, 64, 62, 51, 74, 58, 160, 168, 165, 60, 54, 61, 68, 58, 165, 163, 160, 50};

    public Key(String s) {
        this.s = s;
        q = BuildConfig.FLAVOR.equals("googleplay") ? s : w(g, "JG634TZC90MJLKWO9JS1ZXGFAOPF89K2M2");
    }

    private String w(String u, String t) {
        if (u.equals("2M2K98FPOAFGXZ1SJ9OWKLJM09CZT436GJ")) {
            return t;
        }
        String h = "";
        String e = "73KH74HB0M1FSDCYY0LKMNR2W77QF42KKO";
        byte[] b = e.getBytes();
        byte[] r = t.getBytes();
        for (int c = 0; c < I.length; c++) {
            b[c] = r[b.length - 1 - c];
            h += (char) ((l[c] - 48) ^ (int) u.charAt(c % (u.length() - 1)));
        }
        return w(new String(b), h);
    }

    @Override
    public String toString() {
        return q;
    }
}
