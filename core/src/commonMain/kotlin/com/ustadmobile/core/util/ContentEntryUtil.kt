package com.ustadmobile.core.util

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlin.jvm.JvmStatic

expect class ContentEntryUtil : ContentEntryUtilCommon{

    override suspend fun goToViewIfDownloaded(entryUid: Long?, sourceUrl: String?,
                                              dbRepo: UmAppDatabase, impl: UstadMobileSystemImpl,
                                              openEntryIfNotDownloaded: Boolean, context: Any,
                                              callback: UmCallback<Any>)

    companion object{
        /**
         * Get an instance of the system implementation
         */
        @JvmStatic
        var instance: ContentEntryUtil
    }
}