package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentSiteEnterLinkBinding
import com.ustadmobile.core.controller.SiteEnterLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.SiteEnterLinkView


class SiteEnterLinkFragment : UstadBaseFragment(), SiteEnterLinkView{

    private var mBinding: FragmentSiteEnterLinkBinding? = null

    private var mPresenter: SiteEnterLinkPresenter? = null

    private val inputCheckDelay: Long = 500

    private val inputCheckHandler: Handler = Handler(Looper.getMainLooper())

    private val inputCheckerCallback = Runnable {
        val typedLink = siteLink
        if(typedLink != null){
            progressVisible = true
            mPresenter?.handleCheckLinkText(typedLink)
        }
    }

    override var siteLink: String?
        get() = mBinding?.siteLink
        set(value) {}

    override var validLink: Boolean = false
        set(value) {
            handleError(!value)
            mBinding?.showButton = value
            field = value
        }

    override var progressVisible: Boolean = false
        set(value) {
            field = value
            mBinding?.showProgress = value
        }

    private fun handleError(isError: Boolean){
        mBinding?.siteLinkView?.isErrorEnabled = isError
        mBinding?.siteLinkView?.error = if(isError) getString(R.string.invalid_link) else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentSiteEnterLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.showButton = false
            it.showProgress = false
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter = SiteEnterLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
            this, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        mBinding?.presenter = mPresenter
        mBinding?.organisationLink?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(url: CharSequence?, start: Int, before: Int, count: Int) {
                inputCheckHandler.removeCallbacks(inputCheckerCallback)
            }
            override fun afterTextChanged(s: Editable?) {
                inputCheckHandler.postDelayed(inputCheckerCallback, inputCheckDelay)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        siteLink = null
        mBinding = null
    }
}