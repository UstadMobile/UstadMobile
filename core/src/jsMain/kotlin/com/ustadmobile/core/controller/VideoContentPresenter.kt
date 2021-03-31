package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import io.ktor.client.request.get
import kotlinx.browser.localStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

actual class VideoContentPresenter actual constructor(context: Any, arguments: Map<String, String>, view: VideoPlayerView,
                                                      di: DI)
    : VideoContentPresenterCommon(context, arguments, view, di) {

    actual override fun handleOnResume() {

        GlobalScope.launch {
            val baseMountUrl = localStorage.getItem("contentUrl")!!
            val client = defaultHttpClient()
            val videoContent = client.get<String>(UMFileUtil.joinPaths(baseMountUrl,"/VideoParams/","$containerUid"))
            val params: dynamic = JSON.parse<VideoParams>(videoContent)
            val videoPath = UMFileUtil.joinPaths(baseMountUrl, "$containerUid",params.videoPath!!)
            var audioPath: ContainerEntryWithContainerEntryFile? = null
            if(params.audioPath?.ceUid != 0L){
                audioPath = ContainerEntryWithContainerEntryFile(params.audioPath?.cePath)
            }
            view.videoParams = VideoParams(videoPath,
                audioPath, params.srtLangList, params.srtMap)
        }
    }

}