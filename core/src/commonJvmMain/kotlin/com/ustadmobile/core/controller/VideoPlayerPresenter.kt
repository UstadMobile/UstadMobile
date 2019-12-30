package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.VideoPlayerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual class VideoPlayerPresenter actual constructor(context: Any, arguments: Map<String, String>?,
                                                     view: VideoPlayerView, private val db: UmAppDatabase,
                                                     private val repo: UmAppDatabase)
    : VideoPlayerPresenterCommon(context, arguments, view, db, repo) {

    actual override fun handleOnResume() {
        GlobalScope.launch {

            if (videoParams == null) {

                var container =  containerDao.findByUidAsync(containerUid)
                if(container == null){
                    view.showErrorWithAction(UstadMobileSystemImpl.instance.getString(MessageID.no_video_file_found, context), 0)
                    return@launch
                }

                val result = containerEntryDao.findByContainerAsync(containerUid)
                containerManager = ContainerManager(container, db, repo)
                var defaultLangName = ""
                for (entry in result) {

                    val containerEntryPath = entry.cePath
                    val containerEntryFile = entry.containerEntryFile

                    if (containerEntryPath != null && containerEntryFile != null) {
                        if (VIDEO_EXT_LIST.contains(containerEntryPath)) {
                            videoPath = containerEntryFile.cefPath
                        } else if (containerEntryPath == "audio.c2") {
                            audioEntry = entry
                            audioInput = containerManager.getInputStream(entry)
                        } else if (containerEntryPath == "subtitle.srt" || containerEntryPath.toLowerCase() == "subtitle-english.srt") {

                            defaultLangName = if (containerEntryPath.contains("-"))
                                containerEntryPath.substring(containerEntryPath.indexOf("-") + 1, containerEntryPath.lastIndexOf("."))
                            else "English"
                            srtMap[defaultLangName] = containerEntryPath
                        } else if(containerEntryPath.endsWith(".srt") && containerEntryPath.contains("-") && containerEntryPath.contains(".")){
                            val name = containerEntryPath.substring(containerEntryPath.indexOf("-") + 1, containerEntryPath.lastIndexOf("."))
                            srtMap[name] = containerEntryPath
                            srtLangList.add(name)
                        }
                    }
                }

                srtLangList.sortedWith(Comparator { a, b ->
                    when {
                        a > b -> 1
                        a < b -> -1
                        else -> 0
                    }
                })

                srtLangList.add(0, UstadMobileSystemImpl.instance.getString(MessageID.no_subtitle, context))
                if (defaultLangName.isNotEmpty()) srtLangList.add(1, defaultLangName)

            }

            view.runOnUiThread(kotlinx.coroutines.Runnable {
                videoParams = VideoParams(videoPath, audioEntry, srtLangList, srtMap)
                view.setVideoParams(videoPath, audioInput, srtLangList, srtMap)
            })

        }

    }

}