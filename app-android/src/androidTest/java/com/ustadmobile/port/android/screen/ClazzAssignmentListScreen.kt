package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzAssignmentListFragment
import org.hamcrest.Matcher

object ClazzAssignmentListScreen : KScreen<ClazzAssignmentListScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ClazzAssignmentListFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::Assignment)
    })

    class Assignment(parent: Matcher<View>) : KRecyclerItem<Assignment>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.line1_title) }
        val desc: KTextView = KTextView(parent) { withId(R.id.line2_description) }
        val deadline: KTextView = KTextView(parent) {withId(R.id.line3_deadline)}
        val studentProgressText: KTextView = KTextView(parent) {withId(R.id.line3_progress_text)}
    }

}