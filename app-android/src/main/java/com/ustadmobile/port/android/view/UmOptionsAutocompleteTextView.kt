package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.port.android.impl.UmDropDownOption

class UmOptionsAutocompleteTextView: DropDownListAutoCompleteTextView<UmDropDownOption> {

    private val messageIdDropdownAdapter = object: DropDownListAutoCompleteAdapter<UmDropDownOption> {
        override fun getId(item: UmDropDownOption): Long {return  0}

        override fun getText(item: UmDropDownOption): String {
            return item.label
        }

    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        dropDownListAdapter = messageIdDropdownAdapter
    }

}