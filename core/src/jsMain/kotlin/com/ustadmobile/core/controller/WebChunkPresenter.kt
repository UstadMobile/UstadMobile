package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
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
        view.containerManager = ContainerManager(result, db, repo)
       /* view.mountChunk(result, object : UmCallback<String>{
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

        })*/

    }
}