package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.contentformats.har.HarContainer
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.HarView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.js.JsName


abstract class HarContentPresenterCommon(context: Any, arguments: Map<String, String>, view: HarView,
                                         val localHttp: String, di: DI) :
        UstadBaseController<HarView>(context, arguments, view, di) {

    private var clazzUid: Long = 0L
    lateinit var harContainer: HarContainer
    var containerUid: Long = 0
    val containerDeferred = CompletableDeferred<HarContainer>()

    private val accountManager: UstadAccountManager by instance()

    val dbRepo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    private val contentEntryOpener: ContentEntryOpener by di.on(accountManager.activeAccount).instance()

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val entryUuid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID).toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID).toLong()
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

        GlobalScope.launch {
            try {
                val result = dbRepo.contentEntryDao.getContentByUuidAsync(entryUuid)
                        ?: ContentEntry()
                view.runOnUiThread(Runnable {
                    view.entry = result
                })

                harContainer = HarContainer(containerUid, result, accountManager.activeAccount, db,
                        context, localHttp, di.direct.instance()) {
                    handleUrlLinkToContentEntry(it)
                }
                harContainer.startingUrlDeferred.await()
                containerDeferred.complete(harContainer)
                view.loadUrl(harContainer.startingUrl)

            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.showSnackBar(
                        systemImpl.getString(MessageID.error_opening_file, context))
                })
            }
        }
    }

    @JsName("handleUrlLinkToContentEntry")
    fun handleUrlLinkToContentEntry(sourceUrl: String) {
        val dest = sourceUrl.replace("content-detail?",
                ContentEntryDetailView.VIEW_NAME + "?")
        val params = UMFileUtil.parseURLQueryString(dest)

        if (params.containsKey("sourceUrl")) {

            GlobalScope.launch {
                try {
                    val entry = dbRepo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(params.getValue("sourceUrl"))
                            ?: throw IllegalArgumentException("No File found")
                    contentEntryOpener.openEntry(context, entry.contentEntryUid, downloadRequired = true, goToContentEntryDetailViewIfNotDownloaded = true,
                            noIframe = arguments[ARG_NO_IFRAMES]?.toBoolean() ?: false, clazzUid = clazzUid)
                } catch (e: Exception) {
                    if (e is NoAppFoundException) {
                        view.showErrorWithAction(systemImpl.getString(MessageID.no_app_found, context),
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