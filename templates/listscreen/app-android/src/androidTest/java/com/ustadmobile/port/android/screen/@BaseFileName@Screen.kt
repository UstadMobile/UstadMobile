package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.@BaseFileName@Fragment
import org.hamcrest.Matcher

object @BaseFileName@Screen : KScreen<@BaseFileName@Screen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = @BaseFileName@Fragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::@Entity@)
        itemType(::SortOption)
    })


    val sortList: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_sort_order_list)
    }, itemTypeBuilder = {
        itemType(::Sort)
    })


    class @Entity@(parent: Matcher<View>) : KRecyclerItem<@Entity@>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.line1_text) }
    }

    class SortOption(parent: Matcher<View>) : KRecyclerItem<SortOption>(parent) {
        val sortLayout = KView(parent) { withId(R.id.item_sort_selected_layout) }
        val selectedSort = KTextView(parent) { withId(R.id.item_sort_selected_text)}
        val selectedOrder = KImageView(parent) { withId(R.id.item_sort_asc_desc)}
    }

    class Sort(parent: Matcher<View>) : KRecyclerItem<Sort>(parent) {
        val personName: KTextView = KTextView(parent) { withId(R.id.item_person_text) }
    }


}