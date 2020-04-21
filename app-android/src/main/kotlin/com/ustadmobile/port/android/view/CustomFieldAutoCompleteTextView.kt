package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.lib.db.entities.CustomFieldValueOption

class CustomFieldAutoCompleteTextView : DropDownListAutoCompleteTextView<CustomFieldValueOption> {

    private val customFieldValueOptionAdapter = object: DropDownListAutoCompleteAdapter<CustomFieldValueOption> {
        override fun getId(item: CustomFieldValueOption) = item.customFieldValueOptionUid
        override fun getText(item: CustomFieldValueOption) = item.customFieldValueOptionName ?: ""
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
        dropDownListAdapter = customFieldValueOptionAdapter
    }
}