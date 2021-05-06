package com.ustadmobile.port.android.view.binding

import android.widget.EditText
import android.widget.ImageButton
import androidx.databinding.BindingAdapter
import com.ustadmobile.core.controller.NewCommentItemListener
import com.ustadmobile.port.android.view.CommentsBottomSheet

@BindingAdapter("buttonListener", "buttonSheet","editText")
fun ImageButton.setEditButtonClick(listener: NewCommentItemListener,
                                   sheet: CommentsBottomSheet, text: String?) {
    setOnClickListener {
        if(text.isNullOrEmpty()){
            return@setOnClickListener
        }
        listener.addComment(text)
        sheet.dismiss()
    }

}