package com.ustadmobile.port.android.screen

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzLogEditAttendanceFragment
import com.ustadmobile.port.android.view.ClazzLogListAttendanceFragment
import com.ustadmobile.port.android.view.ContentEntryList2Fragment
import org.hamcrest.Matcher

object ClazzLogListAttendanceScreen : KScreen<ClazzLogListAttendanceScreen>() {
    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ClazzLogListAttendanceFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::MainItem)
    })

    class MainItem(parent: Matcher<View>) : KRecyclerItem<MainItem>(parent) {
        val chartGraph = KView(parent) { withId(R.id.chart) }
    }


}