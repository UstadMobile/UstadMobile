package com.ustadmobile.port.android.screen

import android.view.View
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.HolidayCalendarListFragment
import org.hamcrest.Matcher

object HolidayCalendarListScreen : KScreen<HolidayCalendarListScreen>(){

    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = HolidayCalendarListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::Holiday)
    })


    class Holiday(parent: Matcher<View>) : KRecyclerItem<Holiday>(parent) {
        val holidayTitle: KTextView = KTextView(parent) { withId(R.id.item_holidaycalendar_line1_text) }
    }

}