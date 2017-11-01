package rehanced.com.simpleetherwallet.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

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
        tos.setText(Html.fromHtml(getActivity().getResources().getString(R.string.tos)));
        read = (CheckBox) getView().findViewById(R.id.checkBox);

        read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checked = read.isChecked();
            }
        });
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