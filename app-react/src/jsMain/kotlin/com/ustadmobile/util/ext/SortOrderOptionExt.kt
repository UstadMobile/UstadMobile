package com.ustadmobile.util.ext

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.locale.StringProviderJs
import com.ustadmobile.core.util.SortOrderOption

fun SortOrderOption.description(
    strings: StringProviderJs
): String {
    return buildString {
        append(strings[fieldMessageId])
        order?.also { orderVal ->
            val orderLabel = if(orderVal) {
                strings[MR.strings.ascending]
            }else {
                strings[MR.strings.descending]
            }
            append(" ($orderLabel)")
        }
    }
}
