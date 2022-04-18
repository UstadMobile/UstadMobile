package com.ustadmobile.port.android.view

import android.content.Context
import android.util.AttributeSet
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.CustomFieldValueOption
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

class CustomFieldAutoCompleteTextView : DropDownListAutoCompleteTextView<CustomFieldValueOption> {

    private val di: DI by closestDI()

    private val customFieldValueOptionAdapter = object: DropDownListAutoCompleteAdapter<CustomFieldValueOption> {
        override fun getId(item: CustomFieldValueOption) = item.customFieldValueOptionUid
        override fun getText(item: CustomFieldValueOption): String {
            return if(item.customFieldValueOptionMessageId != 0) {
                val systemImpl: UstadMobileSystemImpl = di.direct.instance()
                systemImpl.getString(item.customFieldValueOptionMessageId, context)
            }else {
                item.customFieldValueOptionName ?: "ERR: unnamed option"
            }
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
        dropDownListAdapter = customFieldValueOptionAdapter
    }
}