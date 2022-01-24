package com.ustadmobile.port.android.screen

import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzEnrolmentEditFragment

object ClazzEnrolmentEditScreen: KScreen<ClazzEnrolmentEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_enrolment
    override val viewClass: Class<*>?
        get() = ClazzEnrolmentEditFragment::class.java

    val roleInputLayout = KTextInputLayout { withId(R.id.clazz_enrolment_edit_role_textinputlayout) }

    val roleTextView = KTextView { withId(R.id.clazz_enrolment_edit_role_text)}

    val startDateLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_fromDate_textinputlayout)}

    val endDateLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_toDate_textinputlayout)}

    val outcomeLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_outcome_textinputlayout)}

    val outcomeText = KTextView { withId(R.id.clazz_enrolment_edit_outcome_text)}

    val leavingReasonLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_reason_textinputlayout)}

}