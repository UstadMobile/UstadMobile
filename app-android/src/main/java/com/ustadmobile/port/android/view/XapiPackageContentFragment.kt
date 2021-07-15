package com.ustadmobile.port.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.toughra.ustadmobile.databinding.FragmentXapiPackageContentBinding
import com.ustadmobile.core.controller.XapiPackageContentPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.XapiPackageContentView

class XapiPackageContentFragment : UstadBaseFragment(), XapiPackageContentView {

    internal var networkManagerProvider: BleNetworkManagerProvider? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is BleNetworkManagerProvider){
            networkManagerProvider = context
        }
    }

    override var contentTitle: String = ""
        set(value) {
            field = value
            ustadFragmentTitle = value
        }

    override var url: String = ""
        set(value) {
            field = value
            mBinding?.url = value
        }

    private var mBinding: FragmentXapiPackageContentBinding? = null

    private var mPresenter: XapiPackageContentPresenter? = null

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
            val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            (context as? MainActivity)?.onAppBarExpand(isPortrait)
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
        mPresenter = XapiPackageContentPresenter(requireContext(), arguments.toStringMap(),
                this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        (context as? MainActivity)?.onAppBarExpand(isPortrait)
    }

    override fun onStop() {
        super.onStop()
        mPresenter?.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter?.onDestroy()
        mPresenter = null
        mBinding = null
    }

    override fun onDetach() {
        super.onDetach()
        networkManagerProvider = null
    }
}