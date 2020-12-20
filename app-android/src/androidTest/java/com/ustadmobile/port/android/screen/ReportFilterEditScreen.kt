package com.ustadmobile.port.android.screen

import androidx.test.espresso.matcher.ViewMatchers.withId
import com.agoda.kakao.edit.KTextInputLayout
import com.agoda.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportFilterEditFragment

object ReportFilterEditScreen: KScreen<ReportFilterEditScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_report_filter_edit
    override val viewClass: Class<*>
        get() = ReportFilterEditFragment::class.java

    val fieldTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_field_text)}

    val conditionTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_condition_textinputlayout)}

    val conditionTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_condition_text)}

    val valuesDropDownTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_values_dropdown_textinputlayout)}

    val valueDropDownTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_values_number_text)}

    val valueIntegerTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_values_number_textinputlayout)}


}