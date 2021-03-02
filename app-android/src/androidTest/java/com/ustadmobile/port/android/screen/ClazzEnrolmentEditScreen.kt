package com.ustadmobile.port.android.screen

import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ClazzEnrolmentEditFragment

object ClazzEnrolmentEditScreen: KScreen<ClazzEnrolmentEditScreen>() {

    override val layoutId: Int?
        get() = R.layout.fragment_clazz_enrolment
    override val viewClass: Class<*>?
        get() = ClazzEnrolmentEditFragment::class.java

    val roleInputLayout = KTextInputLayout { withId(R.id.clazz_enrolment_edit_role_textinputlayout) }

    val startDateLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_fromDate_textinputlayout)}

    val endDateLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_toDate_textinputlayout)}

    val statusLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_outcome_textinputlayout)}

    val leavingReasonLayout = KTextInputLayout {withId(R.id.clazz_enrolment_edit_reason_textinputlayout)}

}