package com.ustadmobile.core.util

import com.ustadmobile.core.controller.ContentEntryDetailPresenter
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackUtil
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.EpubContentView
import com.ustadmobile.core.view.VideoPlayerView
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.core.view.XapiPackageContentView
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus

import java.util.HashMap
import java.util.Hashtable

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

            override fun onSuccess(result: ContentEntryWithContentEntryStatus) {
                goToViewIfDownloaded(result, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            }

            override fun onFailure(exception: Throwable) {
                UmCallbackUtil.onFailIfNotNull(callback, exception)
            }
        })


    }

    private fun goToViewIfDownloaded(entryStatus: ContentEntryWithContentEntryStatus,
                                     dbRepo: UmAppDatabase,
                                     impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                     context: Any,
                                     callback: UmCallback<Any>) {

        if (entryStatus.contentEntryStatus != null && entryStatus.contentEntryStatus.downloadStatus == JobStatus.COMPLETE) {

            dbRepo.containerDao.getMostRecentContainerForContentEntryAsync(entryStatus.contentEntryUid, object : UmCallback<Container> {
                override fun onSuccess(result: Container) {
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
                            override fun onSuccess(resultList: List<ContainerEntryWithContainerEntryFile>) {
                                if (resultList.isEmpty()) {
                                    UmCallbackUtil.onFailIfNotNull(callback, IllegalArgumentException("No file found"))
                                    return
                                }

                                val containerEntryWithContainerEntryFile = resultList[0]
                                if (containerEntryWithContainerEntryFile != null) {
                                    impl.openFileInDefaultViewer(context, containerEntryWithContainerEntryFile.containerEntryFile.cefPath,
                                            result.mimeType, callback)
                                }

                            }

                            override fun onFailure(exception: Throwable) {
                                UmCallbackUtil.onFailIfNotNull(callback, exception)
                            }
                        })
                    }
                    if (viewName != null) {
                        impl.go(viewName, args, context)
                        UmCallbackUtil.onSuccessIfNotNull(callback, Any())
                    }
                }

                override fun onFailure(exception: Throwable) {
                    UmCallbackUtil.onFailIfNotNull(callback, exception)
                }
            })

        } else if (openEntryIfNotDownloaded) {
            val args = HashMap<String, String>()
            args[ContentEntryDetailPresenter.ARG_CONTENT_ENTRY_UID] = entryStatus.contentEntryUid.toString()
            impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
            UmCallbackUtil.onSuccessIfNotNull(callback, Any())
        }

    }

    fun goToContentEntryBySourceUrl(sourceUrl: String, dbRepo: UmAppDatabase,
                                    impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                    context: Any,
                                    callback: UmCallback<Any>) {

        dbRepo.contentEntryDao.findBySourceUrlWithContentEntryStatus(sourceUrl, object : UmCallback<ContentEntryWithContentEntryStatus> {
            override fun onSuccess(result: ContentEntryWithContentEntryStatus) {
                goToViewIfDownloaded(result, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            }

            override fun onFailure(exception: Throwable) {
                UmCallbackUtil.onFailIfNotNull(callback, exception)
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
        if (params?.containsKey("sourceUrl")!!) {
            goToContentEntryBySourceUrl(params.getValue("sourceUrl")!!, dbRepo,
                    impl, openEntryIfNotDownloaded, context,
                    callback)
        }

    }
}
