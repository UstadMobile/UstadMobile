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
import com.toughra.ustadmobile.databinding.FragmentDiscussionTopicDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.DiscussionTopicDetailPresenter
import com.ustadmobile.core.controller.UstadDetailPresenter
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.DiscussionTopicDetailView
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.paging.DataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.DiscussionTopic
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import com.ustadmobile.core.db.UmAppDatabase


class DiscussionTopicDetailFragment: UstadDetailFragment<DiscussionTopic>(),
    DiscussionTopicDetailView {



    private var mBinding: FragmentDiscussionTopicDetailBinding? = null

    private var mPresenter: DiscussionTopicDetailPresenter? = null

    override val detailPresenter: UstadDetailPresenter<*, *>?
        get() = mPresenter

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null


    private var descriptionRecyclerAdapter: DiscussionTopicDescriptionRecyclerAdapter? = null


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
                              savedInstanceState: Bundle?): View {
        val rootView: View

        mBinding = FragmentDiscussionTopicDetailBinding.inflate(inflater, container,
                false).also {
            rootView = it.root
        }

        detailMergerRecyclerView =
            rootView.findViewById(R.id.fragment_discussion_topic_detail_rv)

        // 1
        descriptionRecyclerAdapter = DiscussionTopicDescriptionRecyclerAdapter()



        val accountManager: UstadAccountManager by instance()
        repo = di.direct.on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)
        mPresenter = DiscussionTopicDetailPresenter(requireContext(), arguments.toStringMap(),
            this, di, viewLifecycleOwner).withViewLifecycle()
        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())

        // 2
        postsRecyclerAdapter = DiscussionPostRecyclerAdapter(mPresenter)

        mBinding?.fragmentDiscussionTopicDetailEfab?.setOnClickListener{
            mPresenter?.onClickAddPost()
        }


        detailMergerRecyclerAdapter = ConcatAdapter(descriptionRecyclerAdapter, postsRecyclerAdapter)

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
        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null


    }


    override var entity: DiscussionTopic? = null
        set(value) {
            field = value
            descriptionRecyclerAdapter?.discussionTopic = value
            ustadFragmentTitle = value?.discussionTopicTitle
        }



}