package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.core.util.MessageIdOption

class MessageIdAutoCompleteTextView: DropDownListAutoCompleteTextView<MessageIdOption> {

    private val messageIdDropdownAdapter = object: DropDownListAutoCompleteAdapter<MessageIdOption> {
        override fun getId(item: MessageIdOption) = item.code.toLong()
        override fun getText(item: MessageIdOption) = item.messageStr
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