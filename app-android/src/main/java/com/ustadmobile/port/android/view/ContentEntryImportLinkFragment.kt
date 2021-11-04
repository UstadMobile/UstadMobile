package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentEntryImportLinkBinding
import com.ustadmobile.core.controller.ContentEntryImportLinkPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ContentEntryImportLinkView


class ContentEntryImportLinkFragment() : UstadEditFragment<String>(), ContentEntryImportLinkView {

    private var mBinding: FragmentEntryImportLinkBinding? = null

    private var mPresenter: ContentEntryImportLinkPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, String>?
        get() = mPresenter

    private var menuDoneItem: MenuItem? = null

    override var showHideProgress: Boolean = false
        set(value) {
            field = value
            mBinding?.entryImportLinkTextInput?.isEnabled = value
            menuDoneItem?.isEnabled = value
        }

    override var validLink: Boolean = false
        set(value) {
            mBinding?.entryImportLinkTextInput?.isErrorEnabled = value
            mBinding?.entryImportLinkTextInput?.error = if(value) null else getString(R.string.invalid_link)
            field = value
        }
    override var entity: String? = null
        get() = field
        set(value) {
            field = value
            mBinding?.importLink = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentEntryImportLinkBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ustadFragmentTitle = getString(R.string.enter_url)
        setHasOptionsMenu(true)

        mPresenter = ContentEntryImportLinkPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toStringMap())

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_done, menu)
        menuDoneItem = menu.findItem(R.id.menu_done)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
        menuDoneItem = null
        mBinding = null

    }


}