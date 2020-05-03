package com.ustadmobile.port.android.view

import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.ScormPackagePresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.ScormPackageView

class ScormPackageActivity : ZippedContentActivity(), ScormPackageView {

    private var mWebView: WebView? = null

    private var mPresenter: ScormPackagePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scorm_package)

        mWebView = findViewById(R.id.activity_scorm_package_webview)
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
        mPresenter = ScormPackagePresenter(this,
                UMAndroidUtil.bundleToMap(intent.extras), this)
    }

    override fun setTitle(title: String) {
        super.setTitle(title)
    }


    override fun onDestroy() {
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    override fun loadUrl(url: String) {
        mWebView!!.loadUrl(url)
    }
}
