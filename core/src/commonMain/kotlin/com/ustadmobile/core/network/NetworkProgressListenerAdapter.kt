package com.ustadmobile.core.network

import com.ustadmobile.core.contentjob.ContentJobProgressListener
import com.ustadmobile.lib.db.entities.ContentJobItem

class NetworkProgressListenerAdapter(
    private val contentJobProgressListener: ContentJobProgressListener,
    private val contentJobItem: ContentJobItem,
    private val progressBase: Long = contentJobItem.cjiItemProgress
): NetworkProgressListener {

    override fun onProgress(bytesCompleted: Long, totalBytes: Long) {
        contentJobItem.cjiItemProgress = progressBase + bytesCompleted
        contentJobItem.cjiItemTotal = progressBase + totalBytes
        contentJobProgressListener.onProgress(contentJobItem)
    }

}