package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.util.MessageIdOption2
import dev.icerock.moko.resources.StringResource

fun StringProvider.mapLookup(
    key: Int,
    map: Map<Int, StringResource>,
    fallback: (Int) -> String = { "" },
) : String {
    return map[key]?.let { get(it) } ?: fallback(key)
}

fun StringProvider.messageIdOptionLookup(
    key: Int,
    messageIdList: List<MessageIdOption2>,
    fallback: (Int) -> String = { "" }
) : String {
    return messageIdList.firstOrNull { it.value == key }
        ?.let { get(it.stringResource) } ?: fallback(key)
}
