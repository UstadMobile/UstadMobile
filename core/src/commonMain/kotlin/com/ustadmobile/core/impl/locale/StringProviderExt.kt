package com.ustadmobile.core.impl.locale

import dev.icerock.moko.resources.StringResource

fun StringProvider.mapLookup(
    key: Int,
    map: Map<Int, StringResource>,
    fallback: (Int) -> String = { "" },
) : String {
    return map[key]?.let { get(it) } ?: fallback(key)
}
