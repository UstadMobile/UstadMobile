package com.ustadmobile.core.controller

import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContainerMounter
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

actual class VideoContentPresenter actual constructor(context: Any, arguments: Map<String, String>, view: VideoContentView,
                                                      di: DI)
    : VideoContentPresenterCommon(context, arguments, view, di) {

    private val httpClient: HttpClient by di.instance()

    private val mountHandler: ContainerMounter by instance()

    actual override fun handleOnResume() {
        GlobalScope.launch {
            val baseMountUrl = mountHandler.mountContainer(accountManager.activeAccount.endpointUrl,containerUid)
            val videoContent: String = httpClient.get(
                UMFileUtil.joinPaths(baseMountUrl,"/videoParams")).body()
            val params: VideoParams = JSON.parse(videoContent)
            val videoPath = UMFileUtil.joinPaths(baseMountUrl,params.videoPath?:"")
            var audioPath: ContainerEntryWithContainerEntryFile? = null
            if(params.audioPath?.ceUid != 0L){
                audioPath = ContainerEntryWithContainerEntryFile(params.audioPath?.cePath?:"")
            }
            view.videoParams = VideoParams(videoPath, audioPath, params.srtLangList, params.srtMap)
            view.loading = false
        }
    }

}