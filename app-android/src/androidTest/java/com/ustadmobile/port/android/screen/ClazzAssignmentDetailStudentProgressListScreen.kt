package com.ustadmobile.port.android.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzAssignmentDetailStudentProgressListOverviewFragment
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
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
        itemType(::StudentAttempt)
    })


    class ClazzAssignmentWithMetrics(parent: Matcher<View>) : KRecyclerItem<ClazzAssignmentWithMetrics>(parent) {
        val notStartedText: KTextView = KTextView(parent) { withId(R.id.item_clazz_progress_detail_not_started_value) }
        val startedText: KTextView = KTextView(parent) { withId(R.id.item_clazz_assignment_progress_detail_started_value) }
        val marked: KTextView = KTextView(parent) { withId(R.id.item_clazz_assignment_progress_detail_marked_value)}
    }

    class StudentAttempt(parent: Matcher<View>) : KRecyclerItem<StudentAttempt>(parent) {
        val personName: KTextView = KTextView(parent) { withId(R.id.item_person_text) }
        val commentText:KTextView = KTextView(parent) { withId(R.id.attempt_private_comment) }
    }



}