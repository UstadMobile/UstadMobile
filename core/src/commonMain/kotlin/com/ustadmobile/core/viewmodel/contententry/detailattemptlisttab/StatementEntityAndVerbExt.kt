package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

import com.ustadmobile.lib.db.composites.xapi.StatementEntityAndVerb

val StatementEntityAndVerb.verbDisplayName: String
    get() {
        return verbDisplay?.vlmeEntryString ?: verb?.verbUrlId?.substringAfterLast("/") ?: ""
    }
