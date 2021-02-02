package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportTemplateListFragment
import org.hamcrest.Matcher

object ReportTemplateScreen : KScreen<ReportTemplateScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ReportTemplateListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::ReportTemplate)
    })


    class ReportTemplate(parent: Matcher<View>) : KRecyclerItem<ReportTemplate>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.report_template_title) }
    }

}