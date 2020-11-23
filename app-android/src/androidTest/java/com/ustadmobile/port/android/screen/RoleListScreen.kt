package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import org.hamcrest.Matcher

object RoleListScreen : KScreen<RoleListScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = RoleListScreen::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::MainItem)
    })

    class MainItem(parent: Matcher<View>) : KRecyclerItem<MainItem>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.item_role_text) }
    }


}