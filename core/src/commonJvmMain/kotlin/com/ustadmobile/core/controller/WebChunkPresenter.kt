package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: com.ustadmobile.core.view.WebChunkView, isDownloadEnabled: Boolean, appRepo: UmAppDatabase, umAppDb: UmAppDatabase)
    : WebChunkPresenterCommon(context, arguments, view, isDownloadEnabled, appRepo, umAppDb) {

    actual override suspend fun handleMountChunk() {
        val result = umAppDb.containerDao.findByUidAsync(containerUid!!)
        view.mountChunk(result, object : UmCallback<String> {
            override fun onSuccess(result: String?) {
                if (result != null) {
                    view.loadUrl(result)
                } else {
                    view.runOnUiThread(kotlinx.coroutines.Runnable { view.showError(UstadMobileSystemImpl.instance.getString(MessageID.error_opening_file, context)) })
                }
            }

            override fun onFailure(exception: Throwable?) {
                view.runOnUiThread(kotlinx.coroutines.Runnable { view.showError(UstadMobileSystemImpl.instance.getString(MessageID.error_opening_file, context)) })
            }
        })
    }
}