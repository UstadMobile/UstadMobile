package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.H5PContentPresenter;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.H5PContentView;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

public class H5PContentActivity extends ZippedContentActivity implements H5PContentView {

    protected NetworkManagerAndroid networkManagerAndroid;

    private H5PContentPresenter mPresenter;

    private WebView mWebView;

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
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        super.onServiceConnected(name, iBinder);
        if (name.getClassName().equals(NetworkServiceAndroid.class.getName())) {
            networkManagerAndroid = ((NetworkServiceAndroid.LocalServiceBinder)iBinder).getService()
                    .getNetworkManager();
            mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getIntent().getExtras()));
        }
    }


    @Override
    public void mountH5PDist(UmCallback<String> callback) {
        callback.onSuccess(UMFileUtil.joinPaths(networkManagerAndroid.getHttpAndroidAssetsUrl(),
                "h5p/dist"));
    }

    @Override
    public void mountH5PFile(String zipFile, UmCallback<String> callback) {
        new MountZipAsyncTask(networkManagerAndroid, callback).execute(zipFile);
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
