package com.ustadmobile.core.util.graph

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import dev.icerock.moko.resources.StringResource

class MessageIdFormatter(
    val map: Map<String, StringResource>,
    val systemImpl: UstadMobileSystemImpl,
    val context: Any
) : LabelValueFormatter {

    override fun format(option: Any): String {
        return systemImpl.getString(map[(option as String)] ?: MR.strings.unset)
    }

    override fun formatAsList(option: List<Any>): List<String> {
        return option.map { format(it) }
    }

}