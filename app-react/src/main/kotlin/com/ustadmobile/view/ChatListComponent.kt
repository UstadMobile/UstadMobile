package com.ustadmobile.view

import com.ustadmobile.core.controller.ChatListPresenter
import com.ustadmobile.core.controller.UstadListPresenter
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.view.ChatListView
import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.ChatWithLatestMessageAndCount
import com.ustadmobile.util.UmProps
import com.ustadmobile.util.ext.fromNow
import com.ustadmobile.util.ext.toDate
import com.ustadmobile.view.ext.renderChatListItemWithCounter
import react.RBuilder
import react.setState

class ChatListComponent(mProps: UmProps): UstadListComponent<Chat, ChatWithLatestMessageAndCount>(mProps) ,
    ChatListView {

    private var mPresenter: ChatListPresenter? = null

    override val displayTypeRepo: Any?
        get() = dbRepo?.chatDao

    override val listPresenter: UstadListPresenter<*, in ChatWithLatestMessageAndCount>?
        get() = mPresenter

    override fun onCreateView() {
        super.onCreateView()
        ustadComponentTitle = getString(MessageID.messages)
        showCreateNewItem = false

        fabManager?.text = getString(MessageID.chat)
        fabManager?.onClickListener = {
            setState {
                showAddEntryOptions = true
            }
        }
        mPresenter = ChatListPresenter(this, arguments,
            this, di, this)
        mPresenter?.onCreate(mapOf())
    }

    override fun RBuilder.renderAddContentOptionsDialog() {
        if(showAddEntryOptions) {
            val options = listOf(
                UmDialogOptionItem("add",
                    MessageID.new_chat) {
                    mPresenter?.handleClickCreateNewFab(ChatListPresenter.CHAT_RESULT_KEY)
                },
                UmDialogOptionItem("group",MessageID.new_group) {
                    //TODO -> Handle when logic is implemented
                }
            )

            renderDialogOptions(systemImpl,options){
                setState {
                    showAddEntryOptions = false
                }
            }
        }

    }

    override fun RBuilder.renderListItem(item: ChatWithLatestMessageAndCount) {
        renderChatListItemWithCounter(
            item.chatName, item.latestMessage,
            item.latestMessageTimestamp.toDate()?.fromNow(systemImpl.getDisplayedLocale(this)),
            item.unreadMessageCount)
    }

    override fun handleClickEntry(entry: ChatWithLatestMessageAndCount) {
        mPresenter?.handleClickEntry(entry)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mPresenter?.onDestroy()
        mPresenter = null
    }
}