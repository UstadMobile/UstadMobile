package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.XapiPackagePresenter;
import com.ustadmobile.core.impl.UMLog;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UstadMobileSystemImpl;
import com.ustadmobile.core.impl.ZipFileHandle;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.XapiPackageView;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;
import com.ustadmobile.port.sharedse.impl.zip.ZipFileHandleSharedSE;

/**
 * Created by mike on 9/14/17.
 */

public class XapiPackageActivity extends ZippedContentActivity implements XapiPackageView {

    private XapiPackagePresenter mPresenter;

    private NetworkManagerAndroid networkManagerAndroid;

    private String mMountedPath;

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

        mPresenter = new XapiPackagePresenter(this,this);
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
    public void mountZip(String zipUri, UmCallback callback) {
        new MountZipAsyncTask(networkManagerAndroid, callback).execute(zipUri);
    }

    @Override
    public XapiPackagePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    public ZipFileHandle getMountedZipHandle() {
        return new ZipFileHandleSharedSE(networkManagerAndroid.getHttpMountedZip(mMountedPath));
    }

    @Override
    public void onDestroy() {
        if(mMountedPath != null)
            networkManagerAndroid.unmountZipFromHttp(mMountedPath);

        super.onDestroy();
    }
}
