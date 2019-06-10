package com.ustadmobile.port.android.view

import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.H5PContentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import kotlinx.serialization.ImplicitReflectionSerializer

class H5PContentActivity : ContainerContentActivity(), H5PContentView {

    private var mPresenter: H5PContentPresenter? = null

    private var mWebView: WebView? = null

    @ImplicitReflectionSerializer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_h5p_content)

        mWebView = findViewById(R.id.activity_h5p_content_webview)
        if (Build.VERSION.SDK_INT >= 17) {
            mWebView!!.settings.mediaPlaybackRequiresUserGesture = false
        }

        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.settings.domStorageEnabled = true
        mWebView!!.settings.cacheMode = WebSettings.LOAD_DEFAULT
        mWebView!!.webViewClient = WebViewClient()
        mWebView!!.webChromeClient = WebChromeClient()

        setUMToolbar(R.id.um_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onHttpdConnected(httpd: EmbeddedHTTPD) {
        mPresenter = H5PContentPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this, httpd.containerMounter)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(intent.extras))
    }


    override fun onDestroy() {
        //TODO: Handle unmount of the container
        //val mountedPath = this.mountedPath.get()
//        if (mountedPath != null)
//            unmountZipFromHttp(mountedPath)

        super.onDestroy()
    }

    override fun setTitle(title: String) {
        super.setTitle(title)
    }

    override fun setContentHtml(baseUrl: String, html: String) {
        mWebView!!.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", baseUrl)
    }
}
