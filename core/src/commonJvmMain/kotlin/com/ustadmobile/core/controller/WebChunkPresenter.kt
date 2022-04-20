package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView
import org.kodein.di.DI
import org.kodein.di.instance

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: WebChunkView, di: DI)
    : WebChunkPresenterCommon(context, arguments, view, di) {

    private val systemImpl: UstadMobileSystemImpl by instance()

    actual override suspend fun handleMountChunk() {
        val result = repo.containerDao.findByUidAsync(containerUid ?: 0L)
        if (result == null) {
            view.showSnackBar(
                systemImpl.getString(MessageID.error_opening_file, this)
            )
            return
        }
    }
}