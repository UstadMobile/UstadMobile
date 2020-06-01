package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentClazzWorkSubmissionEditBinding
import com.ustadmobile.core.controller.ClazzWorkSubmissionEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ClazzWorkSubmissionEditView
import com.ustadmobile.lib.db.entities.ClazzWorkSubmissionWithClazzWork


interface ClazzWorkSubmissionEditFragmentEventHandler {

}

class ClazzWorkSubmissionEditFragment: UstadEditFragment<ClazzWorkSubmissionWithClazzWork>(),
        ClazzWorkSubmissionEditView, ClazzWorkSubmissionEditFragmentEventHandler {

    private var mBinding: FragmentClazzWorkSubmissionEditBinding? = null

    private var mPresenter: ClazzWorkSubmissionEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, ClazzWorkSubmissionWithClazzWork>?
        get() = mPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentClazzWorkSubmissionEditBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        mPresenter = ClazzWorkSubmissionEditPresenter(requireContext(), arguments.toStringMap(), this,
                this, UstadMobileSystemImpl.instance,
                UmAccountManager.getActiveDatabase(requireContext()),
                UmAccountManager.getRepositoryForActiveAccount(requireContext()),
                UmAccountManager.activeAccountLiveData)
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override fun onResume() {
        super.onResume()
        setEditFragmentTitle(R.string.submission)
    }

    override var entity: ClazzWorkSubmissionWithClazzWork? = null
        get() = field
        set(value) {
            field = value
            mBinding?.clazzWorkSubmission = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            field = value
            mBinding?.fieldsEnabled = value
        }
}