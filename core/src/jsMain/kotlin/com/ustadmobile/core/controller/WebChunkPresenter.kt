package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView
import org.kodein.di.DI

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: WebChunkView, di: DI)
    : WebChunkPresenterCommon(context, arguments, view, di) {

    actual override suspend fun handleMountChunk() {
        val result = repo.containerDao.findByUidAsync(containerUid!!)
        if (result == null) {
            view.showSnackBar(UstadMobileSystemImpl.instance
                    .getString(MessageID.error_opening_file, this))
            return
        }
        view.containerUid = containerUid ?: 0L
    }
}