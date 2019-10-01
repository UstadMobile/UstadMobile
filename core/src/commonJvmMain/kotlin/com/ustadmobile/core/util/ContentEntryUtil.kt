package com.ustadmobile.core.util

import com.ustadmobile.core.controller.ContentEntryDetailPresenterCommon
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus

actual class ContentEntryUtil: ContentEntryUtilCommon() {

    actual override suspend fun goToViewIfDownloaded(entryUid: Long?, sourceUrl: String?,
                                                     dbRepo: UmAppDatabase, impl: UstadMobileSystemImpl,
                                                     openEntryIfNotDownloaded: Boolean, context: Any,
                                                     callback: UmCallback<Any>) {

        var entryStatus = ContentEntryWithContentEntryStatus()
        if(entryUid != null){
            entryStatus = dbRepo.contentEntryDao.findByUidWithContentEntryStatusAsync(entryUid)!!
        }

        if(sourceUrl != null){
            entryStatus = dbRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(sourceUrl)!!

        }
        val contentEntryStatus = entryStatus.contentEntryStatus

        this.dbRepo = dbRepo; this.impl = impl; this.context = context; this.callback = callback

        if (contentEntryStatus != null && contentEntryStatus.downloadStatus == JobStatus.COMPLETE) {

            val result = dbRepo.containerDao.getMostRecentDownloadedContainerForContentEntryAsync(entryStatus.contentEntryUid)
                    ?: throw IllegalArgumentException("No file found")
            handleFoundContainer(result)
            impl.go(viewName, args, context)
            callback.onSuccess(Any())

        } else if (openEntryIfNotDownloaded) {
            val args = HashMap<String, String>()
            args[ContentEntryDetailPresenterCommon.ARG_CONTENT_ENTRY_UID] = entryStatus.contentEntryUid.toString()
            impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
        }


    }

    actual companion object {
        /**
         * Get an instance of the system implementation
         */
        actual var instance: ContentEntryUtil = ContentEntryUtil()
    }

}