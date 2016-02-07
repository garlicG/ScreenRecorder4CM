package com.garlicg.screenrecord4cm;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;

import com.garlicg.screenrecord4cm.util.ViewFinder;

public class SimpleWebViewActivity extends AppCompatActivity{

    private static final String EXTRA_TITLE = "TITLE";
    private static final String EXTRA_URL = "URL";

    public static Intent newIntent(Context context ,@NonNull String title ,@NonNull String url){
        Intent intent = new Intent(context , SimpleWebViewActivity.class);
        intent.putExtra(EXTRA_TITLE , title);
        intent.putExtra(EXTRA_URL , url);
        return intent;
    }

    @SuppressLint("PrivateResource")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        setContentView(R.layout.activity_webview);

        // Toolbar
        Toolbar toolbar = ViewFinder.byId(this, R.id.toolbar);
        setTitle(intent.getStringExtra(EXTRA_TITLE));
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // WebView
        WebView wv = ViewFinder.byId(this, R.id.webView);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.getSettings().setDisplayZoomControls(false);
        wv.loadUrl(intent.getStringExtra(EXTRA_URL));

    }
}
