package com.ustadmobile.core.util

import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListPresenter
import com.ustadmobile.core.controller.VideoPlayerPresenterCommon
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.MimeType.EPUB
import com.ustadmobile.core.util.MimeType.HAR
import com.ustadmobile.core.util.MimeType.JPG
import com.ustadmobile.core.util.MimeType.KHAN_VIDEO
import com.ustadmobile.core.util.MimeType.MPEG
import com.ustadmobile.core.util.MimeType.OFFICE_POWERPOINT
import com.ustadmobile.core.util.MimeType.OPEN_POWERPOINT
import com.ustadmobile.core.util.MimeType.OPEN_WORD
import com.ustadmobile.core.util.MimeType.PDF
import com.ustadmobile.core.util.MimeType.TEXT
import com.ustadmobile.core.util.MimeType.TINCAN
import com.ustadmobile.core.util.MimeType.WEB_CHUNK
import com.ustadmobile.core.view.*
import kotlin.js.JsName


object MimeType {

    const val TINCAN = "application/tincan+zip"

    const val KHAN_VIDEO = "application/khan-video+zip"

    const val WEB_CHUNK = "application/webchunk+zip"

    const val EPUB = "application/epub+zip"

    const val HAR = "application/har+zip"

    const val MP4 = "video/mp4"

    const val MKV = "video/x-matroska"

    const val WEBM = "video/webm"

    const val M4V = "video/x-m4v"

    const val PDF = "application/pdf"

    const val MPEG = "audio/mpeg"

    const val OFFICE_POWERPOINT = "com.microsoft.office.powerpoint"

    const val OPEN_POWERPOINT = "application/vnd.openxmlformats-officedocument.presentationml.presentation"

    const val JPG = "image/jpeg"

    const val OPEN_WORD = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

    const val TEXT = "text/plain"
}


private val mimeTypeToViewNameMap = mapOf(
        TINCAN to XapiPackageContentView.VIEW_NAME,
        KHAN_VIDEO to VideoPlayerView.VIEW_NAME,
        WEB_CHUNK to WebChunkView.VIEW_NAME,
        HAR to HarView.VIEW_NAME,
        EPUB to EpubContentView.VIEW_NAME
) + VideoPlayerPresenterCommon.VIDEO_MIME_MAP.keys.map { it to VideoPlayerView.VIEW_NAME }.toMap()


val mimeTypeToPlayStoreIdMap = mapOf(
        TEXT to "com.microsoft.office.word",
        MPEG to "music.musicplayer",
        PDF to "com.adobe.reader",
        OPEN_POWERPOINT to "com.microsoft.office.powerpoint",
        OFFICE_POWERPOINT to "com.microsoft.office.powerpoint",
        JPG to "com.pcvirt.ImageViewer",
        OPEN_WORD to "com.microsoft.office.word")

typealias GoToEntryFn = suspend (contentEntryUid: Long,
                                 umAppDatabase: UmAppDatabase,
                                 context: Any,
                                 systemImpl: UstadMobileSystemImpl,
                                 downloadRequired: Boolean,
                                 goToContentEntryDetailViewIfNotDownloaded: Boolean,
                                 noIframe: Boolean) -> Unit

@JsName("goToContentEntry")
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

    when {
        containerToOpen != null -> {

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
            args[ContentEntryListPresenter.ARG_NO_IFRAMES] = noIframe.toString()
            args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()
            args[ARG_CONTAINER_UID] = containerToOpen.containerUid.toString()
            systemImpl.go(viewName, args, context)

        }
        goToContentEntryDetailViewIfNotDownloaded -> {

            val args = HashMap<String, String>()
            args[ARG_CONTENT_ENTRY_UID] = contentEntryUid.toString()
            systemImpl.go(ContentEntryDetailView.VIEW_NAME, args, context)

        }
        else -> {
            throw IllegalArgumentException("No file found")
        }
    }

}

