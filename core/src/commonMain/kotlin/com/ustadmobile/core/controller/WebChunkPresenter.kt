package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemCommon
import com.ustadmobile.core.impl.UstadMobileSystemCommon.Companion.ARG_REFERRER
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.core.view.WebChunkView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.WebChunkView.Companion.ARG_CONTENT_ENTRY_ID
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

class WebChunkPresenter(context: Any, arguments: Map<String, String>, view: WebChunkView,
                        private val appRepo: UmAppDatabase)
    : UstadBaseController<WebChunkView>(context, arguments, view) {

    lateinit var cs: ContentEntry
    private var navigation: String? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        val entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_ID)!!.toLong()
        val containerUid = arguments.getValue(ARG_CONTAINER_UID)!!.toLong()


        navigation = arguments[ARG_REFERRER] ?: ""

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
        }

        GlobalScope.launch {
            val result = appRepo.containerDao.findByUidAsync(containerUid)
            view.mountChunk(result!!, object : UmCallback<String> {
                override fun onSuccess(result: String?) {
                    if (result != null) {
                        view.loadUrl(result)
                    } else {
                        view.runOnUiThread(Runnable { view.showError(UstadMobileSystemImpl.instance.getString(MessageID.error_opening_file, context)) })
                    }
                }

                override fun onFailure(exception: Throwable?) {
                    view.runOnUiThread(Runnable { view.showError(UstadMobileSystemImpl.instance.getString(MessageID.error_opening_file, context)) })
                }
            })

        }
    }

    fun handleUrlLinkToContentEntry(sourceUrl: String) {
        val impl = UstadMobileSystemImpl.instance

        ContentEntryUtil.goToContentEntryByViewDestination(
                sourceUrl,
                appRepo, impl,
                true,
                context, object : UmCallback<Any> {
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
    }

    fun handleUpNavigation() {
        val impl = UstadMobileSystemImpl.instance
        val lastEntryListArgs = UMFileUtil.getLastReferrerArgsByViewname(ContentEntryDetailView.VIEW_NAME, navigation!!)
        if (lastEntryListArgs !=
                null) {
            impl.go(ContentEntryDetailView.VIEW_NAME,
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(HomeView.VIEW_NAME, mapOf(), view.viewContext,
                    UstadMobileSystemCommon.GO_FLAG_CLEAR_TOP or UstadMobileSystemCommon.GO_FLAG_SINGLE_TOP)
        }

    }

}
