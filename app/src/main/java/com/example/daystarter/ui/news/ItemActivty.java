package com.example.daystarter.ui.news;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

import com.example.daystarter.R;

public class ItemActivty extends AppCompatActivity {

    WebView webView;
    WebSettings webSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitynew_item);

        Intent intent= getIntent();
        String link= intent.getStringExtra("Link");

        //링크주소 웹뷰에 보여줌
        webView=findViewById(R.id.wv);

        webView.setWebViewClient(new WebViewClient());
        webSettings = webView.getSettings();
        //화면 사이즈 맞춤
        webSettings.setUseWideViewPort(true);
        //javascript 이용할수있도록
        webSettings.setJavaScriptEnabled(true);
        //내부 저장소 이용
        webSettings.setDomStorageEnabled(true);
        webView.loadUrl(link);

       /*
        wv.getSettings().setJavaScriptEnabled(true);
        //웹페이지 열기
        wv.setWebViewClient(new WebViewClient());
        wv.setWebChromeClient(new WebChromeClient());
        wv.setDomStorageEnabled(true); // 내부 저장소 이용할 수 있게 함
        wv.loadUrl(link);
    */
    }
}
