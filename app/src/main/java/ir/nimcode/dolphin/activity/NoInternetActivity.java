package ir.nimcode.dolphin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import es.dmoral.toasty.Toasty;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;

public class NoInternetActivity extends FullAppCompatActivity {


    @BindView(R.id.retry)
    Button retry;
    @BindView(R.id.setting)
    Button setting;

    private long lastBackButtonPressed;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        ButterKnife.bind(this);

        retry.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Utilities.isAvailableNetwork(NoInternetActivity.this)) {
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (Utilities.isAvailableNetwork(NoInternetActivity.this)) {
            setResult(RESULT_OK, getIntent());
            finish();
        } else {
            if (lastBackButtonPressed + 2000 > System.currentTimeMillis()) {
                Utilities.finishApplication(NoInternetActivity.this);
            } else {
                Toasty.info(NoInternetActivity.this, getString(R.string.exit_message), Toast.LENGTH_LONG, true).show();
            }
            lastBackButtonPressed = System.currentTimeMillis();
        }
    }
}
