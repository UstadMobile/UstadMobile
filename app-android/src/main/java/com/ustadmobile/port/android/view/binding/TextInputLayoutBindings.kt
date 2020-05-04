package com.ustadmobile.port.android.view.binding

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CustomField

@BindingAdapter("hintMessageId")
fun TextInputLayout.setHintMessageId(messageId: Int) {
    hint = UstadMobileSystemImpl.instance.getString(messageId, context)
}

@BindingAdapter("customFieldHint")
fun TextInputLayout.setCustomFieldHint(customField: CustomField?) {
    hint = if(customField != null) {
        UstadMobileSystemImpl.instance.getString(customField.customFieldLabelMessageID, context)
    }else {
        ""
    }
}