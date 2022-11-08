package com.example.zolp;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class ItemDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        WebView mWebView;
        mWebView = findViewById(R.id.detailWebview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        String id = getIntent().getStringExtra("id");
        String url = "https://pcmap.place.naver.com/restaurant/" + id + "/home";
        mWebView.loadUrl(url);
    }
}
