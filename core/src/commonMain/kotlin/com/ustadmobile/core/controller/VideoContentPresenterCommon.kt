package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.xapi.endpoints.XapiStatementEndpoint
import com.ustadmobile.core.contentformats.xapi.endpoints.storeProgressStatement
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.VideoContentView
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on

abstract class VideoContentPresenterCommon(context: Any, arguments: Map<String, String>, view: VideoContentView,
                                           di: DI)
    : UstadBaseController<VideoContentView>(context, arguments, view, di) {


    private var entry: ContentEntry? = null
    private var entryUuid: Long = 0
    internal var containerUid: Long = 0
    var clazzUid: Long = 0L

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    val statementEndpoint by on(accountManager.activeAccount).instance<XapiStatementEndpoint>()

    var timeVideoPlayed = 0L

    lateinit var contextRegistration: String

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
        contextRegistration = randomUuid().toString()

        entryUuid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID).toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID).toLong()
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

        view.loading = true
        GlobalScope.launch(doorMainDispatcher()) {
            entry = db.contentEntryDao.findByUidAsync(entryUuid)
            view.entry = entry
        }

    }

    override fun onResume() {
        super.onResume()
        handleOnResume()
    }

    fun updateProgress(position: Long, videoLength: Long, playerStarted: Boolean = false) {

        if(accountManager.activeAccount.personUid == 0L){
            return
        }


        var playerPlayedVideoDuration = 0L
        if(playerStarted){
            // player pressed play, record start time
            timeVideoPlayed = systemTimeInMillis()
        }else if(timeVideoPlayed == 0L){
            // video never started, dont send statement
            return
        }else if(!playerStarted && timeVideoPlayed > 0){
            // player pressed paused or video ended, so calc duration
            playerPlayedVideoDuration = systemTimeInMillis() - timeVideoPlayed
            timeVideoPlayed = 0
        }else {
            // unhandled cases
            return
        }

        GlobalScope.launch{
            val progress = (position.toFloat() / videoLength * 100).toInt()
            entry?.also {
                statementEndpoint.storeProgressStatement(
                        accountManager.activeAccount, it, progress,
                        playerPlayedVideoDuration,contextRegistration, clazzUid)
            }
        }
    }

    companion object {

        val VIDEO_EXT_LIST = listOf(".mp4", ".mkv", ".webm", ".m4v")

        var VIDEO_MIME_MAP = mapOf("video/mp4" to ".mp4",
            "video/x-matroska" to ".mkv",
            "video/webm" to ".webm",
            "video/x-m4v" to ".m4v")
    }
}
