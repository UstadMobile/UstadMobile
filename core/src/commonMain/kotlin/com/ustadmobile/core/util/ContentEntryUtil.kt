package com.ustadmobile.core.util

import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*


private val mimeTypeToViewNameMap = mapOf(
        "application/tincan+zip" to XapiPackageContentView.VIEW_NAME,
        "application/khan-video+zip" to VideoPlayerView.VIEW_NAME,
        "application/webchunk+zip" to WebChunkView.VIEW_NAME,
        "application/epub+zip" to EpubContentView.VIEW_NAME
) + VideoPlayerPresenterCommon.VIDEO_MIME_MAP.keys.map { it to VideoPlayerView.VIEW_NAME }.toMap()


val mimeTypeToPlayStoreIdMap = mapOf(
        "text/plain" to "com.microsoft.office.word",
        "audio/mpeg" to "music.musicplayer",
        "application/pdf" to "com.adobe.reader",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" to "com.microsoft.office.powerpoint",
        "com.microsoft.office.powerpoint" to "com.microsoft.office.powerpoint",
        "image/jpeg" to "com.pcvirt.ImageViewer",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" to "com.microsoft.office.word")

typealias GoToEntryFn = suspend (contentEntryUid: Long,
                                 umAppDatabase: UmAppDatabase,
                                 context: Any,
                                 systemImpl: UstadMobileSystemImpl,
                                 downloadRequired: Boolean,
                                 goToContentEntryDetailViewIfNotDownloaded: Boolean,
                                 noIframe: Boolean) -> Unit

suspend fun goToContentEntry(contentEntryUid: Long,
                             umAppDatabase: UmAppDatabase,
                             context: Any,
                             systemImpl: UstadMobileSystemImpl,
                             downloadRequired: Boolean,
                             goToContentEntryDetailViewIfNotDownloaded: Boolean = true,
                             noIframe: Boolean = false) {

    val containerToOpen = if (downloadRequired) {
        umAppDatabase.downloadJobItemDao.findMostRecentContainerDownloaded(contentEntryUid)
    } else {
        umAppDatabase.containerDao.getMostRecentContaineUidAndMimeType(contentEntryUid)
    }

    if (containerToOpen != null) {

        val viewName = mimeTypeToViewNameMap[containerToOpen.mimeType]
        if (viewName == null) {

            val container = umAppDatabase.containerEntryDao.findByContainerAsync(containerToOpen.containerUid)
            require(container.isNotEmpty()) { "No file found" }
            val containerEntryFilePath = container[0].containerEntryFile?.cefPath
            if (containerEntryFilePath != null) {
                systemImpl.openFileInDefaultViewer(context, containerEntryFilePath,
                        containerToOpen.mimeType)
            } else {
                throw IllegalArgumentException("No file found")
            }
            return
        }

        val args = HashMap<String, String>()
        args[ContentEntryListFragmentPresenter.ARG_NO_IFRAMES] = noIframe.toString()
        args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()
        args[ARG_CONTAINER_UID] = containerToOpen.containerUid.toString()
        systemImpl.go(viewName, args, context)

    } else if (goToContentEntryDetailViewIfNotDownloaded) {

        val args = HashMap<String, String>()
        args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()
        systemImpl.go(ContentEntryDetailView.VIEW_NAME, args, context)

    } else {
        throw IllegalArgumentException("No file found")
    }

}