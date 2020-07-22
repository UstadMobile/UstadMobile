package com.ustadmobile.sharedse.network

import com.ustadmobile.core.networkmanager.downloadmanager.ContainerDownloadManager

actual fun requestDelete(downloadJobUid: Int, containerDownloadManager: ContainerDownloadManager,
                         context: Any) {
    //TODO: Fix this to use endpoints
//    GlobalScope.async {
//        deleteDownloadJob(UmAccountManager.getActiveDatabase(context), downloadJobUid,
//                containerDownloadManager) {
//
//        }
//    }
}