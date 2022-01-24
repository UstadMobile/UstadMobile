package com.ustadmobile.core.contentjob

import com.ustadmobile.lib.db.entities.ContentJobItem

fun interface ContentJobProgressListener {

    fun onProgress(contentJobItem: ContentJobItem)
}