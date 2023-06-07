package com.ustadmobile.core.util.ext

import com.ustadmobile.core.impl.locale.StringsXml

fun StringsXml.mapLookupOrBlank(key: Int, map: Map<Int, Int>): String{
    return map[key]?.let { get(it) } ?: ""
}
