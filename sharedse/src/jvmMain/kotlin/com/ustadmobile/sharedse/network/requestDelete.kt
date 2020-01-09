package com.ustadmobile.sharedse.network

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

actual fun requestDelete(downloadJobUid: Int, containerDownloadManager: ContainerDownloadManager,
                         context: Any) {
    GlobalScope.async {
        deleteDownloadJob(UmAccountManager.getActiveDatabase(context), downloadJobUid,
                containerDownloadManager) {

        }
    }
}