package rehanced.com.simpleetherwallet.utils;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rehanced.com.simpleetherwallet.data.WalletDisplay;

public class AddressNameConverter {

    private HashMap<String, String> mapdb;
    private static AddressNameConverter instance;

    public static AddressNameConverter getInstance(Context context){
        if(instance == null)
            instance = new AddressNameConverter(context);
        return instance;
    }

    private AddressNameConverter(Context context){
        try {
            load(context);
            if(!contains("0xa9981a33f6b1a18da5db58148b2357f22b44e1e0")){
                put("0xa9981a33f6b1a18da5db58148b2357f22b44e1e0", "Lunary Development ✓", context);
            }
        } catch (Exception e) {
            mapdb = new HashMap<String, String>();
            put("0xa9981a33f6b1a18da5db58148b2357f22b44e1e0", "Lunary Development ✓", context);
        }
    }

    public synchronized void put(String addresse, String name, Context context){
        if(name == null || name.length() == 0)
            mapdb.remove(addresse);
        else
            mapdb.put(addresse, name.length() > 22 ? name.substring(0, 22) : name);
        save(context);
    }

    public String get(String addresse){
        return mapdb.get(addresse);
    }

    public boolean contains(String addresse){ return mapdb.containsKey(addresse); }

    public ArrayList<WalletDisplay> getAsAddressbook(){
        ArrayList<WalletDisplay> erg = new ArrayList<WalletDisplay>();
        for(Map.Entry<String, String> entry : mapdb.entrySet()){
            erg.add(new WalletDisplay(entry.getValue().toString(), entry.getKey().toString()));
        }
        Collections.sort(erg);
        return erg;
    }

    public synchronized void save(Context context){
        FileOutputStream fout;
        try {
            fout = new FileOutputStream(new File(context.getFilesDir(), "namedb.dat"));
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(mapdb);
            oos.close();
            fout.close();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load(Context context) throws IOException, ClassNotFoundException{
        FileInputStream fout = new FileInputStream(new File(context.getFilesDir(), "namedb.dat"));
        ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
        mapdb = (HashMap<String, String>) oos.readObject();
        oos.close();
        fout.close();
    }

}
