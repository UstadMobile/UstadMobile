/*
package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.HarPresenter
import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.impl.PayloadRecorder
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.util.mimeTypeToPlayStoreIdMap
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.core.view.UstadViewWithSnackBar
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HarActivity : ContainerContentActivity(), UstadViewWithSnackBar, HarAndroidView {

    private var savedState: Bundle? = null
    private val clientDeferred = CompletableDeferred<HarWebViewClient>()

    private lateinit var mWebView: WebView
    private lateinit var mPresenter: HarPresenter
    val recorder = PayloadRecorder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_chunk)


        setUMToolbar(R.id.activity_webchunk_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        mWebView = findViewById(R.id.activity_webchunk_webview)
        mWebView.settings.javaScriptEnabled = true
        mWebView.settings.domStorageEnabled = true
        mWebView.settings.allowUniversalAccessFromFileURLs = true
        mWebView.settings.allowFileAccessFromFileURLs = true
        mWebView.settings.mediaPlaybackRequiresUserGesture = false
        mWebView.addJavascriptInterface(recorder, "recorder")

        savedState = savedInstanceState
    }

    override fun onHttpdConnected(httpd: EmbeddedHTTPD) {
        val repository = UmAccountManager.getRepositoryForActiveAccount(this)
        mPresenter = HarPresenter(this, UMAndroidUtil.bundleToMap(intent.extras), this, true, repository, httpd.localHttpUrl)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedState))
    }

    override fun loadUrl(url: String) {
        GlobalScope.launch(Dispatchers.Main) {
            clientDeferred.await()
            mWebView.loadUrl(url)
        }
    }

    override fun onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack()
        } else {
            super.onBackPressed()
        }
    }
    override fun setToolbarTitle(title: String) {
        umToolbar.title = title
    }


}*/
