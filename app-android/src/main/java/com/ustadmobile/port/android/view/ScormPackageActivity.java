package com.ustadmobile.port.android.view;

import android.content.ComponentName;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.toughra.ustadmobile.R;
import com.ustadmobile.core.controller.ScormPackagePresenter;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.util.UMFileUtil;
import com.ustadmobile.core.view.ScormPackageView;
import com.ustadmobile.port.android.netwokmanager.NetworkManagerAndroid;
import com.ustadmobile.port.android.netwokmanager.NetworkServiceAndroid;
import com.ustadmobile.port.android.util.UMAndroidUtil;

import org.acra.ACRA;

public class ScormPackageActivity extends UstadBaseActivity implements ScormPackageView {

    private NetworkManagerAndroid networkManagerAndroid;

    private String mMountedPath;

    private WebView mWebView;

    private ScormPackagePresenter mPresenter;


    private static class MountZipAsyncTask extends AsyncTask<String, Void, String> {

        private NetworkManagerAndroid networkManagerAndroid;

        private UmCallback callback;

        private MountZipAsyncTask(NetworkManagerAndroid networkManagerAndroid, UmCallback callback) {
            this.networkManagerAndroid = networkManagerAndroid;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(String... strings) {
            String mountedUri = networkManagerAndroid.mountZipOnHttp(strings[0], null, false, null);
            return mountedUri != null ?
                    UMFileUtil.joinPaths(networkManagerAndroid.getLocalHttpUrl(), mountedUri) : null;
        }

        @Override
        protected void onPostExecute(String mountedPath) {
            if(mountedPath != null) {
                callback.onSuccess(mountedPath);
            }else {
                Exception mountException= new RuntimeException("Zip mounted as null: corrupt file?");
                ACRA.getErrorReporter().handleSilentException(mountException);
                callback.onFailure(mountException);
            }
        }
    }




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
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        super.onServiceConnected(name, iBinder);
        if (name.getClassName().equals(NetworkServiceAndroid.class.getName())) {
            networkManagerAndroid = ((NetworkServiceAndroid.LocalServiceBinder)iBinder).getService()
                    .getNetworkManager();
            mPresenter.onCreate(UMAndroidUtil.bundleToHashtable(getIntent().getExtras()));
        }
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
    }

    @Override
    public void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void mountZip(String zipUri, UmCallback callback) {
        new MountZipAsyncTask(networkManagerAndroid, callback).execute(zipUri);
    }
}
