package com.ustadmobile.port.android.view

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.toughra.ustadmobile.R
import com.toughra.ustadmobile.databinding.ItemChatListItemBinding
import com.ustadmobile.core.controller.ChatListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.impl.UMAndroidUtil
import com.ustadmobile.core.util.ext.toStringMap
import com.ustadmobile.core.view.ChatListView
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.ChatWithLatestMessageAndCount
import com.ustadmobile.port.android.view.ext.setSelectedIfInList
import com.ustadmobile.port.android.view.util.ListHeaderRecyclerViewAdapter
import com.ustadmobile.port.android.view.util.SelectablePagedListAdapter

class ChatListFragment : UstadListViewFragment<Chat, ChatWithLatestMessageAndCount>(),
        ChatListView, View.OnClickListener,
        BottomSheetOptionSelectedListener{

    private var mPresenter: ChatListPresenter? = null

    override val listPresenter: UstadListPresenter<*, in ChatWithLatestMessageAndCount>?
        get() = mPresenter


    class ChatListViewHolder(val itemBinding: ItemChatListItemBinding)
        : RecyclerView.ViewHolder(itemBinding.root)

    class ChatListRecyclerAdapter(var presenter: ChatListPresenter?)
        : SelectablePagedListAdapter<ChatWithLatestMessageAndCount,
            ChatListViewHolder>(DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
            val itemBinding = ItemChatListItemBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false)
            itemBinding.presenter = presenter
            return ChatListViewHolder(itemBinding)
        }

        override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
            val item = getItem(position)
            holder.itemBinding.chat = item
            holder.itemView.setSelectedIfInList(item, selectedItems, DIFF_CALLBACK)
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            presenter = null
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        mPresenter = ChatListPresenter(requireContext(), UMAndroidUtil.bundleToMap(arguments),
                this, di, viewLifecycleOwner).withViewLifecycle()

        mDataRecyclerViewAdapter = ChatListRecyclerAdapter(mPresenter)
        mUstadListHeaderRecyclerViewAdapter = ListHeaderRecyclerViewAdapter(this,
                requireContext().getString(R.string.new_chat))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fabManager?.text = requireContext().getText(R.string.chat)
        //override this to show our own bottom sheet
        fabManager?.onClickListener = {
            val optionList =
                listOf(
                    BottomSheetOption(R.drawable.ic_add_black_24dp,
                        requireContext().getString(R.string.new_chat), NEW_CHAT)
                )

            val sheet = OptionsBottomSheetFragment(optionList, this)
            sheet.show(childFragmentManager, sheet.tag)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_search).isVisible = true
    }

    override fun onResume() {
        super.onResume()
        mActivityWithFab?.activityFloatingActionButton?.text =
                requireContext().getString(R.string.chat)
    }

    /**
     * OnClick function that will handle when the user clicks to create a new item
     */
    override fun onClick(v: View?) {
        if(v?.id == R.id.item_createnew_layout) {
           mPresenter?.handleClickAddNewItem()
        }else{
            super.onClick(v)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter = null
        dbRepo = null
        mDataRecyclerViewAdapter = null
        mDataBinding = null
    }

    override val displayTypeRepo: Any?
        get() = dbRepo?.chatDao

    override fun onBottomSheetOptionSelected(optionSelected: BottomSheetOption) {
        when(optionSelected.optionCode) {
            NEW_CHAT -> mPresenter?.handleClickCreateNewFab(
                ChatListPresenter.CHAT_RESULT_KEY)
        }
    }

    companion object {
        val DIFF_CALLBACK: DiffUtil.ItemCallback<ChatWithLatestMessageAndCount> = object
            : DiffUtil.ItemCallback<ChatWithLatestMessageAndCount>() {
            override fun areItemsTheSame(oldItem: ChatWithLatestMessageAndCount,
                                         newItem: ChatWithLatestMessageAndCount): Boolean {

                return oldItem.chatUid == newItem.chatUid
            }

            override fun areContentsTheSame(oldItem: ChatWithLatestMessageAndCount,
                                            newItem: ChatWithLatestMessageAndCount): Boolean {
                return oldItem.otherPersonUid == newItem.otherPersonUid &&
                        oldItem.unreadMessageCount == newItem.unreadMessageCount &&
                        oldItem.latestMessageTimestamp == newItem.latestMessageTimestamp &&
                        oldItem.latestMessage == newItem.latestMessage &&
                        oldItem.otherPersonFirstNames == newItem.otherPersonFirstNames &&
                        oldItem.otherPersonLastName == newItem.otherPersonLastName
            }
        }


        const val NEW_CHAT = 2

    }


}