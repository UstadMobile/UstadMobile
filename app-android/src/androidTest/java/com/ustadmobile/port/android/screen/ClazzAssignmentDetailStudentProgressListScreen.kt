package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzAssignmentDetailStudentProgressListOverviewFragment
import org.hamcrest.Matcher

object ClazzAssignmentDetailStudentProgressListScreen : KScreen<ClazzAssignmentDetailStudentProgressListScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ClazzAssignmentDetailStudentProgressListOverviewFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::ClazzAssignmentWithMetrics)
    })


    class ClazzAssignmentWithMetrics(parent: Matcher<View>) : KRecyclerItem<ClazzAssignmentWithMetrics>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.line1_text) }
    }


}