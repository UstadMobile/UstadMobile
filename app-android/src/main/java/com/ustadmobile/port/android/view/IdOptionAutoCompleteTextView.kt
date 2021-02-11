package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.core.util.IdOption

class IdOptionAutoCompleteTextView: DropDownListAutoCompleteTextView<IdOption> {

    private val idOptionDropdownAdapter = object: DropDownListAutoCompleteAdapter<IdOption> {
        override fun getId(item: IdOption) = item.optionId.toLong()
        override fun getText(item: IdOption) = item.toString()
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
        dropDownListAdapter = idOptionDropdownAdapter
    }

}