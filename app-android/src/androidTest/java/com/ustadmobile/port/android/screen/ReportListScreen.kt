package com.ustadmobile.port.android.screen

import com.agoda.kakao.recycler.KRecyclerView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportListFragment

object ReportListScreen : KScreen<ReportListScreen>() {
    override val layoutId: Int?
        get() =  R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ReportListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {

    })

}