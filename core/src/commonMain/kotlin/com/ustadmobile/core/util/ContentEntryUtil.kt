package com.ustadmobile.core.util

import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus
import java.util.*

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

        dbRepo.contentEntryDao.findByUidWithContentEntryStatus(contentEntryUid, object : UmCallback<ContentEntryWithContentEntryStatus> {

            override fun onSuccess(result: ContentEntryWithContentEntryStatus?) {
                goToViewIfDownloaded(result!!, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            }

            override fun onFailure(exception: Throwable?) {
                if (exception != null) {
                    UmCallbackUtil.onFailIfNotNull(callback, exception)
                }
            }
        })


    }

    private fun goToViewIfDownloaded(entryStatus: ContentEntryWithContentEntryStatus,
                                     dbRepo: UmAppDatabase,
                                     impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                     context: Any,
                                     callback: UmCallback<Any>) {
        val contentEntryStatus = entryStatus.contentEntryStatus
        if (contentEntryStatus != null && contentEntryStatus.downloadStatus == JobStatus.COMPLETE) {

            dbRepo.containerDao.getMostRecentDownloadedContainerForContentEntryAsync(entryStatus.contentEntryUid,
                    object : UmCallback<Container> {
                        override fun onSuccess(result: Container?) {
                            if (result == null) {
                                UmCallbackUtil.onFailIfNotNull(callback, IllegalArgumentException("No file found"))
                                return
                            }

                            val args = HashMap<String, String>()
                            var viewName: String? = null
                            when (result.mimeType) {
                                "application/zip", "application/tincan+zip" -> {
                                    args[XapiPackageContentView.ARG_CONTAINER_UID] = result.containerUid.toString()
                                    viewName = XapiPackageContentView.VIEW_NAME
                                }
                                "video/mp4" -> {
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
                                "application/khan-video+zip" -> {

                                    args[VideoPlayerView.ARG_CONTAINER_UID] = result.containerUid.toString()
                                    args[VideoPlayerView.ARG_CONTENT_ENTRY_ID] = result.containerContentEntryUid.toString()
                                    viewName = VideoPlayerView.VIEW_NAME
                                }
                                else -> dbRepo.containerEntryDao.findByContainer(result.containerUid, object : UmCallback<List<ContainerEntryWithContainerEntryFile>> {
                                    override fun onSuccess(resultList: List<ContainerEntryWithContainerEntryFile>?) {
                                        if (resultList?.isEmpty()!!) {
                                            UmCallbackUtil.onFailIfNotNull(callback, IllegalArgumentException("No file found"))
                                            return
                                        }

                                        val containerEntryFilePath = resultList[0].containerEntryFile?.cefPath
                                        if(containerEntryFilePath != null) {
                                            impl.openFileInDefaultViewer(context, containerEntryFilePath,
                                                    result.mimeType!!, callback)
                                        }else {
                                            TODO("Show error message here")
                                        }


                                    }

                                    override fun onFailure(exception: Throwable?) {
                                        if (exception != null) {
                                            UmCallbackUtil.onFailIfNotNull(callback, exception)
                                        }
                                    }
                                })
                            }
                            if (viewName != null) {
                                impl.go(viewName, args, context)
                                UmCallbackUtil.onSuccessIfNotNull(callback, Any())
                            }
                        }

                        override fun onFailure(exception: Throwable?) {
                            if (exception != null) {
                                UmCallbackUtil.onFailIfNotNull(callback, exception)
                            }
                        }
                    })

        } else if (openEntryIfNotDownloaded) {
            val args = HashMap<String, String>()
            args[ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID] = entryStatus.contentEntryUid.toString()
            impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
            UmCallbackUtil.onSuccessIfNotNull(callback, Any())
        }

    }

    private fun goToContentEntryBySourceUrl(sourceUrl: String, dbRepo: UmAppDatabase,
                                            impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                            context: Any,
                                            callback: UmCallback<Any>) {

        dbRepo.contentEntryDao.findBySourceUrlWithContentEntryStatus(sourceUrl, object : UmCallback<ContentEntryWithContentEntryStatus> {
            override fun onSuccess(result: ContentEntryWithContentEntryStatus?) {
                goToViewIfDownloaded(result!!, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            }

            override fun onFailure(exception: Throwable?) {
                if (exception != null) {
                    UmCallbackUtil.onFailIfNotNull(callback, exception)
                }
            }
        })


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
