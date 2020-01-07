package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.WebChunkView
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual class WebChunkPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                  view: WebChunkView, isDownloadEnabled: Boolean,
                                                  appRepo: UmAppDatabase,
                                                  umAppDb: UmAppDatabase)
    : WebChunkPresenterCommon(context, arguments, view, isDownloadEnabled, appRepo, umAppDb) {

    actual override suspend fun handleMountChunk() {
        val result = umAppDb.containerDao.findByUidAsync(containerUid!!)
        view.mountChunk(result, object : UmCallback<String>{
            override fun onSuccess(result: String?) {

                GlobalScope.launch {
                    val client = defaultHttpClient()
                    val indexContent = client.get<String>(UMFileUtil.joinPaths(result!!,"index.json"))
                    val indexLog:dynamic = JSON.parse<IndexLog>(indexContent)
                    val urlToLoad = indexLog.entries[0].url
                    view.loadUrl(urlToLoad)
                }
            }

            override fun onFailure(exception: Throwable?) {
                view.runOnUiThread(kotlinx.coroutines.Runnable { view.showError(UstadMobileSystemImpl.instance.getString(MessageID.error_opening_file, context)) })
            }

        })

    }
}