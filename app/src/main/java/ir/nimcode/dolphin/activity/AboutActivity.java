package ir.nimcode.dolphin.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;

public class AboutActivity extends FullAppCompatActivity {

    @BindView(R.id.call)
    FloatingActionButton call;
    @BindView(R.id.telegram)
    FloatingActionButton telegram;

    private SweetAlertDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.about_us)));

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(Intent.ACTION_DIAL);
                in.setData(Uri.parse(getString(R.string.support_tel)));
                startActivity(in);
            }
        });

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText(getString(R.string.error))
                .setContentText(getString(R.string.not_telegram_install_error));

        telegram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isAppInstalled = Utilities.isAppAvailable(AboutActivity.this, "org.telegram.messenger");
                if (isAppInstalled) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(getString(R.string.support_telegram_id)));
                    startActivity(intent);
                } else {
                    pDialog.show();
                }
            }
        });

    }
}
