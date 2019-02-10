package com.ustadmobile.port.android.view;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.XapiPackagePresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.view.XapiPackageView;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mike on 9/14/17.
 */

public class XapiPackageActivity extends ZippedContentActivity implements XapiPackageView {

    private XapiPackagePresenter mPresenter;

    private AtomicReference<String> mMountedPath;

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xapi_package);
        mWebView = (WebView)findViewById(R.id.activity_xapi_package_webview);
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

        mMountedPath = new AtomicReference<>();

        mPresenter = new XapiPackagePresenter(this,this);
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
    public void mountZip(String zipUri, UmCallback<String> callback) {
        super.mountZip(zipUri, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                mMountedPath.set(result);
                UmCallbackUtil.onSuccessIfNotNull(callback, result);
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });
    }

    @Override
    public XapiPackagePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public void onDestroy() {
        if(mMountedPath != null)
            super.unmountZipFromHttp(mMountedPath.get());

        super.onDestroy();
    }
}
