package com.ustadmobile.port.android.screen

import com.agoda.kakao.edit.KTextInputLayout
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportFilterEditFragment

object ReportFilterEditScreen: KScreen<ReportFilterEditScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_report_filter_edit
    override val viewClass: Class<*>
        get() = ReportFilterEditFragment::class.java

    val fieldTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_field_textinputlayout)}

    val conditionTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_condition_textinputlayout)}

    val valuesDropDownTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_values_dropdown_textinputlayout)}


}