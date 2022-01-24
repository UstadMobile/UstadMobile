package com.ustadmobile.port.android.screen

import android.view.View
import androidx.test.espresso.matcher.ViewMatchers.withId
import io.github.kakaocup.kakao.common.views.KView
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.image.KImageView
import io.github.kakaocup.kakao.recycler.KRecyclerItem
import io.github.kakaocup.kakao.recycler.KRecyclerView
import io.github.kakaocup.kakao.text.KTextView
import com.kaspersky.kaspresso.screens.KScreen
import com.toughra.ustadmobile.R
import com.ustadmobile.port.android.view.ReportFilterEditFragment
import org.hamcrest.Matcher

object ReportFilterEditScreen: KScreen<ReportFilterEditScreen>() {

    override val layoutId: Int
        get() = R.layout.fragment_report_filter_edit
    override val viewClass: Class<*>
        get() = ReportFilterEditFragment::class.java

    val fieldTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_field_text)}

    val conditionTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_condition_textinputlayout)}

    val conditionTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_condition_text)}

    val valuesDropDownTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_values_dropdown_textinputlayout)}

    val valueDropDownTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_values_text)}

    val valueIntegerTextInputLayout = KTextInputLayout { withId(R.id.fragment_report_filter_edit_dialog_values_number_textinputlayout)}

    val valueNumberTextValue = KTextView { withId(R.id.fragment_report_filter_edit_dialog_values_number_text)}

    val uidAndLabelRecycler : KRecyclerView = KRecyclerView({
        withId(R.id.item_filter_rv)
    }, itemTypeBuilder = {
        itemType(::UidAndLabel)
    })

    class UidAndLabel(parent: Matcher<View>) : KRecyclerItem<UidAndLabel>(parent) {
        val labelName = KTextView(parent) { withId(R.id.item_clazz_simple_line1_text)}
        val deleteButton = KImageView(parent) { withId(R.id.item_clazz_simple_secondary_menu_imageview)}
    }

}