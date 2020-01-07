package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.WebChunkView

expect class WebChunkPresenter(context: Any, arguments: Map<String, String>, view: WebChunkView,
                               isDownloadEnabled: Boolean, appRepo: UmAppDatabase, umAppDb: UmAppDatabase): WebChunkPresenterCommon {

    override suspend fun handleMountChunk()

}