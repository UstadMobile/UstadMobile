package com.ustadmobile.core.util

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus

actual class ContentEntryUtil: ContentEntryUtilCommon() {

    actual override suspend fun goToViewIfDownloaded(entryStatus: ContentEntryWithContentEntryStatus,
                                                     dbRepo: UmAppDatabase, impl: UstadMobileSystemImpl,
                                                     openEntryIfNotDownloaded: Boolean, context: Any,
                                                     callback: UmCallback<Any>) {
        this.dbRepo = dbRepo; this.impl = impl; this.context = context; this.callback = callback
        val result = dbRepo.containerDao.getMostRecentContainerForContentEntryAsync(entryStatus.contentEntryUid)
                ?: throw IllegalArgumentException("No file found")
        handleFoundContainer(result)
        impl.go(viewName, args, context)
        callback.onSuccess(Any())

    }

    actual companion object {
        /**
         * Get an instance of the system implementation
         */
        actual var instance: ContentEntryUtil = ContentEntryUtil()
    }

}