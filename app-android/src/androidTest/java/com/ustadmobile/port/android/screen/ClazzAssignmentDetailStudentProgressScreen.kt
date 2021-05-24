package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.image.KImageView
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzAssignmentDetailStudentProgressFragment
import org.hamcrest.Matcher

object ClazzAssignmentDetailStudentProgressScreen : KScreen<ClazzAssignmentDetailStudentProgressScreen>() {


    override val layoutId: Int?
        get() = R.layout.fragment_list
    override val viewClass: Class<*>?
        get() = ClazzAssignmentDetailStudentProgressFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_list_recyclerview)
    }, itemTypeBuilder = {
        itemType(::Content)
        itemType(::TotalScore)
        itemType(::PrivateComments)
        itemType(::NewPrivateComments)
    })

    class Content(parent: Matcher<View>) : KRecyclerItem<Content>(parent) {
        val contentTitle: KTextView = KTextView(parent) { withId(R.id.content_entry_item_title) }
        val score: KTextView = KTextView(parent) {withId(R.id.item_person_progress)}
        val scoreResults: KTextView = KTextView(parent) {withId(R.id.item_person_score_results)}
    }

    class TotalScore(parent: Matcher<View>) : KRecyclerItem<TotalScore>(parent) {
        val score: KTextView = KTextView(parent) {withId(R.id.item_person_score)}
        val scoreResults: KTextView = KTextView(parent) {withId(R.id.item_person_total_score_results)}
    }

    class PrivateComments(parent: Matcher<View>) : KRecyclerItem<PrivateComments>(parent) {
        val commenterName: KTextView = KTextView(parent) {withId(R.id.item_comments_list_text)}
        val commenterComment: KTextView = KTextView(parent) {withId(R.id.item_comments_list_line2_text)}
    }

    class NewPrivateComments(parent: Matcher<View>) : KRecyclerItem<NewPrivateComments>(parent) {
        val newComment: KEditText = KEditText(parent) {withId(R.id.item_comment_new_comment_et)}
    }

}