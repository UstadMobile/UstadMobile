package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.Chat
import com.ustadmobile.lib.db.entities.ChatWithLatestMessageAndCount

interface ChatListView: UstadListView<Chat, ChatWithLatestMessageAndCount> {

    companion object {
        const val VIEW_NAME = "ChatListView"
    }

}