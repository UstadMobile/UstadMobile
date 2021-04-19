package com.ustadmobile.core.util.graph

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl

class MessageIdFormatter(val map: Map<String, Int>,
                         val systemImpl: UstadMobileSystemImpl, val context: Any) : LabelValueFormatter {

    override fun format(option: Any): String {
        return systemImpl.getString(map[(option as String)] ?: MessageID.unset, context)
    }

    override fun formatAsList(option: List<Any>): List<String> {
        return option.map { format(it) }
    }

}