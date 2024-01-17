package com.ustadmobile.core.contentformats

import com.ustadmobile.lib.db.entities.ContentEntryImportJob

fun interface ContentImportProgressListener {

    fun onProgress(contentJobItem: ContentEntryImportJob)
}