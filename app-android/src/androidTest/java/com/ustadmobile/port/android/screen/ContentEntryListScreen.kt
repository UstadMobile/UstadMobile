package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ContentEntryList2Fragment
import org.hamcrest.Matcher

object ContentEntryListScreen : KScreen<ContentEntryListScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ContentEntryList2Fragment::class.java

    val newBottomSheet: KView = KView { withId(R.id.bottom_content_option_sheet) }

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::MainItem)
        itemType(::NewItem)
    })

    class MainItem(parent: Matcher<View>) : KRecyclerItem<MainItem>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.content_entry_item_title) }
        val selectButton = KButton(parent) { withId(R.id.content_entry_select_btn)}
    }

    class NewItem(parent: Matcher<View>) : KRecyclerItem<NewItem>(parent){
        val newEntryItem = KView {
            withId(R.id.item_createnew_layout)
        }
        val newItemTitle = KTextView(parent) { withId(R.id.item_createnew_line1_text)}
    }


}