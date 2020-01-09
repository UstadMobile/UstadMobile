package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.XapiPackageContentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.view.XapiPackageContentView
import java.util.concurrent.atomic.AtomicReference
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD

/**
 * Created by mike on 9/14/17.
 */

class XapiPackageContentActivity : ContainerContentActivity(), XapiPackageContentView {

    private lateinit var mPresenter: XapiPackageContentPresenter

    private var mMountedPath: AtomicReference<String>? = null

    private var mWebView: WebView? = null

    private var mProgressBar: ProgressBar? = null

    private var savedState: Bundle? = null

    @SuppressLint("SetJavaScriptEnabled", "ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_xapi_content_package)
        mWebView = findViewById(R.id.activity_xapi_package_webview)
        mProgressBar = findViewById(R.id.progressBar)

        if (Build.VERSION.SDK_INT >= 17) {
            mWebView!!.settings.mediaPlaybackRequiresUserGesture = false
        }

        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.settings.domStorageEnabled = true
        mWebView!!.settings.cacheMode = WebSettings.LOAD_DEFAULT
        mWebView!!.webViewClient = WebViewClient()
        mWebView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)

                if (newProgress == 100) {
                    mProgressBar!!.visibility = View.GONE
                } else {
                    if (mProgressBar!!.isIndeterminate)
                        mProgressBar!!.isIndeterminate = false

                    mProgressBar!!.progress = newProgress
                }
            }
        }

        setSupportActionBar(findViewById(R.id.um_toolbar))
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mMountedPath = AtomicReference()

        mProgressBar!!.isIndeterminate = true
        mProgressBar!!.visibility = View.VISIBLE
        savedState = savedInstanceState
    }

    override fun onHttpdConnected(httpd: EmbeddedHTTPD) {
        mPresenter = XapiPackageContentPresenter(this,
                bundleToMap(intent.extras), this, httpd.containerMounter)

        mPresenter.onCreate(bundleToMap(savedState))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun setTitle(title: String) {
        super.setTitle(title)
    }

    override fun loadUrl(url: String) {
        UMLog.l(UMLog.INFO, 0, "Xapi: Loading: $url")
        mWebView!!.loadUrl(url)
    }

    override fun onDestroy() {
       /* val mountedPath = mMountedPath!!.get()
        if (mountedPath != null)
            super.unmountContainer(mountedPath) */

        super.onDestroy()
    }

    override fun showSnackBarNotification(errorMessage: String, action: () -> Unit, actionMessageId: Int) {
        mProgressBar!!.progress = 0
        mProgressBar!!.visibility = View.GONE
    }
}
