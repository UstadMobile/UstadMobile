package com.ustadmobile.port.android.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.Chat
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ChatListScreen : KScreen<ChatListScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_list

    override val viewClass: Class<*>?
        get() = ChatListScreen::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::MainItem)
    })

    class MainItem(parent: Matcher<View>) : KRecyclerItem<MainItem>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.item_chat_list_item_chat_title) }
        val latestMessage: KTextView = KTextView(parent) { withId(R.id.item_chat_list_item_recent_message)}
        val numMessages: KTextView = KTextView(parent){ withId(R.id.item_chat_list_item_number_messages)}
        val latestMessageTime: KTextView = KTextView(parent){ withId(R.id.item_chat_list_item_recent_message_timestamp)}

    }


}