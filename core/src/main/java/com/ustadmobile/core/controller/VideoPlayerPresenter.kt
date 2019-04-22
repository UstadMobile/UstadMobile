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
import com.ustadmobile.lib.db.entities.Container
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
    var srtMap: HashMap<String, String>? = null
        private set
    var srtLangList: ArrayList<String>? = null
        private set
    var container: Container? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val db = UmAppDatabase.getInstance(context)
        val dbRepo = UmAccountManager.getRepositoryForActiveAccount(context)
        contentEntryDao = dbRepo.contentEntryDao
        val containerEntryDao = db.containerEntryDao
        val containerDao = dbRepo.containerDao;

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

        containerDao.findByUid(containerUid, object : UmCallback<Container> {
            override fun onSuccess(result: Container?) {

                container = result
                containerEntryDao.findByContainer(containerUid, object : UmCallback<List<ContainerEntryWithContainerEntryFile>> {
                    override fun onSuccess(result: List<ContainerEntryWithContainerEntryFile>?) {

                        srtMap = HashMap()
                        srtLangList = ArrayList()
                        var defaultLangName = ""
                        for (entry in result!!) {


                            val fileInContainer = entry.cePath
                            if (fileInContainer.endsWith(".mp4") || fileInContainer.endsWith(".webm")) {
                                videoPath = entry.containerEntryFile.cefPath
                            } else if (fileInContainer == "audio.c2") {
                                audioPath = entry.containerEntryFile.cefPath
                            } else if (fileInContainer == "subtitle.srt" || fileInContainer.toLowerCase() == "subtitle-english.srt") {
                                defaultLangName = if (fileInContainer.contains("-"))
                                    fileInContainer.substring(fileInContainer.indexOf("-") + 1, fileInContainer.lastIndexOf("."))
                                else "English"
                                srtMap!![defaultLangName] = entry.cePath;
                            } else {
                                val name = fileInContainer.substring(fileInContainer.indexOf("-") + 1, fileInContainer.lastIndexOf("."))
                                srtMap!![name] = entry.cePath;
                                srtLangList!!.add(name)
                            }
                        }

                        srtLangList!!.sortedWith(Comparator { a, b ->
                            when {
                                a > b -> 1
                                a < b -> -1
                                else -> 0
                            }
                        })


                        srtLangList!!.add(0, "No Subtitles")
                        srtLangList!!.add(1, defaultLangName)

                        view.runOnUiThread(Runnable { view.setVideoParams(videoPath!!, audioPath!!, srtLangList!!, srtMap!!) })
                    }

                    override fun onFailure(exception: Throwable?) {

                    }
                })
            }

            override fun onFailure(exception: Throwable?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
