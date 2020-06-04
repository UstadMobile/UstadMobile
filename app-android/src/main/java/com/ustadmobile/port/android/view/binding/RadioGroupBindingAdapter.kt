package com.ustadmobile.port.android.view.binding

import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption

@BindingAdapter("radioGroupOptions", "radioGroupOptionsResponse")
fun RadioGroup.setRadioButtonOptions(options: List<ClazzWorkQuestionOption>, response: Long) {

    removeAllViews()
    for (item in options) {
        val radioButton = RadioButton(context)
        radioButton.setText(item.clazzWorkQuestionOptionText)
        radioButton.isSelected =
                response == item.clazzWorkQuestionOptionUid
        radioButton.setTag(R.id.tag_clazzwork_quiz_option_uid, item.clazzWorkQuestionOptionUid)
        radioButton.isEnabled = response == 0L
        radioButton.isChecked = response == item.clazzWorkQuestionOptionUid
        addView(radioButton)

    }
}

@BindingAdapter("radioGroupOptionsResponseAttrChanged")
fun RadioGroup.setResponseUpdateListener(inverseBindingListener: InverseBindingListener) {
    setOnCheckedChangeListener { radioButtonGroup: RadioGroup, i: Int ->

        val radioButton: RadioButton? = radioButtonGroup.findViewById<RadioButton>(i)
        if(radioButton != null) {
            val optionUid: Long = radioButton.getTag(R.id.tag_clazzwork_quiz_option_uid) as Long
            radioButtonGroup.setTag(R.id.tag_clazzwork_quiz_question_selected_uid, optionUid)
            inverseBindingListener.onChange()
        }
    }
}

@InverseBindingAdapter(attribute = "radioGroupOptionsResponse")
fun RadioGroup.getResponseUpdateValue(): Long {
    return getTag(R.id.tag_clazzwork_quiz_question_selected_uid) as Long
}