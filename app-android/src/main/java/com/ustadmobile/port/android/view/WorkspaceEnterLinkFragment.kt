package com.ustadmobile.port.android.view

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentWorkSpaceEnterLinkBinding
import com.ustadmobile.core.controller.WorkspaceEnterLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.WorkspaceEnterLinkView
import java.util.concurrent.atomic.AtomicBoolean


class WorkspaceEnterLinkFragment : UstadBaseFragment(), WorkspaceEnterLinkView{

    private var mBinding: FragmentWorkSpaceEnterLinkBinding? = null

    private var mPresenter: WorkspaceEnterLinkPresenter? = null

    private val inputCheckDelay: Long = 500

    private val inputCheckHandler: Handler = Handler()

    private val inputCheckerCallback = Runnable {
        val typedLink = workspaceLink
        if(typedLink != null){
            mPresenter?.handleCheckLinkText(typedLink)
        }
    }

    override var workspaceLink: String?
        get() = mBinding?.workspaceLink
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
        mBinding?.workspaceLinkView?.isErrorEnabled = isError
        mBinding?.workspaceLinkView?.error = if(isError) getString(R.string.invalid_url) else null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentWorkSpaceEnterLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.showButton = false
            it.showProgress = false
        }
        mPresenter = WorkspaceEnterLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),this)
        mPresenter?.onCreate(savedInstanceState.toStringMap())
        mBinding?.organisationLink?.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(url: CharSequence?, start: Int, before: Int, count: Int) {
                inputCheckHandler.removeCallbacks(inputCheckerCallback)
            }
            override fun afterTextChanged(s: Editable?) {
                progressVisible = true
                inputCheckHandler.postDelayed(inputCheckerCallback, inputCheckDelay)
            }
        })

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        workspaceLink = null
        mBinding = null
    }
}