package ir.nimcode.dolphin.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

import ir.nimcode.dolphin.R;

public class ShowErrorActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_error);

        String error = getIntent().getStringExtra("errorBody");
        WebView wv = (WebView) findViewById(R.id.web_view);

        WebSettings settings = wv.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        wv.loadData(error, "text/html; charset=utf-8", "utf-8");
    }
}
