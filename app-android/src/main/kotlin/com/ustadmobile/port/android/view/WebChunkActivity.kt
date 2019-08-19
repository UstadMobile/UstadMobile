package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.webkit.WebView
import com.toughra.ustadmobile.R
import com.ustadmobile.core.controller.WebChunkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil.bundleToMap
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.view.ViewWithErrorNotifier
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.port.android.impl.WebChunkWebViewClient

class WebChunkActivity : UstadBaseActivity(), WebChunkView, ViewWithErrorNotifier {

    private var mPresenter: WebChunkPresenter? = null

    private var mWebView: WebView? = null
    private var webClient: WebChunkWebViewClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_chunk)

        setUMToolbar(R.id.activity_webchunk_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        mWebView = findViewById(R.id.activity_webchunk_webview)
        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.settings.domStorageEnabled = true
        mWebView!!.settings.allowUniversalAccessFromFileURLs = true
        mWebView!!.settings.allowFileAccessFromFileURLs = true
        mWebView!!.settings.mediaPlaybackRequiresUserGesture = false

        mPresenter = WebChunkPresenter(this,
                bundleToMap(intent.extras), this)
        mPresenter!!.onCreate(bundleToMap(savedInstanceState))

    }

    override fun mountChunk(container: Container, callback: UmCallback<String>) {
        webClient = WebChunkWebViewClient(container, mPresenter!!, this)
        runOnUiThread {
            mWebView!!.webViewClient = webClient
            callback.onSuccess(webClient!!.url)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun clickUpNavigation() {
        if (mPresenter != null) {
            mPresenter!!.handleUpNavigation()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Respond to the action bar's Up/Home button
            android.R.id.home -> {
                runOnUiThread { this.clickUpNavigation() }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun loadUrl(url: String) {
        mWebView!!.loadUrl(url)
    }

    override fun showError(message: String) {
        showErrorNotification(message, {}, 0)
    }

    override fun setToolbarTitle(title: String) {
        umToolbar.title = title
    }

    override fun showErrorWithAction(message: String, actionMessageId: Int, mimeType: String) {
        showErrorNotification(message, {
            var appPackageName = ContentEntryUtil.mimeTypeToPlayStoreIdMap[mimeType]
            if (appPackageName == null) {
                appPackageName = "cn.wps.moffice_eng"
            }
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
            } catch (anfe: android.content.ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
            }
        }, actionMessageId)
    }
}
