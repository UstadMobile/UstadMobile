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
import com.toughra.ustadmobile.databinding.FragmentDiscussionPostDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.DiscussionPostDetailPresenter
import com.ustadmobile.core.controller.NewCommentItemListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.DiscussionPostDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.DoorDataSourceFactory
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.DiscussionPostWithDetails
import com.ustadmobile.lib.db.entities.MessageWithPerson
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on

class DiscussionPostDetailFragment: UstadBaseFragment(), DiscussionPostDetailView,
    NewCommentItemListener, Observer<PagedList<MessageWithPerson>> {

    private var mBinding: FragmentDiscussionPostDetailBinding? = null

    private var mPresenter: DiscussionPostDetailPresenter? = null

    private var dbRepo: UmAppDatabase? = null

    private var messagesRecyclerAdapter: MessagesRecyclerAdapter? = null

    private var messageListObserver = Observer<PagedList<MessageWithPerson>?>{
        t -> messagesRecyclerAdapter?.submitList(t)
    }

    private var messageListLiveData: LiveData<PagedList<MessageWithPerson>>? = null

    val accountManager: UstadAccountManager by instance()

    private var descriptionRecyclerAdapter: DiscussionPostDescriptionRecyclerAdapter? = null

    private var newReplyRecyclerAdapter: NewMessageSendRecyclerViewAdapter? = null

    private var detailMergerRecyclerView: RecyclerView? = null
    private var detailMergerRecyclerAdapter: ConcatAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View? {
        val rootView: View

        dbRepo = on(accountManager.activeAccount).direct.instance(tag = UmAppDatabase.TAG_REPO)

        //1.
        descriptionRecyclerAdapter = DiscussionPostDescriptionRecyclerAdapter(di, requireContext())

        mPresenter = DiscussionPostDetailPresenter(
            requireContext(),
            arguments.toStringMap(),
            this,
            di
        )


        val stackedLayoutManager = LinearLayoutManager(requireContext())
        stackedLayoutManager.reverseLayout = true

        //2.
        newReplyRecyclerAdapter  = NewMessageSendRecyclerViewAdapter(this,
            requireContext().getString(R.string.add_a_reply)).apply{
            visible = true
        }

        //3.
        messagesRecyclerAdapter = MessagesRecyclerAdapter(accountManager.activeAccount.personUid,
            mPresenter?.ps, mPresenter, di, requireContext())



        mBinding = FragmentDiscussionPostDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
        }

        detailMergerRecyclerView = rootView.findViewById(R.id.fragment_discussion_post_detail_rv)

        detailMergerRecyclerAdapter = ConcatAdapter(
            descriptionRecyclerAdapter,
            newReplyRecyclerAdapter,
            messagesRecyclerAdapter)

        detailMergerRecyclerView?.adapter = detailMergerRecyclerAdapter
        detailMergerRecyclerView?.layoutManager = LinearLayoutManager(requireContext())
        
        return rootView
    }



    override var entity: DiscussionPostWithDetails? = null
        set(value) {
            field = value
            descriptionRecyclerAdapter?.discussionTopic = value
            ustadFragmentTitle = value?.discussionPostTitle
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentDiscussionPostDetailRv?.adapter = null
        mBinding = null
        mPresenter = null
        entity = null
        messagesRecyclerAdapter = null
        descriptionRecyclerAdapter = null
        detailMergerRecyclerView?.adapter = null
        detailMergerRecyclerView = null

    }

    override var title: String?
        get() = ustadFragmentTitle
        set(value) {
            ustadFragmentTitle= value

        }

    override var replies: DoorDataSourceFactory<Int, MessageWithPerson>? = null
        set(value) {
            messageListLiveData?.removeObserver(messageListObserver)
            field = value
            val messageDao = dbRepo?.messageDao ?:return
            messageListLiveData = value?.asRepositoryLiveData(messageDao)
            messageListLiveData?.observe(this, messageListObserver)
        }

    override var editButtonMode: EditButtonMode
        get() = EditButtonMode.GONE
        set(value) {}


    override fun onChanged(t: PagedList<MessageWithPerson>?) {
        messagesRecyclerAdapter?.submitList(t)
    }

    override fun addComment(text: String) {
        newReplyRecyclerAdapter?.clearComment()
        mPresenter?.addMessage(text)

    }


}