package com.ustadmobile.core.controller

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.impl.UstadMobileSystemImpl.Companion.ARG_REFERRER
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.DummyView
import com.ustadmobile.core.view.WebChunkView
import com.ustadmobile.core.view.WebChunkView.Companion.ARG_CONTAINER_UID
import com.ustadmobile.core.view.WebChunkView.Companion.ARG_CONTENT_ENTRY_ID
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry

class WebChunkPresenter(context: Any, arguments: Map<String, String>, view: WebChunkView)
    : UstadBaseController<WebChunkView>(context, arguments, view) {

    private var navigation: String? = null

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)
        val contentEntryDao = repoAppDatabase.contentEntryDao
        val containerDao = repoAppDatabase.containerDao

        val entryUuid = arguments.getValue(ARG_CONTENT_ENTRY_ID)!!.toLong()
        val containerUid = arguments.getValue(ARG_CONTAINER_UID)!!.toLong()


        navigation = arguments[ARG_REFERRER] ?: ""

        contentEntryDao.getContentByUuid(entryUuid, object : UmCallback<ContentEntry> {
            override fun onSuccess(result: ContentEntry?) {
                view.runOnUiThread(Runnable { view.setToolbarTitle(result!!.title) })
            }

            override fun onFailure(exception: Throwable?) {
                view.runOnUiThread(Runnable { view.showError(UstadMobileSystemImpl.instance.getString(MessageID.error_opening_file, context)) })
            }
        })

        containerDao.findByUid(containerUid, object : UmCallback<Container> {

            override fun onSuccess(result: Container?) {
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

            override fun onFailure(exception: Throwable?) {

            }
        })
    }

    fun handleUrlLinkToContentEntry(sourceUrl: String) {
        val impl = UstadMobileSystemImpl.instance
        val repoAppDatabase = UmAccountManager.getRepositoryForActiveAccount(context)

        ContentEntryUtil.goToContentEntryByViewDestination(
                sourceUrl,
                repoAppDatabase, impl,
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
                    UMFileUtil.parseURLQueryString(lastEntryListArgs), view.context,
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP)
        } else {
            impl.go(DummyView.VIEW_NAME, mapOf(), view.context,
                    UstadMobileSystemImpl.GO_FLAG_CLEAR_TOP or UstadMobileSystemImpl.GO_FLAG_SINGLE_TOP)
        }

    }

}
