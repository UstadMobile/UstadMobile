package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.WebChunkView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: WebChunkView,isDownloadEnabled: Boolean, private val appRepo: UmAppDatabase)
    : WebChunkPresenterCommon(context, arguments, view, isDownloadEnabled, appRepo) {

    actual override suspend fun handleMountChunk() {
        val result = appRepo.containerDao.findByUidAsync(containerUid!!)
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