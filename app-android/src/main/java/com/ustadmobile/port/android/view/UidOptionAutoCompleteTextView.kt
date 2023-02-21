package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.core.util.UidOption

class UidOptionAutoCompleteTextView : DropDownListAutoCompleteTextView<UidOption> {

    private val uidOptionAdapter = object: DropDownListAutoCompleteAdapter<UidOption> {
        override fun getId(item: UidOption) = item.uidOption
        override fun getText(item: UidOption): String {
            return item.toString()
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
        dropDownListAdapter = uidOptionAdapter
    }
}