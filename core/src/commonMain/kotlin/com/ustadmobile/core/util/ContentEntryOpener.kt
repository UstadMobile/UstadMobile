package com.ustadmobile.core.util

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.controller.VideoContentPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UMLog
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEARNER_GROUP_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.DoorTag
import io.github.aakira.napier.Napier
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import org.kodein.di.on

private val mimeTypeToViewNameMap = mapOf(
        "application/tincan+zip" to XapiPackageContentView.VIEW_NAME,
        "application/khan-video+zip" to VideoContentView.VIEW_NAME,
        "application/webchunk+zip" to WebChunkView.VIEW_NAME,
        "application/epub+zip" to EpubContentView.VIEW_NAME,
        "application/har+zip" to HarView.VIEW_NAME,
        "application/h5p-tincan+zip" to XapiPackageContentView.VIEW_NAME,
        "application/pdf" to PDFContentView.VIEW_NAME,
) + VideoContentPresenterCommon.VIDEO_MIME_MAP.keys.map { it to VideoContentView.VIEW_NAME }.toMap()


val mimeTypeToPlayStoreIdMap = mapOf(
        "text/plain" to "com.microsoft.office.word",
        "audio/mpeg" to "music.musicplayer",
        "application/pdf" to "com.adobe.reader",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" to "com.microsoft.office.powerpoint",
        "com.microsoft.office.powerpoint" to "com.microsoft.office.powerpoint",
        "image/jpeg" to "com.pcvirt.ImageViewer",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "com.microsoft.office.word")



class ContentEntryOpener(override val di: DI, val endpoint: Endpoint) : DIAware {

    private val umAppDatabase: UmAppDatabase by di.on(endpoint).instance(tag = DoorTag.TAG_DB)

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    /**
     * Opens the given ContentEntry. If the entry is available, then open the relevant view and show the latest container
     *
     */
    suspend fun openEntry(
        context: Any,
        contentEntryUid: Long,
        downloadRequired: Boolean,
        goToContentEntryDetailViewIfNotDownloaded: Boolean,
        noIframe: Boolean,
        learnerGroupUid: Long = 0,
        clazzUid: Long = 0
    ) {

        Napier.d("OPENING ENTRY " + contentEntryUid.toString())

        val containerToOpen = umAppDatabase.containerDao
            .getMostRecentAvailableContainerUidAndMimeType(contentEntryUid,
                downloadRequired)

        val goToOptions = if(learnerGroupUid != 0L){
            UstadMobileSystemCommon.UstadGoOptions("", true)
        }else{
            UstadMobileSystemCommon.UstadGoOptions("", false)
        }

        when {
            containerToOpen != null -> {
                val viewName = mimeTypeToViewNameMap[containerToOpen.mimeType]
                if(viewName != null) {
                    val args = mapOf(ARG_NO_IFRAMES to noIframe.toString(),
                            ARG_CONTENT_ENTRY_UID to contentEntryUid.toString(),
                            ARG_CONTAINER_UID to containerToOpen.containerUid.toString(),
                            ARG_CLAZZUID to clazzUid.toString(),
                            ARG_LEARNER_GROUP_UID to learnerGroupUid.toString())

                    systemImpl.go(viewName, args, context, goToOptions)
                }else {
                    val container = umAppDatabase.containerEntryDao.findByContainerAsync(containerToOpen.containerUid)
                    require(container.isNotEmpty()) { "No file found in the container." }
                    val containerEntryFilePath = container[0].containerEntryFile?.cefPath
                    if (containerEntryFilePath != null) {
                        systemImpl.openFileInDefaultViewer(context, DoorUri.parse(containerEntryFilePath),
                                containerToOpen.mimeType)
                    } else {
                        throw IllegalArgumentException("No file found in container")
                    }
                    return
                }
            }

            goToContentEntryDetailViewIfNotDownloaded -> {
                systemImpl.go(ContentEntryDetailView.VIEW_NAME,
                        mapOf(ARG_ENTITY_UID to contentEntryUid.toString(),
                        ARG_CLAZZUID to clazzUid.toString()), context, goToOptions)
            }

            else -> {
                throw IllegalArgumentException("No file found. Container null")
            }
        }

    }
}


