package rehanced.com.simpleetherwallet.fragments;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import rehanced.com.simpleetherwallet.R;

public class ToSFragment extends Fragment {

    private TextView tos;
    private CheckBox read;
    private boolean checked = false;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        tos = (TextView) getView().findViewById(R.id.tostext);
        tos.setText(Html.fromHtml(loadTerms()));
        read = (CheckBox) getView().findViewById(R.id.checkBox);

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checked = read.isChecked();
            }
        });
    }

    private String loadTerms() {
        StringBuilder termsString = new StringBuilder();
        BufferedReader reader;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getActivity().getAssets().open("gpl.txt")));

            String str;
            while ((str = reader.readLine()) != null) {
                termsString.append(str);
            }

            reader.close();
            return termsString.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isToSChecked() {
        return checked;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tos_layout, container, false);
    }
}