package com.ustadmobile.core.viewmodel.contententry.detailoverviewtab

import com.ustadmobile.lib.db.composites.ContentEntryImportJobProgress

val ContentEntryImportJobProgress.progress: Float
    get() = if(cjiItemTotal > 0) {
        cjiItemProgress.toFloat() / cjiItemTotal.toFloat()
    }else {
        0f
    }
