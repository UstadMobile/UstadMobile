package com.ustadmobile.port.android.screen

import android.view.View
import com.agoda.kakao.common.views.KView
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzWorkWithSubmission
import com.ustadmobile.port.android.view.ClazzWorkSubmissionMarkingFragment
import org.hamcrest.Matcher

object ClazzWorkMarkingFragmentScreen : KScreen<ClazzWorkMarkingFragmentScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_work_submission_marking
    override val viewClass: Class<*>?
        get() = ClazzWorkSubmissionMarkingFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_clazz_work_submission_marking_rv)
    }, itemTypeBuilder = {
        itemType(::SimpleHeading)
        itemType(::ContentEntryList)
        itemType(::Submission)
        itemType(::QuestionSet)
        itemType(::SubmitSubmission)
        itemType(::Comments)
        itemType(::SubmitWithMetrics)
    })

    class SimpleHeading(parent: Matcher<View>) : KRecyclerItem<SimpleHeading>(parent) {
        val headingTitleTextView: KTextView = KTextView(parent) { withId(R.id.item_simpl_heading_heading_tv) }
    }


    class ContentEntryList(parent: Matcher<View>) : KRecyclerItem<ContentEntryList>(parent) {
        val entryTitle: KTextView = KTextView(parent) { withId(R.id.content_entry_item_title) }
    }

    class Submission(parent: Matcher<View>) : KRecyclerItem<Submission>(parent) {
        val submissionEditText: KEditText = KEditText(parent) { withId(R.id.item_clazzwork_submission_score_edit_et) }
    }

    class QuestionSet(parent: Matcher<View>) : KRecyclerItem<QuestionSet>(parent) {
        val questionTitle: KTextView = KTextView(parent) { withId(R.id.item_clazzworkquestionandoptionswithresponse_title_tv) }
        val answerEditText: KEditText = KEditText(parent) { withId(R.id.item_clazzworkquestionandoptionswithresponse_answer_et) }
        val radioOptions: KView = KView(parent) { withId(R.id.activity_role_assignment_detail_radio_options) }
    }

    class SubmitSubmission(parent: Matcher<View>) : KRecyclerItem<SubmitSubmission>(parent) {
        val submitButton: KButton = KButton(parent) { withId(R.id.item_simpl_button_button_tv) }
    }

    class Comments(parent: Matcher<View>) : KRecyclerItem<Comments>(parent) {
        val commentTextView: KTextView = KTextView(parent) { withId(R.id.item_comments_list_line2_text) }
    }

    class SubmitComment(parent: Matcher<View>) : KRecyclerItem<SubmitComment>(parent) {
        val newCommentEditText: KEditText = KEditText(parent) { withId(R.id.item_comment_new_comment_et) }
        val submitCommentButton = KButton(parent) { withId(R.id.item_comment_new_send_ib)}
    }


    class SubmitWithMetrics(parent: Matcher<View>) : KRecyclerItem<SubmitWithMetrics>(parent) {
        val markingButton: KButton = KButton(parent) { withId(R.id.item_clazzworksubmission_marking_button_with_extra_button) }
    }

}