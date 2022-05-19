package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseDiscussionCourseBlockEditBinding
import com.ustadmobile.core.controller.CourseDiscussionEditPresenter
import com.ustadmobile.core.controller.UstadEditPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseDiscussionEditView
import com.ustadmobile.door.DoorMutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ustadmobile.lib.db.entities.CourseBlockWithEntity
import com.ustadmobile.lib.db.entities.DiscussionTopic

class CourseDiscussionEditFragment: UstadEditFragment<CourseBlockWithEntity>(),
    CourseDiscussionEditView {

    private var mBinding: FragmentCourseDiscussionCourseBlockEditBinding? = null

    private var mPresenter: CourseDiscussionEditPresenter? = null

    override val mEditPresenter: UstadEditPresenter<*, CourseBlockWithEntity>?
        get() = mPresenter

    private var topicListRecyclerAdapter: DiscussionTopicDraggableRecyclerAdapter? = null

    private var topicListRecyclerView: RecyclerView? = null

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View
        mBinding = FragmentCourseDiscussionCourseBlockEditBinding.inflate(inflater, container,
            false).also {
            rootView = it.root
        }

        topicListRecyclerView =
            rootView.findViewById(R.id.fragment_course_discussion_course_block_edit_topic_list_rv)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditFragmentTitle(R.string.add_discussion, R.string.edit_discussion)

        mPresenter = CourseDiscussionEditPresenter(requireContext(),
            arguments.toStringMap(), this, viewLifecycleOwner, di).withViewLifecycle()

        topicListRecyclerAdapter = DiscussionTopicDraggableRecyclerAdapter(mPresenter,
            mBinding?.fragmentCourseDiscussionCourseBlockEditTopicListRv)

        topicListRecyclerView?.adapter = topicListRecyclerAdapter
        topicListRecyclerView?.layoutManager = LinearLayoutManager(requireContext())


        mBinding?.presenter = mPresenter
        mPresenter?.onCreate(backStackSavedState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null
        topicListRecyclerView = null
        topicListRecyclerAdapter = null
    }

    override var loading: Boolean = false

    override var entity: CourseBlockWithEntity? = null
        get() = field
        set(value) {
            field = value
            mBinding?.block = value
        }
    override var blockTitleError: String? = null
        set(value) {
            field = value
            mBinding?.blockTitleError = value
        }

    override var fieldsEnabled: Boolean = false
        get() = field
        set(value) {
            super.fieldsEnabled = value
            field = value
            mBinding?.fieldsEnabled = value
        }

    override var startDate: Long
        get() = mBinding?.startDate ?: 0
        set(value) {
            mBinding?.startDate = value
        }

    override var startTime: Long
        get() = mBinding?.startTime ?: 0
        set(value) {
            mBinding?.startTime = value
        }

    override var timeZone: String? = null
        set(value) {
            mBinding?.timeZone = value
            field = value
        }

    private val topicListObserver = Observer<List<DiscussionTopic>> {
            t -> topicListRecyclerAdapter?.dataSet = t
    }


    override var topicList: DoorMutableLiveData<List<DiscussionTopic>>? = null
        set(value) {
            field?.removeObserver(topicListObserver)
            field = value
            value?.observe(this, topicListObserver)
        }

}