package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

actual fun requestDelete(contentEntryUid: Long, context: Any) {
    GlobalScope.async {
        deleteDownloadJob(UmAppDatabase.Companion.getInstance(context), contentEntryUid) {

        }
    }
}