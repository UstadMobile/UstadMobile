package com.ustadmobile.port.android.view

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.H5PContentPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.core.view.H5PContentView.Companion.ANDROID_ASSETS_PATH
import com.ustadmobile.sharedse.network.AndroidAssetsHandler
import com.ustadmobile.sharedse.network.EmbeddedHttpdService
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.serialization.ImplicitReflectionSerializer


class H5PContentActivity : UstadBaseActivity(), H5PContentView {


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

    override fun onBleNetworkServiceBound(networkManagerBle: NetworkManagerBle?) {
        super.onBleNetworkServiceBound(networkManagerBle)
        val httpd = networkManagerBle!!.httpd
        httpd.addRoute("$ANDROID_ASSETS_PATH(.)+",
                AndroidAssetsHandler::class.java, applicationContext)
        mPresenter = H5PContentPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this,
                httpd.containerMounter)
        mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(intent.extras))
    }

    override fun setContentTitle(title: String) {
        setTitle(title)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == android.R.id.home){
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setContentHtml(baseUrl: String, html: String) {
        mWebView!!.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", baseUrl)
    }
}
