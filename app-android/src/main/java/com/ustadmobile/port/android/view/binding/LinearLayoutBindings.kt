package com.ustadmobile.port.android.view.binding

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.toughra.ustadmobile.R
import com.ustadmobile.lib.db.entities.ClazzWorkQuestionOption

@BindingAdapter("questionOptions", "questionOptionsResponse")
fun LinearLayout.setQuestionOptionsWithResponse(options: List<ClazzWorkQuestionOption>, response: Long) {

    removeAllViews()
    for (item in options) {
        val option = TextView(context)
        option.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        option.text = item.clazzWorkQuestionOptionText
        option.setTag(R.id.tag_clazzwork_quiz_option_uid, item.clazzWorkQuestionOptionUid)
        option.setTextAppearance(context, R.style.UmTheme_TextAppearance_Subtitle1)
        option.visibility = View.VISIBLE
        option.setPadding(8,8,8,8)
        option.background = context.getDrawable(R.drawable.bg_selected_quiz)
        if(response == item.clazzWorkQuestionOptionUid){
            option.isSelected = true
            option.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor))
        }


        addView(option)

    }
}