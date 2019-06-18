package rehanced.com.simpleetherwallet.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import rehanced.com.simpleetherwallet.R;
import rehanced.com.simpleetherwallet.utils.Dialogs;
import rehanced.com.simpleetherwallet.utils.Settings;

public class WalletGenActivity extends SecureAppCompatActivity {

    public static final int REQUEST_CODE = 401;

    private EditText password;
    private EditText passwordConfirm;
    private CoordinatorLayout coord;
    private TextView walletGenText, toolbar_title;
    private String privateKeyProvided;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_gen);

        password = (EditText) findViewById(R.id.password);
        passwordConfirm = (EditText) findViewById(R.id.passwordConfirm);
        walletGenText = (TextView) findViewById(R.id.walletGenText);
        toolbar_title = (TextView) findViewById(R.id.toolbar_title);


        coord = (CoordinatorLayout) findViewById(R.id.main_content);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                genCheck();
            }
        });

        if (getIntent().hasExtra("PRIVATE_KEY")) {
            privateKeyProvided = getIntent().getStringExtra("PRIVATE_KEY");
            walletGenText.setText(getResources().getText(R.string.import_text));
            toolbar_title.setText(R.string.import_title);
            mEmailSignInButton.setText(R.string.import_button);
        }

        if (((AnalyticsApplication) this.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) this.getApplication()).track("Walletgen Activity");
        }
    }

    private void genCheck() {
        if (!passwordConfirm.getText().toString().equals(password.getText().toString())) {
            snackError(getResources().getString(R.string.error_incorrect_password));
            return;
        }
        if (!isPasswordValid(passwordConfirm.getText().toString())) {
            snackError(getResources().getString(R.string.error_invalid_password));
            return;
        }
        Dialogs.writeDownPassword(this);
    }

    public void gen() {
        Settings.walletBeingGenerated = true; // Lock so a user can only generate one wallet at a time

        // For statistics only
        if (((AnalyticsApplication) this.getApplication()).isGooglePlayBuild()) {
            ((AnalyticsApplication) this.getApplication()).event("Wallet generated");
        }

        Intent data = new Intent();
        data.putExtra("PASSWORD", passwordConfirm.getText().toString());
        if (privateKeyProvided != null)
            data.putExtra("PRIVATE_KEY", privateKeyProvided);
        setResult(RESULT_OK, data);
        finish();
    }


    public void snackError(String s) {
        if (coord == null) return;
        Snackbar mySnackbar = Snackbar.make(coord, s, Snackbar.LENGTH_SHORT);
        mySnackbar.show();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 9;
    }


}

