package com.ustadmobile.port.android.screen

import android.view.View
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzAssignmentDetailOverviewFragment
import io.github.kakaocup.kakao.edit.KEditText
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import org.hamcrest.Matcher

object ClazzAssignmentDetailOverviewScreen : KScreen<ClazzAssignmentDetailOverviewScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_assignment_detail_overview
    override val viewClass: Class<*>?
        get() = ClazzAssignmentDetailOverviewFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_clazz_assignment_detail_overview)
    }, itemTypeBuilder = {
        itemType(::AssignmentDetail)
        itemType(::ClassComments)
        itemType(::PrivateComments)
        itemType(::NewClassComments)
        itemType(::NewPrivateComments)
    })

    class AssignmentDetail(parent: Matcher<View>) : KRecyclerItem<AssignmentDetail>(parent) {
        val desc: KTextView = KTextView(parent) { withId(R.id.item_ca_detail_description) }
        val deadline: KTextView = KTextView(parent) {withId(R.id.item_ca_detail_description_deadline_date)}
    }

    class ClassComments(parent: Matcher<View>) : KRecyclerItem<ClassComments>(parent) {
        val commenterName: KTextView = KTextView(parent) {withId(R.id.item_comments_list_text)}
        val commenterComment: KTextView = KTextView(parent) {withId(R.id.item_comments_list_line2_text)}
    }

    class PrivateComments(parent: Matcher<View>) : KRecyclerItem<PrivateComments>(parent) {
        val commenterName: KTextView = KTextView(parent) {withId(R.id.item_comments_list_text)}
        val commenterComment: KTextView = KTextView(parent) {withId(R.id.item_comments_list_line2_text)}
    }

    class NewClassComments(parent: Matcher<View>) : KRecyclerItem<NewClassComments>(parent) {
        val newComment: KEditText = KEditText(parent) {withId(R.id.item_comment_new_comment_et)}
    }

    class NewPrivateComments(parent: Matcher<View>) : KRecyclerItem<NewPrivateComments>(parent) {
        val newComment: KEditText = KEditText(parent) {withId(R.id.item_comment_new_comment_et)}
    }



}