package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEntryImportLinkBinding
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryImportLinkView

class ContentEntryImportLinkFragment : UstadBaseFragment(), ContentEntryImportLinkView {

    private var mBinding: FragmentEntryImportLinkBinding? = null

    private var mPresenter: ContentEntryImportLinkPresenter? = null

    override var inProgress: Boolean
        get() = mBinding?.inProgress ?: false
        set(value) {
            loading = value
            mBinding?.inProgress = value
        }


    override var validLink: Boolean = false
        set(value) {
            mBinding?.entryImportLinkTextInput?.isErrorEnabled = value
            mBinding?.entryImportLinkTextInput?.error = if(value) null else getString(R.string.invalid_link)
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentEntryImportLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.entryImportLinkEditText.setOnEditorActionListener { v, actionId, event ->
                val importLinkVal = it.importLink
                if(actionId == EditorInfo.IME_ACTION_GO && importLinkVal != null){
                    mPresenter?.handleClickDone(importLinkVal)
                    true
                }else {
                    false
                }
            }
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ustadFragmentTitle = getString(R.string.enter_url)
        setHasOptionsMenu(true)

        mPresenter = ContentEntryImportLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di).withViewLifecycle()
        mBinding?.mPresenter = mPresenter
        mPresenter?.onCreate(savedInstanceState.toStringMap())
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_done -> {
                mPresenter?.handleClickDone(mBinding?.entryImportLinkEditText?.text.toString())
                return super.onOptionsItemSelected(item)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        mBinding = null

    }


}