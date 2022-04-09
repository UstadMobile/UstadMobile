package com.ustadmobile.port.android.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import org.hamcrest.Matcher

object ChatDetailScreen : KScreen<ChatDetailScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_chat_detail

    override val viewClass: Class<*>?
        get() = ChatDetailScreen::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_chat_detail_messages)

    }, itemTypeBuilder = {
        itemType(::MainItem)
    })

    class MainItem(parent: Matcher<View>) : KRecyclerItem<MainItem>(parent) {
        val newMessage: KEditText = KEditText(parent){withId(R.id.fragment_chat_detail_message_et)}
        val sendButton: KButton = KButton(parent){withId(R.id.fragment_chat_detail_message_send_ib)}
    }


}