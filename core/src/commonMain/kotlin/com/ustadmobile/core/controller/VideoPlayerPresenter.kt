package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.ARG_REFERRER
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.core.view.VideoPlayerView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.VideoPlayerView.Companion.ARG_CONTENT_ENTRY_ID
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry

class VideoPlayerPresenter(context: Any, arguments: Map<String, String>?, view: VideoPlayerView)
    : UstadBaseController<VideoPlayerView>(context, arguments!!, view) {

    private var contentEntryDao: ContentEntryDao? = null
    private var navigation: String? = null
    var audioPath: String? = null
        private set
    var srtPath: String? = null
        private set
    var videoPath: String? = null
        private set

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val db = UmAppDatabase.getInstance(context)
        val dbRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        contentEntryDao = dbRepo.contentEntryDao
        val containerEntryDao = db.containerEntryDao

        navigation = arguments[ARG_REFERRER] ?: ""
        val entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_ID)!!.toLong()
        val containerUid = arguments.getValue(ARG_CONTAINER_UID)!!.toLong()

        contentEntryDao!!.getContentByUuid(entryUuid, object : UmCallback<ContentEntry> {
            override fun onSuccess(result: ContentEntry?) {
                view.setVideoInfo(result!!)
            }

            override fun onFailure(exception: Throwable?) {

            }
        })


        containerEntryDao.findByContainer(containerUid, object : UmCallback<List<ContainerEntryWithContainerEntryFile>> {
            override fun onSuccess(result: List<ContainerEntryWithContainerEntryFile>?) {

                for (entry in result!!) {

                    val fileInContainer = entry.cePath
                    val containerEntryFile = entry.containerEntryFile
                    if(fileInContainer != null && containerEntryFile != null) {
                        if (fileInContainer.endsWith(".mp4") || fileInContainer.endsWith(".webm")) {
                            videoPath = containerEntryFile.cefPath
                        } else if (fileInContainer == "audio.c2") {
                            audioPath = containerEntryFile.cefPath
                        } else if (fileInContainer == "subtitle.srt") {
                            srtPath = containerEntryFile.cefPath
                        }
                    }
                }

                view.runOnUiThread(Runnable { view.setVideoParams(videoPath!!, audioPath!!, srtPath!!) })
            }

            override fun onFailure(exception: Throwable?) {

            }
        })


    }

    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation!!)
        if (lastEntryListArgs !=
                null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.context,
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(DummyView.VIEW_NAME, mapOf(), view.context,
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP)
        }

    }
}
