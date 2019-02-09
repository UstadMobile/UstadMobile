package com.ustadmobile.port.android.view;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ScormPackagePresenter;
import com.ustadmobile.core.view.ScormPackageView;

public class ScormPackageActivity extends ZippedContentActivity implements ScormPackageView {

    private WebView mWebView;

    private ScormPackagePresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorm_package);

        mWebView = (WebView)findViewById(R.id.activity_scorm_package_webview);
        if(Build.VERSION.SDK_INT >= 17) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient());


        setUMToolbar(R.id.um_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPresenter = new ScormPackagePresenter(this, this);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }


    @Override
    public void onDestroy() {
        mPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }
}
