package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

actual class VideoContentPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                      view: VideoContentView, di: DI)
    : VideoContentPresenterCommon(context, arguments, view, di) {

    var container: Container? = null

    private val systemImpl: UstadMobileSystemImpl by instance()

    actual override fun handleOnResume() {
        GlobalScope.launch {

            if (view.videoParams == null) {

                val containerResult = db.containerDao.findByUidAsync(containerUid)
                if (containerResult == null) {
                    view.showSnackBar(systemImpl.getString(MessageID.no_video_file_found, context), {}, 0)
                    view.loading = false
                    return@launch
                }
                container = containerResult
                val result = db.containerEntryDao.findByContainerAsync(containerUid)
                var defaultLangName = ""
                for (entry in result) {

                    val containerEntryPath = entry.cePath
                    val containerEntryFile = entry.containerEntryFile

                    if (containerEntryPath != null && containerEntryFile != null) {
                        if (VIDEO_EXT_LIST.any { containerEntryPath.toLowerCase().endsWith(it) }) {
                            videoPath = containerEntryFile.cefPath
                        } else if (containerEntryPath == "audio.c2") {
                            audioEntry = entry
                        } else if (containerEntryPath == "subtitle.srt" || containerEntryPath.toLowerCase() == "subtitle-english.srt") {

                            defaultLangName = if (containerEntryPath.contains("-"))
                                containerEntryPath.substring(containerEntryPath.indexOf("-") + 1, containerEntryPath.lastIndexOf("."))
                            else "English"
                            srtMap[defaultLangName] = containerEntryPath
                        } else if (containerEntryPath.endsWith(".srt") && containerEntryPath.contains("-") && containerEntryPath.contains(".")) {
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

                srtLangList.add(0, systemImpl.getString(MessageID.no_subtitle, context))
                if (defaultLangName.isNotEmpty()) srtLangList.add(1, defaultLangName)

            }

            view.runOnUiThread(kotlinx.coroutines.Runnable {
                view.videoParams = VideoParams(videoPath, audioEntry, srtLangList, srtMap)
            })

        }

    }

}