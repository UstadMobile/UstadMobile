package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentDiscussionTopicEditBinding
import com.ustadmobile.core.controller.DiscussionTopicEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.DiscussionTopicEditView
import com.ustadmobile.lib.db.entities.DiscussionTopic

class DiscussionTopicEditFragment: UstadEditFragment<DiscussionTopic>(),
    DiscussionTopicEditView {


    private var mBinding: FragmentDiscussionTopicEditBinding? = null

    private var mPresenter: DiscussionTopicEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, DiscussionTopic>?
        get() = mPresenter



    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        val rootView: View
        mBinding = FragmentDiscussionTopicEditBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
        }

        mPresenter = DiscussionTopicEditPresenter(requireContext(),
            arguments.toStringMap(), this, viewLifecycleOwner, di).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_topic, R.string.edit_topic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
    }

    override var loading: Boolean = false

    override var entity: DiscussionTopic? = null
        set(value) {
            field = value
            mBinding?.discussionTopic = value
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