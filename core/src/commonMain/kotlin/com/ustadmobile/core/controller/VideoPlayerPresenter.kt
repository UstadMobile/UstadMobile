package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.core.view.VideoPlayerView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.VideoPlayerView.Companion.ARG_CONTENT_ENTRY_ID
import com.ustadmobile.lib.db.entities.Container
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlinx.io.InputStream

class VideoPlayerPresenter(context: Any, arguments: Map<String, String>?, view: VideoPlayerView)
    : UstadBaseController<VideoPlayerView>(context, arguments!!, view) {

    private lateinit var contentEntryDao: ContentEntryDao

    private var navigation: String? = null

    var audioInput: InputStream? = null
        private set
    var videoPath: String? = null
        private set
    var srtMap = mutableMapOf<String, String>()
        private set
    var srtLangList = mutableListOf<String>()
        private set
    var container: Container? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val db = UmAppDatabase.getInstance(context)
        val dbRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        contentEntryDao = dbRepo.contentEntryDao
        val containerEntryDao = db.containerEntryDao
        val containerDao = db.containerDao

        navigation = arguments[ARG_REFERRER] ?: ""
        val entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_ID)!!.toLong()
        val containerUid = arguments.getValue(ARG_CONTAINER_UID)!!.toLong()


        GlobalScope.launch(Dispatchers.Main) {
            val contentEntry = contentEntryDao.getContentByUuidAsync(entryUuid)
            if (contentEntry != null)
                view.setVideoInfo(contentEntry)
        }

        GlobalScope.launch {
            val result = contentEntryDao.getContentByUuidAsync(entryUuid)
            view.setVideoInfo(result!!)
        }

        GlobalScope.launch {
            container = containerDao.findByUidAsync(containerUid)
            val result = containerEntryDao.findByContainerAsync(containerUid)
            val containerManager = ContainerManager(container!!, db, dbRepo)
            var defaultLangName = ""
            for (entry in result) {

                val fileInContainer = entry.cePath
                val containerEntryFile = entry.containerEntryFile

                if (fileInContainer != null && containerEntryFile != null) {
                    if (fileInContainer.endsWith(".mp4") || fileInContainer.endsWith(".webm")) {
                        videoPath = containerEntryFile.cefPath
                    } else if (fileInContainer == "audio.c2") {
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

            srtLangList.add(0, "No Subtitles")
            srtLangList.add(1, defaultLangName)

            view.runOnUiThread(Runnable { view.setVideoParams(videoPath, audioInput, srtLangList, srtMap) })
        }

    }

    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation!!)
        if (lastEntryListArgs !=
                null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(HomeView.VIEW_NAME, mapOf(), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        }

    }
}
