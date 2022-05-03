package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentDiscussionPostEditBinding
import com.ustadmobile.core.controller.DiscussionPostEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.DiscussionPostEditView
import com.ustadmobile.lib.db.entities.DiscussionPost

class DiscussionPostEditFragment: UstadEditFragment<DiscussionPost>(),
    DiscussionPostEditView {


    private var mBinding: FragmentDiscussionPostEditBinding? = null

    private var mPresenter: DiscussionPostEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, DiscussionPost>?
        get() = mPresenter



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentDiscussionPostEditBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
        }

        mPresenter = DiscussionPostEditPresenter(requireContext(),
            arguments.toStringMap(), this, di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.post, R.string.edit_topic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var loading: Boolean = false

    override var entity: DiscussionPost? = null
        set(value) {
            field = value
            mBinding?.discussionPost = value
        }

    override var blockTitleError: String? = null
        set(value) {
            field = value
            mBinding?.blockTitleError = value
        }

    override var fieldsEnabled: Boolean = false
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

}