package com.ustadmobile.port.android.view

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.toughra.ustadmobile.databinding.FragmentHarContentBinding
import com.ustadmobile.core.controller.HarContentPresenter
import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.impl.PayloadRecorder
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.util.mimeTypeToPlayStoreIdMap
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.impl.http.EmbeddedHTTPD
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.instance


class HarContentFragment : UstadBaseFragment(), HarAndroidView, FragmentBackHandler {

    private val clientDeferred = CompletableDeferred<HarWebViewClient>()

    private var webView: WebView? = null
    private var presenter: HarContentPresenter? = null
    val recorder = PayloadRecorder()

    private var mBinding: FragmentHarContentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentHarContentBinding.inflate(inflater, container, false).also {
            webView = it.harWebview
            webView?.settings?.javaScriptEnabled = true
            webView?.settings?.domStorageEnabled = true
            webView?.settings?.allowUniversalAccessFromFileURLs = true
            webView?.settings?.allowFileAccessFromFileURLs = true
            webView?.settings?.mediaPlaybackRequiresUserGesture = false
            webView?.addJavascriptInterface(recorder, "recorder")
        }

        return mBinding?.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch(Dispatchers.Main) {
            val thisFrag = this@HarContentFragment
            val embeddedHttp: EmbeddedHTTPD by di.instance()
            presenter = HarContentPresenter(requireContext(), arguments.toStringMap(),
                    thisFrag, embeddedHttp.localHttpUrl, di).withViewLifecycle()
            presenter?.onCreate(savedInstanceState.toNullableStringMap())
        }
    }


    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            ustadFragmentTitle = value?.title
        }

    override fun setChromeClient(client: HarWebViewClient) {
        webView?.webViewClient = client
        client.setRecoder(recorder)
        clientDeferred.complete(client)
    }

    override fun loadUrl(url: String) {
        GlobalScope.launch(Dispatchers.Main) {
            clientDeferred.await()
            webView?.loadUrl(url)
        }
    }

    override fun showErrorWithAction(message: String, actionMessageId: Int, mimeType: String) {
        showSnackBar(message, {
            var appPackageName = mimeTypeToPlayStoreIdMap[mimeType]
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

    override fun onHostBackPressed(): Boolean {
        return if (webView?.canGoBack() == true) {
            webView?.goBack()
            true
        } else {
            false
        }
    }
}