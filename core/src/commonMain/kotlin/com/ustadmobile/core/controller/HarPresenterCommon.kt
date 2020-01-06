package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.contentformats.har.HarRequest
import com.ustadmobile.core.contentformats.har.HarResponse
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

abstract class HarPresenterCommon(context: Any, arguments: Map<String, String?>, view: HarView, var isDownloadEnabled: Boolean, var appRepo: UmAppDatabase) :
        UstadBaseController<HarView>(context, arguments, view) {

    private lateinit var navigation: String
    lateinit var harContainer: HarContainer
    var containerUid: Long = 0
    val containerDeferred = CompletableDeferred<HarContainer>()


    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)


        val entryUuid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID)!!.toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID)!!.toLong()

        navigation = arguments[UstadMobileSystemCommon.ARG_REFERRER] ?: ""

        GlobalScope.launch {
            try {
                val result = appRepo.contentEntryDao.getContentByUuidAsync(entryUuid)
                view.runOnUiThread(Runnable {
                    val resultTitle = result?.title
                    if (resultTitle != null)
                        view.setToolbarTitle(resultTitle)
                })
            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.showError(UstadMobileSystemImpl.instance
                            .getString(MessageID.error_opening_file, context))
                })
            }


            val result = appRepo.containerDao.findByUidAsync(containerUid)!!
            var containerManager = ContainerManager(result, UmAppDatabase.getInstance(context), appRepo)
            harContainer = HarContainer(containerManager)
            containerDeferred.complete(harContainer)
            view.loadUrl(harContainer.startingUrl)
        }
    }

    fun handleInterceptRequest(request: HarRequest): HarResponse {


        return HarResponse()
    }


/* fun handleUrlLinkToContentEntry(sourceUrl: String) {
     val impl = UstadMobileSystemImpl.instance

     ContentEntryUtil.instance.goToContentEntryByViewDestination(
             sourceUrl, arguments[ContentEntryListFragmentPresenter.ARG_NO_IFRAMES].toBoolean(),
             appRepo, impl,
             true,
             context, isDownloadEnabled, object : UmCallback<Any> {
         override fun onSuccess(result: Any?) {

         }

         override fun onFailure(exception: Throwable?) {
             if (exception != null) {
                 val message = exception.message
                 if (exception is NoAppFoundException) {
                     view.runOnUiThread(Runnable {
                         view.showErrorWithAction(impl.getString(MessageID.no_app_found, context),
                                 MessageID.get_app,
                                 exception.mimeType!!)
                     })
                 } else {
                     view.runOnUiThread(Runnable { view.showError(message!!) })
                 }
             }
         }
     })
 }*/

    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation)
        if (lastEntryListArgs != null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(HomeView.VIEW_NAME, mapOf(), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        }

    }


}