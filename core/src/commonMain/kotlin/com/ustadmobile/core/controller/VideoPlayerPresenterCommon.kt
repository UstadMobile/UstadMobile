package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContentEntryDao
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
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.InputStream

abstract class VideoPlayerPresenterCommon(context: Any, arguments: Map<String, String>?, view: VideoPlayerView,
                                 private val db: UmAppDatabase, private val repo: UmAppDatabase)
    : UstadBaseController<VideoPlayerView>(context, arguments!!, view) {


    internal var containerUid: Long = 0
    private var navigation: String? = null

    internal lateinit var contentEntryDao: ContentEntryDao
    internal lateinit var containerDao: ContainerDao
    internal lateinit var containerEntryDao: ContainerEntryDao

    data class VideoParams(val videoPath: String? = null,
                           val audioPath: ContainerEntryWithContainerEntryFile? = null,
                           val srtLangList: MutableList<String> = mutableListOf(),
                           val srtMap: MutableMap<String, String> = mutableMapOf())

    var videoParams: VideoParams? = null

    var audioEntry: ContainerEntryWithContainerEntryFile? = null

    var audioInput: InputStream? = null
        get() {
            val audioEntryVal = audioEntry
            return if (audioEntryVal != null) {
                containerManager.getInputStream(audioEntryVal)
            } else {
                null
            }
        }

    internal var videoPath: String? = null
    internal var srtMap = mutableMapOf<String, String>()
    internal var srtLangList = mutableListOf<String>()

    lateinit var container: Container

    lateinit var containerManager: ContainerManager

    abstract  fun handleOnResume()

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        containerEntryDao = db.containerEntryDao
        containerDao = db.containerDao
        contentEntryDao = db.contentEntryDao

        navigation = arguments[ARG_REFERRER] ?: ""
        val entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_ID)!!.toLong()
        containerUid = arguments.getValue(ARG_CONTAINER_UID)!!.toLong()

        GlobalScope.launch {
            val contentEntry = contentEntryDao.getContentByUuidAsync(entryUuid)
            if (contentEntry != null)
                view.setVideoInfo(contentEntry)
        }

    }

    override fun onResume() {
        super.onResume()
        handleOnResume()
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
