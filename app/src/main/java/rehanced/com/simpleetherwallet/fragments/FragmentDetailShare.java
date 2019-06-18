package rehanced.com.simpleetherwallet.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.utils.qr.AddressEncoder;
import rehanced.com.simpleetherwallet.utils.qr.Contents;
import rehanced.com.simpleetherwallet.utils.qr.QREncoder;

public class FragmentDetailShare extends Fragment {

    private String ethaddress = "";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail_share, container, false);

        ethaddress = getArguments().getString("ADDRESS");

        Button clipboard = (Button) rootView.findViewById(R.id.copytoclip);
        clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, ethaddress);
                startActivity(Intent.createChooser(i, "Share via"));
            }
        });

        final float scale = getContext().getResources().getDisplayMetrics().density;
        int qrCodeDimention = (int) (310 * scale + 0.5f);

        ImageView qrcode = (ImageView) rootView.findViewById(R.id.qrcode);

        qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", ethaddress);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getActivity(), R.string.wallet_menu_action_copied_to_clipboard, Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        QREncoder qrCodeEncoder = new QREncoder(prefs.getBoolean("qr_encoding_erc", true) ? AddressEncoder.encodeERC(new AddressEncoder(ethaddress)) : ethaddress, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention);
        try {
            Bitmap bitmap = qrCodeEncoder.encodeAsBitmap();
            qrcode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return rootView;
    }

}