package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KButton
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportDetailFragment

object ReportDetailScreen : KScreen<ReportDetailScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_report_detail
    override val viewClass: Class<*>?
        get() = ReportDetailFragment::class.java

    val reportList: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_detail_report_list)
    }, itemTypeBuilder = {

    })

    val addToListButton = KButton { withId(R.id.preview_add_to_dashboard_button)}

}