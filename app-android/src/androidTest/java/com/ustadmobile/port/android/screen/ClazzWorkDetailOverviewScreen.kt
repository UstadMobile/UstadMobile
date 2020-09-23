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
import com.ustadmobile.port.android.view.ClazzWorkDetailOverviewFragment
import org.hamcrest.Matcher

object ClazzWorkDetailOverviewScreen : KScreen<ClazzWorkDetailOverviewScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_work_with_submission_detail
    override val viewClass: Class<*>?
        get() = ClazzWorkDetailOverviewFragment::class.java

    val recycler: KRecyclerView = KRecyclerView({
        withId(R.id.fragment_clazz_work_with_submission_detail_rv)
    }, itemTypeBuilder = {
        itemType(::ClazzWorkBasicDetail)
        itemType(::SimpleHeading)
        itemType(::ContentEntryList)
        itemType(::Submission)
        itemType(::QuestionSet)
        itemType(::SubmitSubmission)
    })

    class ClazzWorkBasicDetail(parent: Matcher<View>) : KRecyclerItem<ClazzWorkBasicDetail>(parent) {
        val title: KTextView = KTextView(parent) { withId(R.id.item_clazzwork_detail_description_title) }
        val startDate: KTextView = KTextView(parent) { withId(R.id.item_clazzwork_detail_description_start_date) }
        val dueDate: KTextView = KTextView(parent) { withId(R.id.item_clazzwork_detail_description_due_date) }
    }

    class SimpleHeading(parent: Matcher<View>) : KRecyclerItem<SimpleHeading>(parent) {
        val headingTitleTextView: KTextView = KTextView(parent) { withId(R.id.item_simpl_heading_heading_tv) }
    }


    class ContentEntryList(parent: Matcher<View>) : KRecyclerItem<ContentEntryList>(parent) {
        val entryTitle: KTextView = KTextView(parent) { withId(R.id.content_entry_item_title) }
    }

    class Submission(parent: Matcher<View>) : KRecyclerItem<Submission>(parent) {
        val submissionEditText: KEditText = KEditText(parent) { withId(R.id.item_clazzwork_submission_text_entry_et) }
        val submitButton: KButton = KButton(parent) { withId(R.id.item_simpl_button_button_tv) }

    }

    class QuestionSet(parent: Matcher<View>) : KRecyclerItem<QuestionSet>(parent) {
        val questionTitle: KTextView = KTextView(parent) { withId(R.id.item_clazzworkquestionandoptionswithresponse_title_tv) }
        val answerEditText: KEditText = KEditText(parent) { withId(R.id.item_clazzworkquestionandoptionswithresponse_answer_et) }
        val radioOptions: KView = KView(parent) { withId(R.id.activity_role_assignment_detail_radio_options) }
    }

    class SubmitSubmission(parent: Matcher<View>) : KRecyclerItem<SubmitSubmission>(parent) {
        val submitButton: KButton = KButton(parent) { withId(R.id.item_simpl_button_button_tv) }
    }


}