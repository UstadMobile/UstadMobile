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

                container = containerDao.findByUidAsync(containerUid)!!
                val result = containerEntryDao.findByContainerAsync(containerUid)
                containerManager = ContainerManager(container, db, repo)
                var defaultLangName = ""
                for (entry in result) {

                    val fileInContainer = entry.cePath
                    val containerEntryFile = entry.containerEntryFile

                    if (fileInContainer != null && containerEntryFile != null) {
                        if (fileInContainer.endsWith(".mp4") || fileInContainer.endsWith(".webm")) {
                            videoPath = containerEntryFile.cefPath
                        } else if (fileInContainer == "audio.c2") {
                            audioEntry = entry
                            audioInput = containerManager.getInputStream(entry)
                        } else if (fileInContainer == "subtitle.srt" || fileInContainer.toLowerCase() == "subtitle-english.srt") {

                            defaultLangName = if (fileInContainer.contains("-"))
                                fileInContainer.substring(fileInContainer.indexOf("-") + 1, fileInContainer.lastIndexOf("."))
                            else "English"
                            srtMap[defaultLangName] = fileInContainer
                        } else {
                            val name = fileInContainer.substring(fileInContainer.indexOf("-") + 1, fileInContainer.lastIndexOf("."))
                            srtMap[name] = fileInContainer
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

                if (videoPath.isNullOrEmpty() && result.isNotEmpty()) {
                    videoPath = result[0].containerEntryFile?.cefPath
                }

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