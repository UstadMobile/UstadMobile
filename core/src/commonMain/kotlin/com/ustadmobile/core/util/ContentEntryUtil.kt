package com.ustadmobile.core.util

import com.ustadmobile.core.controller.ContentEntryDetailPresenter.Companion.ARG_CONTENT_ENTRY_UID
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.*
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntryWithContentEntryStatus
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.jvm.JvmStatic

class ContentEntryUtil{

    internal lateinit var impl: UstadMobileSystemImpl

    private lateinit var dbRepo: UmAppDatabase

    internal lateinit var context: Any

    internal lateinit var callback: UmCallback<Any>

    internal lateinit var viewName: String

    private var noIframe: Boolean = false

    internal val args = HashMap<String, String>()

    init {
        mimeTypeToPlayStoreIdMap["text/plain"] = "com.microsoft.office.word"
        mimeTypeToPlayStoreIdMap["audio/mpeg"] = "music.musicplayer"
        mimeTypeToPlayStoreIdMap["application/pdf"] = "com.adobe.reader"
        mimeTypeToPlayStoreIdMap["application/vnd.openxmlformats-officedocument.presentationml.presentation"] = "com.microsoft.office.powerpoint"
        mimeTypeToPlayStoreIdMap["com.microsoft.office.powerpoint"] = "com.microsoft.office.powerpoint"
        mimeTypeToPlayStoreIdMap["image/jpeg"] = "com.pcvirt.ImageViewer"
        mimeTypeToPlayStoreIdMap["application/vnd.openxmlformats-officedocument.wordprocessingml.document"] = "com.microsoft.office.word"
    }



    fun goToContentEntry(isDownloadEnabled:Boolean,contentEntryUid: Long,noIframe: Boolean ,dbRepo: UmAppDatabase,
                         impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                         context: Any,
                         callback: UmCallback<Any>) {
        this.noIframe = noIframe

        GlobalScope.launch {
            try {
                handleGoToView(isDownloadEnabled,contentEntryUid,null, dbRepo, impl,
                        openEntryIfNotDownloaded, context, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
    }

    private suspend fun handleFoundContainer(result: Container){

        if (result.mimeType?.startsWith("video/") == true) {
            result.mimeType = "video/mp4"
        }

        args[ContentEntryListFragmentPresenter.ARG_NO_IFRAMES] = noIframe.toString()
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
            }else -> {

            val container = dbRepo.containerEntryDao.findByContainerAsync(result.containerUid)
            require(container.isNotEmpty()) { "No file found" }
            val containerEntryFilePath = container[0].containerEntryFile?.cefPath
            if (containerEntryFilePath != null) {
                impl.openFileInDefaultViewer(context, containerEntryFilePath,
                        result.mimeType!!, callback)
            } else {
                TODO("Show error message here")
            }
        }

        }
    }


    private fun goToContentEntryBySourceUrl(isDownloadEnabled:Boolean,sourceUrl: String,dbRepo: UmAppDatabase,
                                            impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                            context: Any,callback: UmCallback<Any>) {

        GlobalScope.launch {
            try {
                handleGoToView(isDownloadEnabled,null,sourceUrl, dbRepo, impl, openEntryIfNotDownloaded, context, callback)
            } catch (e: Exception) {
                callback.onFailure(e)
            }
        }
    }


    private suspend fun handleGoToView(isDownloadEnabled:Boolean, entryUid: Long?, sourceUrl: String?,
                                       dbRepo: UmAppDatabase, impl: UstadMobileSystemImpl,
                                       openEntryIfNotDownloaded: Boolean, context: Any,
                                       callback: UmCallback<Any>){
        if(isDownloadEnabled){
            var entryStatus = ContentEntryWithContentEntryStatus()
            if(entryUid != null){
                entryStatus = dbRepo.contentEntryDao.findByUidWithContentEntryStatusAsync(entryUid)!!
            }

            if(sourceUrl != null){
                entryStatus = dbRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(sourceUrl)!!

            }
            val contentEntryStatus = entryStatus.contentEntryStatus

            this.dbRepo = dbRepo; this.impl = impl; this.context = context; this.callback = callback

            if (contentEntryStatus != null && contentEntryStatus.downloadStatus == JobStatus.COMPLETE) {

                val result = dbRepo.containerDao.getMostRecentDownloadedContainerForContentEntryAsync(entryStatus.contentEntryUid)
                        ?: throw IllegalArgumentException("No file found")
                handleFoundContainer(result)
                impl.go(viewName, args, context)
                callback.onSuccess(Any())

            } else if (openEntryIfNotDownloaded) {
                val args = HashMap<String, String>()
                args[ARG_CONTENT_ENTRY_UID] = entryStatus.contentEntryUid.toString()
                impl.go(ContentEntryDetailView.VIEW_NAME, args, context)
            }

        }else{
            this.dbRepo = dbRepo; this.impl = impl; this.context = context; this.callback = callback
            val result = dbRepo.containerDao.getMostRecentContainerForContentEntryAsync(entryUid!!)
                    ?: throw IllegalArgumentException("No file found")
            handleFoundContainer(result)
            impl.go(viewName, args, context)
            callback.onSuccess(Any())
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
    fun goToContentEntryByViewDestination(viewDestination: String,noIframe: Boolean, dbRepo: UmAppDatabase,
                                          impl: UstadMobileSystemImpl, openEntryIfNotDownloaded: Boolean,
                                          context: Any, isDownloadEnabled:Boolean, callback: UmCallback<Any>) {
        //substitute for previously scraped content
        val dest = viewDestination.replace("content-detail?",
                ContentEntryDetailView.VIEW_NAME + "?")
        this.noIframe = noIframe

        val params = UMFileUtil.parseURLQueryString(dest)
        if (params.containsKey("sourceUrl")) {
            goToContentEntryBySourceUrl(isDownloadEnabled,params.getValue("sourceUrl")!!,dbRepo,
                    impl, openEntryIfNotDownloaded, context,
                    callback)
        }

    }


    companion object{

        val mimeTypeToPlayStoreIdMap = HashMap<String, String>()

        /**
         * Get an instance of the system implementation
         */
        @JvmStatic
        var instance: ContentEntryUtil = ContentEntryUtil()
    }
}