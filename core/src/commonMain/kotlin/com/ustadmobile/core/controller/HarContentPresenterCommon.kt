package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.goToContentEntry
import com.ustadmobile.core.view.ContentEntry2DetailView
import com.ustadmobile.core.view.HarView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import kotlin.js.JsName

@ExperimentalStdlibApi
abstract class HarContentPresenterCommon(context: Any, arguments: Map<String, String>, view: HarView,
                                         val db: UmAppDatabase, val appRepo: UmAppDatabase, val localHttp: String, di: DI) :
        UstadBaseController<HarView>(context, arguments, view, di) {

    lateinit var harContainer: HarContainer
    var containerUid: Long = 0
    val containerDeferred = CompletableDeferred<HarContainer>()

    private val accountManager: UstadAccountManager by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val entryUuid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID).toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID).toLong()

        GlobalScope.launch {
            try {
                val result = appRepo.contentEntryDao.getContentByUuidAsync(entryUuid)
                        ?: ContentEntry()
                view.runOnUiThread(Runnable {
                    view.entry = result
                })

                val containerResult = appRepo.containerDao.findByUidAsync(containerUid) ?: throw Exception()
                val containerManager = ContainerManager(containerResult, db, appRepo)
                harContainer = HarContainer(containerManager, result, accountManager.activeAccount, context, localHttp) {
                    handleUrlLinkToContentEntry(it)
                }
                containerDeferred.complete(harContainer)
                view.loadUrl(harContainer.startingUrl)

            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.showSnackBar(UstadMobileSystemImpl.instance
                            .getString(MessageID.error_opening_file, context))
                })
            }
        }
    }

    @JsName("handleUrlLinkToContentEntry")
    fun handleUrlLinkToContentEntry(sourceUrl: String) {
        val impl = UstadMobileSystemImpl.instance

        val dest = sourceUrl.replace("content-detail?",
                ContentEntry2DetailView.VIEW_NAME + "?")
        val params = UMFileUtil.parseURLQueryString(dest)

        if (params.containsKey("sourceUrl")) {

            GlobalScope.launch {
                try {
                    val entry = appRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(params.getValue("sourceUrl"))
                            ?: throw IllegalArgumentException("No File found")
                    goToContentEntry(entry.contentEntryUid, appRepo, context, impl, true,
                            true,
                            arguments[ARG_NO_IFRAMES]?.toBoolean()
                                    ?: false)
                } catch (e: Exception) {
                    if (e is NoAppFoundException) {
                        view.showErrorWithAction(impl.getString(MessageID.no_app_found, context),
                                MessageID.get_app,
                                e.mimeType ?: "")
                    } else {
                        view.showSnackBar(e.message ?: "")
                    }
                }

            }

        }
    }

    fun handleUpNavigation() {
        // handled by nav controller
    }


}