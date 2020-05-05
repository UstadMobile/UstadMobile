package com.ustadmobile.core.controller

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.HarView
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.js.JsName

abstract class HarPresenterCommon(context: Any, arguments: Map<String, String?>, view: HarView, var isDownloadEnabled: Boolean, val appRepo: UmAppDatabase, val localHttp: String) :
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
                        ?: ContentEntry()
                view.runOnUiThread(Runnable {
                    val resultTitle = result.title
                    if (resultTitle != null)
                        view.setToolbarTitle(resultTitle)
                })

                val containerResult = appRepo.containerDao.findByUidAsync(containerUid)!!
                val containerManager = ContainerManager(containerResult, UmAccountManager.getRepositoryForActiveAccount(context), appRepo)
                val account = UmAccountManager.getActiveAccount(context)
                harContainer = HarContainer(containerManager, result, account, context, localHttp) {
                    handleUrlLinkToContentEntry(it)
                }
                containerDeferred.complete(harContainer)
                view.loadUrl(harContainer.startingUrl)

            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.showError(UstadMobileSystemImpl.instance
                            .getString(MessageID.error_opening_file, context))
                })
            }
        }
    }

    @JsName("handleUrlLinkToContentEntry")
    fun handleUrlLinkToContentEntry(sourceUrl: String) {
        val impl = UstadMobileSystemImpl.instance

        val dest = sourceUrl.replace("content-detail?",
                ContentEntryDetailView.VIEW_NAME + "?")
        val params = UMFileUtil.parseURLQueryString(dest)

        if (params.containsKey("sourceUrl")) {

            GlobalScope.launch {
                try {
                    val entry = appRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(params.getValue("sourceUrl"))
                            ?: throw IllegalArgumentException("No File found")
                    goToContentEntry(entry.contentEntryUid, appRepo, context, impl, true,
                            true,
                            arguments[ContentEntryListPresenter.ARG_NO_IFRAMES]?.toBoolean()
                                    ?: false)
                } catch (e: Exception) {
                    if (e is NoAppFoundException) {
                        view.showErrorWithAction(impl.getString(MessageID.no_app_found, context),
                                MessageID.get_app,
                                e.mimeType!!)
                    } else {
                        view.showError(e.message!!)
                    }
                }

            }

        }
    }

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