package com.ustadmobile.port.android.view.util

import android.text.Editable
import android.text.TextWatcher

class RunAfterTextChangedTextWatcher(val block: () -> Unit) : TextWatcher {

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun afterTextChanged(s: Editable?) {
        block()
    }
}