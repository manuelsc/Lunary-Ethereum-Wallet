package rehanced.com.simpleetherwallet.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.activities.MainActivity;
import rehanced.com.simpleetherwallet.data.WatchWallet;
import rehanced.com.simpleetherwallet.fragments.FragmentWallets;

public class Dialogs {


    public static void addWatchOnly(final MainActivity c){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_watch_only_title);

        final EditText input = new EditText(c);
        input.setSingleLine();
        FrameLayout container = new FrameLayout(c);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.topMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.bottomMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        params.rightMargin = c.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        input.setLayoutParams(params);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
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
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                InputMethodManager inputMgr = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                if(input.getText().toString().length() == 42 && input.getText().toString().startsWith("0x")) {
                    final boolean suc = WalletStorage.getInstance(c).add(new WatchWallet(input.getText().toString()), c);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (c.fragments != null && c.fragments[1] != null) {
                                        try {
                                            ((FragmentWallets) c.fragments[1]).update();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    c.snackError(c.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er));
                                    if (suc)
                                        AddressNameConverter.getInstance(c).put(input.getText().toString(), "Watch " + input.getText().toString().substring(0, 6), c);
                                }
                            }, 100);
                } else {
                    c.snackError("Invalid Ethereum address!");
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                InputMethodManager inputMgr = (InputMethodManager)input.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMgr.hideSoftInputFromWindow(input.getWindowToken(), 0);
                dialog.cancel();
            }
        });

        builder.show();

    }

    public static void importWallets(final MainActivity c, final ArrayList<File> files){
        String addresses = "";
        for(int i=0; i < files.size() && i < 3; i++)
            addresses += WalletStorage.stripWalletName(files.get(i).getName())+"\n";

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_importing_wallets_title);
        builder.setMessage(String.format(c.getString(R.string.dialog_importing_wallets_text), files.size(), files.size() > 1 ? "s" : "", addresses));
        builder.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    WalletStorage.getInstance(c).importWallets(c, files);
                    c.snackError("Wallet"+(files.size() > 1 ? "s" : "")+" successfully imported!");
                    if(c.fragments != null && c.fragments[1] != null)
                        ((FragmentWallets) c.fragments[1]).update();
                } catch (Exception e) {
                    c.snackError("Error while importing wallets");
                    e.printStackTrace();
                }
                dialog.cancel();
            }
        });
        builder.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void cantExportNonWallet(Context c){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_ex_nofull_title);
        builder.setMessage(R.string.dialog_ex_nofull_text);
        builder.setNeutralButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void exportWallet(Context c, DialogInterface.OnClickListener yes){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_exporting_title);
        builder.setMessage(R.string.dialog_exporting_text);
        builder.setPositiveButton(R.string.button_ok, yes);
        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static void noFullWallet(Context c){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_nofullwallet);
        builder.setMessage(R.string.dialog_nofullwallet_text);
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void noWallet(Context c){
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_no_wallets);
        builder.setMessage(R.string.dialog_no_wallets_text);
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public static void noImportWalletsFound(Context c) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= 24) // Otherwise buttons on 7.0+ are nearly invisible
            builder = new AlertDialog.Builder(c, R.style.AlertDialogTheme);
        else
            builder = new AlertDialog.Builder(c);
        builder.setTitle(R.string.dialog_no_wallets_found);
        builder.setMessage(R.string.dialog_no_wallets_found_text);
        builder.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
