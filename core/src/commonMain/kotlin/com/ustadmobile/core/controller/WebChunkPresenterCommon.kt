package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryOpener
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.core.view.UstadView.Companion.ARG_NO_IFRAMES
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import kotlin.js.JsName

open class IndexLog {

    var title: String? = null

    var entries: MutableList<IndexEntry>? = null

    var links: Map<String, String>? = null

    inner class IndexEntry {

        var url: String = ""

        var mimeType: String? = null

        var path: String? = null

        var headers: Map<String, String>? = null

        var requestHeaders: Map<String, String>? = null

    }

}

abstract class WebChunkPresenterCommon(context: Any, arguments: Map<String, String>,
                                       view: WebChunkView,
                                       di: DI)

    : UstadBaseController<WebChunkView>(context, arguments, view, di) {

    private var clazzUid: Long = 0L

    internal var containerUid: Long? = null

    private val contentEntryOpener: ContentEntryOpener by di.instance()

    val accountManager: UstadAccountManager by instance()

    val db: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_DB)

    val repo: UmAppDatabase by on(accountManager.activeAccount).instance(tag = DoorTag.TAG_REPO)

    private val systemImpl: UstadMobileSystemImpl by di.instance()

    @JsName("handleMountChunk")
    abstract suspend fun handleMountChunk()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        val entryUuid = arguments.getValue(UstadView.ARG_CONTENT_ENTRY_UID).toLong()
        containerUid = arguments.getValue(UstadView.ARG_CONTAINER_UID).toLong()
        clazzUid = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L

        GlobalScope.launch {
            try {
                val result = repo.contentEntryDao.getContentByUuidAsync(entryUuid)
                view.runOnUiThread(Runnable {
                    view.entry = result
                })
            } catch (e: Exception) {
                view.runOnUiThread(Runnable {
                    view.showSnackBar(
                        systemImpl.getString(MessageID.error_opening_file, context))
                })
            }

            handleMountChunk()
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
                    val entry = repo.contentEntryDao.findBySourceUrlWithContentEntryStatusAsync(params.getValue("sourceUrl"))
                            ?: throw IllegalArgumentException("No File found")
                    contentEntryOpener.openEntry(context, entry.contentEntryUid, true,
                        true, arguments[ARG_NO_IFRAMES]?.toBoolean() ?: false, clazzUid = clazzUid)
                } catch (e: Exception) {
                    if (e is NoAppFoundException) {
                        view.showNoAppFoundError(systemImpl.getString(MessageID.no_app_found, context),
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
        //This is now handled by jetpack navcontroller
    }

}
