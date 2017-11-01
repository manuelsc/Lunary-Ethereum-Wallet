package rehanced.com.simpleetherwallet.utils;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.HashMap;

public class TokenIconCache {

    private HashMap<String, byte[]> cache;
    private static TokenIconCache instance;

    public static TokenIconCache getInstance(Context c) {
        if (instance == null)
            instance = new TokenIconCache(c);
        return instance;
    }

    private TokenIconCache(Context c) {
        try {
            load(c);

        } catch (Exception e) {
            cache = new HashMap<String, byte[]>();
        }
        Log.d("iconmap", cache.toString());
    }

    public Bitmap get(String s) {
        if (cache.get(s) == null) return null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateInSampleSize(options, 20, 31);
        return BitmapFactory.decodeByteArray(cache.get(s), 0, cache.get(s).length, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public boolean put(Context c, String s, Bitmap b) {
        if (b == null || cache.containsKey(s)) return false;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        cache.put(s, byteArray);
        try {
            save(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean contains(String s) {
        return cache.containsKey(s);
    }

    public void save(Context activity) throws Exception {
        ObjectOutputStream outputStream = null;
        OutputStream fos = null;
        try {
            fos = new BufferedOutputStream(new FileOutputStream(new File(activity.getFilesDir(), "tokeniconcache.dat")));
            outputStream = new ObjectOutputStream(fos);
            outputStream.writeObject(cache);
        } finally {
            if (outputStream != null)
                outputStream.close();
            if (fos != null)
                fos.close();
        }
    }

    @SuppressWarnings("unchecked")
    public void load(Context activity) throws Exception {
        ObjectInputStream inputStream = null;
        try {
            inputStream = new ObjectInputStream(
                    new BufferedInputStream(new FileInputStream(new File(activity.getFilesDir(), "tokeniconcache.dat"))));
            cache = (HashMap<String, byte[]>) inputStream.readObject();
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

}

