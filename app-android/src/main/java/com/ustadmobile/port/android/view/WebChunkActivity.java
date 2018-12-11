package com.ustadmobile.port.android.view;

import android.os.Bundle;
import android.webkit.WebView;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.WebChunkPresenter;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.port.android.impl.WebChunkWebViewClient;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.zip.ZipFile;

public class WebChunkActivity extends UstadBaseActivity implements WebChunkView {

    private WebChunkPresenter mPresenter;

    private WebView mWebView;
    private WebChunkWebViewClient webClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_chunk);

        mWebView = findViewById(R.id.activity_webchunk_webview);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);

        mPresenter = new WebChunkPresenter(getContext(),
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));

    }

    @Override
    public void mountChunk(String webChunkPath, UmCallback<String> callback) {
        webClient = new WebChunkWebViewClient(webChunkPath);
        mWebView.setWebViewClient(webClient);
        callback.onSuccess(webClient.getUrl());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webClient.close();
    }

    @Override
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }
}
