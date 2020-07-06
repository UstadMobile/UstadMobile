package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: com.ustadmobile.core.view.WebChunkView, isDownloadEnabled: Boolean, appRepo: UmAppDatabase, umAppDb: UmAppDatabase)
    : WebChunkPresenterCommon(context, arguments, view, isDownloadEnabled, appRepo, umAppDb) {

    actual override suspend fun handleMountChunk() {
        val result = umAppDb.containerDao.findByUidAsync(containerUid ?: 0L)
        if (result == null) {
            view.showSnackBar(UstadMobileSystemImpl.instance
                    .getString(MessageID.error_opening_file, this))
            return
        }
        view.containerManager = ContainerManager(result, umAppDb, appRepo)
    }
}