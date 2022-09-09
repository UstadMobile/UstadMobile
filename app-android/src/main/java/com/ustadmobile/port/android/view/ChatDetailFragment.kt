package com.ustadmobile.port.android.view

import android.content.Context
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import com.toughra.ustadmobile.databinding.FragmentChatDetailBinding
import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.controller.ChatDetailPresenter
import com.ustadmobile.core.controller.NewCommentItemListener
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.toNullableStringMap
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ChatDetailView
import com.ustadmobile.core.view.EditButtonMode
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.ext.asRepositoryLiveData
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.MessageWithPerson
import com.ustadmobile.port.android.view.ext.observeIfFragmentViewIsReady
import com.ustadmobile.port.android.view.util.ViewNameListFragmentPagerAdapter
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on


interface ChatDetailFragmentEventHandler {

}

class ChatDetailFragment: UstadBaseFragment(), ChatDetailView, ChatDetailFragmentEventHandler,
    NewCommentItemListener, Observer<PagedList<MessageWithPerson>> {

    private var mBinding: FragmentChatDetailBinding? = null

    private var mPresenter: ChatDetailPresenter? = null

    private var mPagerAdapter: ViewNameListFragmentPagerAdapter? = null

    private var dbRepo: UmAppDatabase? = null

    private var messagesRecyclerAdapter: MessagesRecyclerAdapter? = null

    private var messageListObserver = Observer<PagedList<MessageWithPerson>?>{
        t -> messagesRecyclerAdapter?.submitList(t)
    }

    private var messageListLiveData: LiveData<PagedList<MessageWithPerson>>? = null

    val accountManager: UstadAccountManager by instance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
    : View {
        val rootView: View

        dbRepo = on(accountManager.activeAccount).direct.instance(tag = DoorTag.TAG_REPO)

        mPresenter = ChatDetailPresenter(
            requireContext(), arguments.toStringMap(), this,
            di
        )


        messagesRecyclerAdapter = MessagesRecyclerAdapter(accountManager.activeAccount.personUid,
            mPresenter?.ps, mPresenter, di, requireContext())

        val stackedLayoutManager = LinearLayoutManager(requireContext())
        stackedLayoutManager.reverseLayout = true

        mBinding = FragmentChatDetailBinding.inflate(inflater, container, false).also {
            rootView = it.root
            it.listener = this
            it.fragmentChatDetailMessages.apply {
                adapter = messagesRecyclerAdapter
                layoutManager = stackedLayoutManager

            }
            it.fragmentChatDetailMessageEt.movementMethod = LinkMovementMethod.getInstance()
        }
        
        return rootView
    }

    private fun dpToPx(context: Context?, value: Int): Float {
        val metrics = context?.resources?.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), metrics)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mPresenter?.onCreate(savedInstanceState.toNullableStringMap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mBinding?.fragmentChatDetailMessages?.adapter = null
        mPagerAdapter = null
        mBinding = null
        mPresenter = null
        entity = null
        messagesRecyclerAdapter = null
    }

    override var title: String?
        get() = ustadFragmentTitle
        set(value) {
            ustadFragmentTitle= value

        }


    override fun onChanged(t: PagedList<MessageWithPerson>?) {
        messagesRecyclerAdapter?.submitList(t)
    }

    override var messageList: DataSource.Factory<Int, MessageWithPerson>? = null
        set(value) {
            messageListLiveData?.removeObserver(messageListObserver)
            field = value
            val dvRepoVal = dbRepo?: return
            messageListLiveData = value?.asRepositoryLiveData(dvRepoVal.messageDao)
            messageListLiveData?.observeIfFragmentViewIsReady(this, messageListObserver)

        }

    override var editButtonMode: EditButtonMode
        get() = EditButtonMode.GONE
        set(value) {}

    override var entity: Chat? = null
        set(value) {
            field = value
            ustadFragmentTitle = value?.chatTitle
            mBinding?.chat = value
        }

    override fun addComment(text: String) {
        mBinding?.fragmentChatDetailMessageEt?.text?.clear()
        mPresenter?.addMessage(text)
    }


}