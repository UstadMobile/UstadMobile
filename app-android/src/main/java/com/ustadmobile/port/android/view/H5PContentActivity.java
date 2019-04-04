package com.ustadmobile.port.android.view;

import android.os.Build;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.H5PContentPresenter;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.H5PContentView;
import com.ustadmobile.port.android.netwokmanager.EmbeddedHttpdService;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import java.util.concurrent.atomic.AtomicReference;

public class H5PContentActivity extends ZippedContentActivity implements H5PContentView {

    private H5PContentPresenter mPresenter;

    private WebView mWebView;

    private AtomicReference<String> mountedPath = new AtomicReference<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_h5p_content);

        mWebView = (WebView)findViewById(R.id.activity_h5p_content_webview);
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
        mPresenter = new H5PContentPresenter(this, this);
        runWhenHttpdReady(() ->
                mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getIntent().getExtras())));
    }

    @Override
    public void mountH5PDist(UmCallback<String> callback) {
        callback.onSuccess(UMFileUtil.joinPaths(EmbeddedHttpdService.ANDROID_ASSETS_PATH,
                "h5p/dist"));
    }

    @Override
    public void mountH5PFile(String zipFile, UmCallback<String> callback) {
        mountZip(zipFile, new UmCallback<String>() {
            @Override
            public void onSuccess(String result) {
                mountedPath.set(result);
                UmCallbackUtil.onSuccessIfNotNull(callback, result);
            }

            @Override
            public void onFailure(Throwable exception) {
                UmCallbackUtil.onFailIfNotNull(callback, exception);
            }
        });
    }

    @Override
    public void onDestroy() {
        String mountedPath = this.mountedPath.get();
        if(mountedPath != null)
            unmountZipFromHttp(mountedPath);

        super.onDestroy();
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void setContentHtml(String baseUrl, String html) {
        mWebView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", baseUrl);
    }
}
