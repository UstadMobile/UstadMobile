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
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.CourseDiscussionDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.CourseDiscussion
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
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

    private var postsHeadingRecyclerAdapter: SimpleHeadingRecyclerAdapter? = null

    private var postsLiveData: LiveData<PagedList<DiscussionPostWithDetails>>? = null


    private var repo: UmAppDatabase? = null


    private var postsRecyclerAdapter: DiscussionPostRecyclerAdapter? = null

    private val postsObserver = Observer<PagedList<DiscussionPostWithDetails>?> {
            t -> postsRecyclerAdapter?.submitList(t)
    }



    override var posts: DataSourceFactory<Int, DiscussionPostWithDetails>? = null
        set(value) {

            postsLiveData?.removeObserver(postsObserver)
            field = value
            val postsDao = repo?.discussionPostDao ?:return
            postsLiveData = value?.asRepositoryLiveData(postsDao)
            postsLiveData?.observe(this, postsObserver)
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
        postsHeadingRecyclerAdapter = SimpleHeadingRecyclerAdapter(getString(R.string.posts))
        postsHeadingRecyclerAdapter?.visible = true


        mBinding?.fragmentCourseDiscussionDetailEfab?.setOnClickListener{
            mPresenter?.onClickAddPost()
        }


        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
        mPresenter = CourseDiscussionDetailPresenter(requireContext(), arguments.toStringMap(), this,
                 di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        // 3
        postsRecyclerAdapter = DiscussionPostRecyclerAdapter(mPresenter)


        detailMergerRecyclerAdapter = ConcatAdapter(descriptionRecyclerAdapter,
            postsHeadingRecyclerAdapter, postsRecyclerAdapter)

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
        postsHeadingRecyclerAdapter = null
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