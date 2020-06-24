package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toughra.ustadmobile.databinding.FragmentXapiPackageContentBinding
import com.ustadmobile.core.controller.XapiPackageContentFragmentPresenter
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.sharedse.network.NetworkManagerBle
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicReference

class XapiPackageContentFragment(private val networkServiceProvider:CompletableDeferred<NetworkManagerBle>? = null) : UstadBaseFragment(), XapiPackageContentView {

    override var contentTitle: String = ""
        set(value) {
            field = value
            title = value
        }

    override var urlToLoad: String = ""
        set(value) {
            field = value
            mBinding?.urlToLoad = value
        }

    override suspend fun mountContainer(containerUid: Long): String {
        val containerPath = networkManagerBle?.httpd?.mountContainer(containerUid,null)
        mountedContainerPath?.set(UMFileUtil.joinPaths(networkManagerBle?.httpd?.localHttpUrl.toString(),
                containerPath.toString()))
        return mountedContainerPath?.get()?:""
    }


    override suspend fun unMountContainer() {
        val path = mountedContainerPath?.get()
        if(path != null)
        networkManagerBle?.httpd?.unmountContainer(path)
    }

    private var mBinding: FragmentXapiPackageContentBinding? = null

    private var mPresenter: XapiPackageContentFragmentPresenter? = null

    private var networkManagerBle: NetworkManagerBle ? = null

    private var mountedContainerPath: AtomicReference<String>? = AtomicReference("")

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentXapiPackageContentBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.progressBar.isIndeterminate = true
            it.progressBar.visibility = View.VISIBLE
            it.activityXapiPackageWebview.settings.javaScriptEnabled = true
            it.activityXapiPackageWebview.settings.domStorageEnabled = true
            it.activityXapiPackageWebview.settings.cacheMode = WebSettings.LOAD_DEFAULT
            it.activityXapiPackageWebview.webViewClient = WebViewClient()
        }

        mBinding?.activityXapiPackageWebview?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                loading = newProgress != 100
                if (loading) {
                    val isIndeterminate = mBinding?.progressBar?.isIndeterminate
                    if (isIndeterminate != null && isIndeterminate)
                        mBinding?.progressBar?.isIndeterminate = false

                    mBinding?.progressBar?.progress = newProgress
                } else {
                    mBinding?.progressBar?.visibility = View.GONE
                }
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        GlobalScope.launch {
            val thisFrag = this@XapiPackageContentFragment
            networkManagerBle = (networkServiceProvider?:(activity as? MainActivity)?.networkManagerBle)?.await()
            withContext(Dispatchers.Main){
                mPresenter = XapiPackageContentFragmentPresenter(requireContext(), arguments.toStringMap(), thisFrag)
                mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        networkManagerBle = null
        mPresenter = null
        mBinding = null
    }
}