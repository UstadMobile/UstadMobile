package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.FragmentCourseDiscussionDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.CourseDiscussionDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmAppDatabase.Companion.TAG_REPO
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionTopicListDetail
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


class CourseDiscussionDetailFragment: UstadDetailFragment<CourseDiscussion>(),
    CourseDiscussionDetailView {



    private var mBinding: FragmentCourseDiscussionDetailBinding? = null

    private var mPresenter: CourseDiscussionDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null


    private var descriptionRecyclerAdapter: CourseDiscussionDescriptionRecyclerAdapter? = null

    private var topicsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null

    private var topicsLiveData: LiveData<PagedList<DiscussionTopicListDetail>>? = null


    private var repo: UmAppDatabase? = null


    private var topicsRecyclerAdapter: DiscussionTopicRecyclerAdapter? = null

    private val topicsObserver = Observer<PagedList<DiscussionTopicListDetail>?> {
            t -> topicsRecyclerAdapter?.submitList(t)
    }



    override var topics: DoorDataSourceFactory<Int, DiscussionTopicListDetail>? = null
        set(value) {

            topicsLiveData?.removeObserver(topicsObserver)
            field = value
            val topicsDao = repo?.discussionTopicDao ?:return
            topicsLiveData = value?.asRepositoryLiveData(topicsDao)
            topicsLiveData?.observe(this, topicsObserver)
        }




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View

        mBinding = FragmentCourseDiscussionDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
            it.fragmentCourseDiscussionDetailRv

        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_course_discussion_detail_rv)

        // 1
        descriptionRecyclerAdapter = CourseDiscussionDescriptionRecyclerAdapter()

        // 2
        topicsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(getString(R.string.topics))
        topicsHeadingRecyclerAdapter?.visible = true




        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = TAG_REPO)
        mPresenter = CourseDiscussionDetailPresenter(requireContext(), arguments.toStringMap(), this,
                 di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        // 3
        topicsRecyclerAdapter = DiscussionTopicRecyclerAdapter(mPresenter)


        detailMergerRecyclerAdapter = ConcatAdapter(descriptionRecyclerAdapter,
            topicsHeadingRecyclerAdapter, topicsRecyclerAdapter)

        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())



        return rootView
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
        mPresenter = null
        entity = null

        descriptionRecyclerAdapter = null
        topicsHeadingRecyclerAdapter = null
        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null



    }


    override var entity: CourseDiscussion? = null
        set(value) {
            field = value
            descriptionRecyclerAdapter?.courseDiscussion = value
            ustadFragmentTitle = value?.courseDiscussionTitle
        }



}