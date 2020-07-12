package com.ustadmobile.port.android.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.toughra.ustadmobile.databinding.FragmentWebChunkBinding
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.controller.WebChunkPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.util.mimeTypeToPlayStoreIdMap
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.android.impl.WebChunkWebViewClient

@ExperimentalStdlibApi
class WebChunkFragment : UstadBaseFragment(), WebChunkView, FragmentBackHandler {

    private var mBinding: FragmentWebChunkBinding? = null

    private var webView: WebView? = null

    private var mPresenter: WebChunkPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentWebChunkBinding.inflate(inflater, container, false).also {
            webView = it.webchunkWebview
        }

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true
        webView?.settings?.allowUniversalAccessFromFileURLs = true
        webView?.settings?.allowFileAccessFromFileURLs = true
        webView?.settings?.mediaPlaybackRequiresUserGesture = false

        mPresenter = WebChunkPresenter(this,
                arguments.toStringMap(), this, di)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return mBinding?.root
    }


    override fun onHostBackPressed(): Boolean {
        return if (webView?.canGoBack() == true) {
            webView?.goBack()
            true
        } else {
            false
        }

    }

    override var entry: ContentEntry? = null
        get() = field
        set(value) {
            field = value
            title = value?.title
        }

    override var containerManager: ContainerManager? = null
        get() = field
        set(value) {
            field = value
            if (value == null) {
                showSnackBar(UstadMobileSystemImpl.instance
                        .getString(MessageID.error_opening_file, this))
                return
            }
            val webClient = WebChunkWebViewClient(value, mPresenter)
            runOnUiThread(Runnable {
                webView?.webViewClient = webClient
                webView?.loadUrl(webClient.url)
            })
        }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        webView = null
    }

    override fun showNoAppFoundError(message: String, actionMessageId: Int, mimeType: String) {
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
}