package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import kotlinx.coroutines.Runnable

actual class ContentEntryDetailPresenter actual constructor(context: Any, arguments: Map<String, String?>,
                                                            view: ContentEntryDetailView,
                                                            monitor: LocalAvailabilityMonitor,
                                                            statusProvider: DownloadJobItemStatusProvider?,
                                                            private val appRepo: UmAppDatabase)
    : ContentEntryDetailPresenterCommon(context, arguments, view, monitor, statusProvider, appRepo) {

    actual override fun handleDownloadButtonClick() {
        view.showBaseProgressBar(true)
        ContentEntryUtil.instance.goToContentEntry(entryUuid, appRepo, impl, isDownloadComplete,
                context, object : UmCallback<Any> {

            override fun onSuccess(result: Any?) {
                view.showBaseProgressBar(false)
            }

            override fun onFailure(exception: Throwable?) {
                if (exception != null) {
                    val message = exception.message
                    if (exception is NoAppFoundException) {
                        view.runOnUiThread(Runnable {
                            view.showFileOpenError(impl.getString(MessageID.no_app_found, context),
                                    MessageID.get_app,
                                    exception.mimeType!!)
                        })
                    } else {
                        view.runOnUiThread(Runnable { view.showFileOpenError(message!!) })
                    }
                }
            }
        })
    }

}