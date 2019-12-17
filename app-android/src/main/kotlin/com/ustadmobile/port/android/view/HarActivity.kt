package com.ustadmobile.port.android.view

import android.os.Bundle
import android.webkit.WebView
import com.ustadmobile.core.controller.HarPresenter
import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.core.view.ViewWithErrorNotifier
import kotlinx.coroutines.*

class HarActivity : UstadBaseActivity(), ViewWithErrorNotifier, HarAndroidView {

    private val clientDeferred = CompletableDeferred<HarWebViewClient>()

    private lateinit var mWebView: WebView
    private lateinit var mPresenter: HarPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        mPresenter = HarPresenter(this, UMAndroidUtil.bundleToMap(intent.extras), this)
        mPresenter.onCreate(UMAndroidUtil.bundleToMap(savedInstanceState))
    }

    override fun loadUrl(url: String) {
        GlobalScope.launch(Dispatchers.Main) {
            clientDeferred.await()
            mWebView.loadUrl(url)
        }
    }

    override fun showError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setToolbarTitle(title: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showErrorWithAction(message: String, actionMessageId: Int, mimeType: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setChromeClient(client: HarWebViewClient) {
        mWebView.webViewClient = client
        clientDeferred.complete(client)
    }

}