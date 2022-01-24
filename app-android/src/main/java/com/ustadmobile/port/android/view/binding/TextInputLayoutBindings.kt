package com.ustadmobile.port.android.view.binding

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.ustadmobile.core.util.ext.systemImpl
import com.ustadmobile.lib.db.entities.CustomField

@BindingAdapter("hintMessageId")
fun TextInputLayout.setHintMessageId(messageId: Int) {
    hint = systemImpl.getString(messageId, context)
}

@BindingAdapter("customFieldHint")
fun TextInputLayout.setCustomFieldHint(customField: CustomField?) {
    hint = if (customField != null) {
        systemImpl.getString(customField.customFieldLabelMessageID, context)
    } else {
        ""
    }
}

@BindingAdapter("errorText")
fun TextInputLayout.setErrorText(errorMessage: String?) {
    this.error = errorMessage
}