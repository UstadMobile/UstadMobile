package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.LeavingReasonListFragment
import org.hamcrest.Matcher

object LeavingReasonListScreen : KScreen<LeavingReasonListScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = LeavingReasonListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::LeavingReason)
        itemType(::SortOption)
    })


    val sortList: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_sort_order_list)
    }, itemTypeBuilder = {
        itemType(::Sort)
    })


    class LeavingReason(parent: Matcher<View>) : KRecyclerItem<LeavingReason>(parent) {
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