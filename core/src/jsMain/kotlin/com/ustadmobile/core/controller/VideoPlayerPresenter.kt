package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.networkmanager.defaultHttpClient
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.VideoPlayerView
import io.ktor.client.request.get
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.browser.localStorage

actual class VideoPlayerPresenter actual constructor(context: Any, arguments: Map<String, String>?,
                                                     view: VideoPlayerView, db: UmAppDatabase, repo: UmAppDatabase)
    : VideoPlayerPresenterCommon(context, arguments, view, db, repo) {

    actual override fun handleOnResume() {

        GlobalScope.launch {
            val baseMountUrl = localStorage.getItem("contentUrl")!!
            val client = defaultHttpClient()
            val videoContent = client.get<String>(UMFileUtil.joinPaths(baseMountUrl,"/VideoParams/","$containerUid"))
            val params: dynamic = JSON.parse<VideoParams>(videoContent)
            val videoPath = UMFileUtil.joinPaths(baseMountUrl, "$containerUid",params.videoPath!!)
            var audioPath = ""
            if(params.audioPath?.ceUid != 0L){
                audioPath = UMFileUtil.joinPaths(baseMountUrl, "$containerUid",params.audioPath?.cePath!!)
            }
            view.setVideoParams(videoPath, audioPath,params.srtLangList,params.srtMap)
        }
    }

}