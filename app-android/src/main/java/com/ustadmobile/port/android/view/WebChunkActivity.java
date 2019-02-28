package com.ustadmobile.port.android.view;

import android.content.Context;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.WebChunkPresenter;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.view.WebChunkView;
import com.ustadmobile.port.android.impl.WebChunkWebViewClient;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class WebChunkActivity extends UstadBaseActivity implements WebChunkView {

    private WebChunkPresenter mPresenter;

    private WebView mWebView;
    private WebChunkWebViewClient webClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_chunk);

        setUMToolbar(R.id.activity_webchunk_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

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
        webClient = new WebChunkWebViewClient(webChunkPath, mPresenter);
        mWebView.setWebViewClient(webClient);
        callback.onSuccess(webClient.getUrl());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        webClient.close();
    }

    private void clickUpNavigation() {
        runOnUiThread(() -> {
            if (mPresenter != null) {
                mPresenter.handleUpNavigation();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                clickUpNavigation();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void showError(String message) {
        runOnUiThread(() -> {
            Toast.makeText((Context) getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void setToolbarTitle(String title) {
        runOnUiThread(() -> {
            getUMToolbar().setTitle(title);
        });
    }
}
