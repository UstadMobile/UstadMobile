package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView
import org.kodein.di.DI

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: com.ustadmobile.core.view.WebChunkView, di: org.kodein.di.DI)
    : WebChunkPresenterCommon(context, arguments, view, di) {

    actual override suspend fun handleMountChunk() {
        val result = repo.containerDao.findByUidAsync(containerUid ?: 0L)
        if (result == null) {
            view.showSnackBar(UstadMobileSystemImpl.instance
                    .getString(MessageID.error_opening_file, this))
            return
        }
    }
}