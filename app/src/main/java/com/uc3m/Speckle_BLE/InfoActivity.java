package com.uc3m.Speckle_BLE;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.wv_info)
    WebView wvInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info_activity);
        ButterKnife.bind(this);
        wvInfo.loadUrl("file:///android_asset/open_source_licenses.html");
    }

}