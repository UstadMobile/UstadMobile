package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportListFragment
import org.hamcrest.Matcher

object ReportListScreen : KScreen<ReportListScreen>() {
    override val layoutId: Int?
        get() =  R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ReportListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::Report)
    })

    class Report(parent: Matcher<View>) : KRecyclerItem<Report>(parent) {
        val reportLayout = KView(parent) { withId(R.id.item_reportlist_report_cl) }
        val reportTitle = KTextView { withId(R.id.report_title)}
    }

}