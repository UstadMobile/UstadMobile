package com.ustadmobile.core.util.ext

import com.ustadmobile.lib.db.composites.xapi.VerbEntityAndName

fun VerbEntityAndName.displayName(): String {
    return verbName?.vlmeEntryString ?: verbEntity.verbUrlId?.substringAfterLast("/") ?: ""
}
