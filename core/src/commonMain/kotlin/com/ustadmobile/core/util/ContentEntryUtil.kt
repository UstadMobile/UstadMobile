package com.ustadmobile.core.util

import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ContentEntryUtil {

    val mimeTypeToPlayStoreIdMap = HashMap<String, String>()

    init {
        mimeTypeToPlayStoreIdMap["text/plain"] = "com.microsoft.office.word"
        mimeTypeToPlayStoreIdMap["audio/mpeg"] = "music.musicplayer"
        mimeTypeToPlayStoreIdMap["application/pdf"] = "com.adobe.reader"
        mimeTypeToPlayStoreIdMap["application/vnd.openxmlformats-officedocument.presentationml.presentation"] = "com.microsoft.office.powerpoint"
        mimeTypeToPlayStoreIdMap["com.microsoft.office.powerpoint"] = "com.microsoft.office.powerpoint"
        mimeTypeToPlayStoreIdMap["image/jpeg"] = "com.pcvirt.ImageViewer"
        mimeTypeToPlayStoreIdMap["application/vnd.openxmlformats-officedocument.wordprocessingml.document"] = "com.microsoft.office.word"
    }


    fun goToContentEntry(contentEntryUid: Long, dbRepo: UmAppDatabase,
                         impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                         context: Any,
                         callback: UmCallback<Any>) {

        GlobalScope.launch {
            try {
                val result = dbRepo.contentEntryDao.findByUidWithContentEntryStatusAsync(contentEntryUid)
                goToViewIfDownloaded(result!!, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
    }

    private suspend fun goToViewIfDownloaded(entryStatus: ContentEntryWithContentEntryStatus,
                                             dbRepo: UmAppDatabase,
                                             impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                             context: Any,
                                             callback: UmCallback<Any>) {
        val contentEntryStatus = entryStatus.contentEntryStatus
        if (contentEntryStatus != null && contentEntryStatus.downloadStatus == JobStatus.COMPLETE) {

            val result = dbRepo.containerDao.getMostRecentDownloadedContainerForContentEntryAsync(entryStatus.contentEntryUid)
                    ?: throw IllegalArgumentException("No file found")
            val args = HashMap<String, String>()
            var viewName: String? = null

            if (result.mimeType?.startsWith("video/") == true) {
                result.mimeType = "video/mp4"
            }

            when (result.mimeType) {
                "application/zip", "application/tincan+zip" -> {
                    args[XapiPackageContentView.ARG_CONTAINER_UID] = result.containerUid.toString()
                    viewName = XapiPackageContentView.VIEW_NAME
                }
                "video/mp4", "application/khan-video+zip" -> {

                    args[VideoPlayerView.ARG_CONTAINER_UID] = result.containerUid.toString()
                    args[VideoPlayerView.ARG_CONTENT_ENTRY_ID] = result.containerContentEntryUid.toString()
                    viewName = VideoPlayerView.VIEW_NAME
                }
                "application/webchunk+zip" -> {

                    args[WebChunkView.ARG_CONTAINER_UID] = result.containerUid.toString()
                    args[WebChunkView.ARG_CONTENT_ENTRY_ID] = result.containerContentEntryUid.toString()
                    viewName = WebChunkView.VIEW_NAME
                }
                "application/epub+zip" -> {

                    args[EpubContentView.ARG_CONTAINER_UID] = result.containerUid.toString()
                    viewName = EpubContentView.VIEW_NAME
                }

                "application/h5p+zip" -> {
                    args[UstadView.ARG_CONTAINER_UID] = result.containerUid.toString()
                    viewName = H5PContentView.VIEW_NAME
                }
                else -> {

                    val container = dbRepo.containerEntryDao.findByContainerAsync(result.containerUid)
                    if (container.isEmpty()) {
                        throw IllegalArgumentException("No file found")
                    }
                    val containerEntryFilePath = container[0].containerEntryFile?.cefPath
                    if (containerEntryFilePath != null) {
                        impl.openFileInDefaultViewer(context, containerEntryFilePath,
                                result.mimeType!!, callback)
                    } else {
                        TODO("Show error message here")
                    }

                }

            }
            if (viewName != null) {
                impl.go(viewName, args, context)
                callback.onSuccess(Any())
            }

        } else if (openEntryIfNotDownloaded) {
            val args = HashMap<String, String>()
            args[ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID] = entryStatus.contentEntryUid.toString()
            impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
        }

    }

    private fun goToContentEntryBySourceUrl(sourceUrl: String, dbRepo: UmAppDatabase,
                                            impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                            context: Any,
                                            callback: UmCallback<Any>) {

        GlobalScope.launch {
            try {
                val result = dbRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(sourceUrl)
                goToViewIfDownloaded(result!!, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }


    }

    /**
     * Used to handle navigating when the user clicks a link in content.
     *
     * @param viewDestination
     * @param dbRepo
     * @param impl
     * @param openEntryIfNotDownloaded
     * @param context
     * @param callback
     */
    fun goToContentEntryByViewDestination(viewDestination: String, dbRepo: UmAppDatabase,
                                          impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                          context: Any, callback: UmCallback<Any>) {
        //substitute for previously scraped content
        val dest = viewDestination.replace("content-detail?",
                ContentEntryDetailView.VIEW_NAME + "?")

        val params = UMFileUtil.parseURLQueryString(dest)
        if (params.containsKey("sourceUrl")) {
            goToContentEntryBySourceUrl(params.getValue("sourceUrl")!!, dbRepo,
                    impl, openEntryIfNotDownloaded, context,
                    callback)
        }

    }
}
