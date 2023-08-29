package com.ustadmobile.port.android.view.binding

import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout
import com.ustadmobile.core.util.ext.systemImpl
import com.ustadmobile.lib.db.entities.CustomField


@BindingAdapter("errorText")
fun TextInputLayout.setErrorText(errorMessage: String?) {
    this.error = errorMessage
}