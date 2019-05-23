package com.ustadmobile.port.android.view

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.H5PContentPresenter
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.H5PContentView
import com.ustadmobile.port.android.netwokmanager.EmbeddedHttpdService
import com.ustadmobile.port.android.util.UMAndroidUtil

import java.util.concurrent.atomic.AtomicReference

class H5PContentActivity : ZippedContentActivity(), H5PContentView {

    private var mPresenter: H5PContentPresenter? = null

    private var mWebView: WebView? = null

    private val mountedPath = AtomicReference<String>()

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
        mPresenter = H5PContentPresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
        runWhenHttpdReady { mPresenter!!.onCreate(UMAndroidUtil.bundleToMap(intent.extras)) }
    }

    override fun mountH5PDist(callback: UmCallback<String>) {
        callback.onSuccess(UMFileUtil.joinPaths(EmbeddedHttpdService.ANDROID_ASSETS_PATH,
                "h5p/dist"))
    }

    override fun mountH5PContainer(containerUid: Long, callback: UmCallback<String>) {
        mountContainer(containerUid, object : UmCallback<String> {
            override fun onSuccess(result: String?) {
                mountedPath.set(result)
                UmCallbackUtil.onSuccessIfNotNull(callback, result)
            }

            override fun onFailure(exception: Throwable?) {
                UmCallbackUtil.onFailIfNotNull(callback, exception!!)
            }
        })
    }

    override fun onDestroy() {
        val mountedPath = this.mountedPath.get()
        if (mountedPath != null)
            unmountZipFromHttp(mountedPath)

        super.onDestroy()
    }

    override fun setTitle(title: String) {
        super.setTitle(title)
    }

    override fun setContentHtml(baseUrl: String, html: String) {
        mWebView!!.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", baseUrl)
    }
}
