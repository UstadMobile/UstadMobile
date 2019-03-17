package com.ustadmobile.port.android.view;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.XapiPackageContentPresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.XapiPackageContentView;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mike on 9/14/17.
 */

public class XapiPackageContentActivity extends ZippedContentActivity implements XapiPackageContentView {

    private XapiPackageContentPresenter mPresenter;

    private AtomicReference<String> mMountedPath;

    private WebView mWebView;

    private ProgressBar mProgressBar;

    @SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xapi_content_package);
        mWebView = findViewById(R.id.activity_xapi_package_webview);
        mProgressBar = findViewById(R.id.progressBar);

        if(Build.VERSION.SDK_INT >= 17) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        }

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);

                if(newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                }else {
                    if(mProgressBar.isIndeterminate())
                        mProgressBar.setIndeterminate(false);

                    mProgressBar.setProgress(newProgress);
                }
            }
        });

        setSupportActionBar(findViewById(R.id.um_toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMountedPath = new AtomicReference<>();

        mPresenter = new XapiPackageContentPresenter(this,
                UMAndroidUtil.bundleToHashtable(getIntent().getExtras()), this);
        mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(savedInstanceState));
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void loadUrl(String url) {
        UstadMobileSystemImpl.l(UMLog.INFO, 0, "Xapi: Loading: " +url);
        mWebView.loadUrl(url);
    }

    @Override
    public void onDestroy() {
        String mountedPath = mMountedPath.get();
        if(mountedPath!= null)
            super.unmountContainer(mountedPath);

        super.onDestroy();
    }

    @Override
    public void showErrorNotification(String errorMessage) {
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(View.GONE);
    }
}
