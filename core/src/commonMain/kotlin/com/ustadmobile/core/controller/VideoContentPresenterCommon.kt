package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ContainerDao
import com.ustadmobile.core.db.dao.ContainerEntryDao
import com.ustadmobile.core.db.dao.ContentEntryDao
import com.ustadmobile.core.generated.locale.MessageID.loading
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.io.InputStream

abstract class VideoContentPresenterCommon(context: Any, arguments: Map<String, String>?, view: VideoPlayerView,
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

    var audioEntry: ContainerEntryWithContainerEntryFile? = null

    internal var videoPath: String? = null
    internal var srtMap = mutableMapOf<String, String>()
    internal var srtLangList = mutableListOf<String>()

    abstract fun handleOnResume()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        containerEntryDao = db.containerEntryDao
        containerDao = db.containerDao
        contentEntryDao = db.contentEntryDao

        navigation = arguments[ARG_REFERRER] ?: ""
        val entryUuid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID).toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID).toLong()

        view.loading = true
        GlobalScope.launch {
            view.entry = contentEntryDao.getContentByUuidAsync(entryUuid)
        }

    }

    override fun onResume() {
        super.onResume()
        handleOnResume()
    }

    fun handleUpNavigation() {
        //This is now handled by jetpack navigation
    }


    companion object {

        val VIDEO_EXT_LIST = listOf(".mp4", ".mkv", ".webm", ".m4v")

        var VIDEO_MIME_MAP = mapOf("video/mp4" to ".mp4", "video/x-matroska" to ".mkv", "video/webm" to ".webm", "video/x-m4v" to ".m4v")
    }
}
