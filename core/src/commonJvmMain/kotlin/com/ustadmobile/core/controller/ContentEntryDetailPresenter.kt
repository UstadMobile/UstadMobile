package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.controller.ContentEntryListFragmentPresenter.Companion.ARG_NO_IFRAMES
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.AppConfig
import com.ustadmobile.core.impl.NoAppFoundException
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.networkmanager.DownloadJobItemStatusProvider
import com.ustadmobile.core.networkmanager.LocalAvailabilityMonitor
import com.ustadmobile.core.util.ContentEntryUtil
import com.ustadmobile.core.view.ContentEntryDetailView
import com.ustadmobile.core.view.LoginView


actual class ContentEntryDetailPresenter actual constructor(context: Any, arguments: Map<String, String?>,
                                                            view: ContentEntryDetailView,
                                                            monitor: LocalAvailabilityMonitor,
                                                            statusProvider: DownloadJobItemStatusProvider?,
                                                            private val appRepo: UmAppDatabase)
    : ContentEntryDetailPresenterCommon(context, arguments, view, monitor, statusProvider, appRepo) {

    actual override fun handleDownloadButtonClick() {

        if (isDownloadComplete) {

            val loginFirst = impl.getAppConfigString(AppConfig.KEY_LOGIN_REQUIRED_FOR_CONTENT_OPEN,
                    "false", context)!!.toBoolean()

            if (loginFirst) {
                impl.go(LoginView.VIEW_NAME, args, view.viewContext)
            } else {
                view.showBaseProgressBar(true)
                ContentEntryUtil.instance.goToContentEntry(entryUuid, arguments[ARG_NO_IFRAMES]?.toBoolean()!!,appRepo, impl, isDownloadComplete,
                        context, object : UmCallback<Any> {

                    override fun onSuccess(result: Any?) {
                        view.showBaseProgressBar(false)
                    }

                    override fun onFailure(exception: Throwable?) {
                        if (exception != null) {
                            val message = exception.message
                            if (exception is NoAppFoundException) {
                                view.runOnUiThread(kotlinx.coroutines.Runnable {
                                    view.showFileOpenError(impl.getString(MessageID.no_app_found, context),
                                            MessageID.get_app,
                                            exception.mimeType!!)
                                })
                            } else {
                                view.runOnUiThread(kotlinx.coroutines.Runnable { view.showFileOpenError(message!!) })
                            }
                        }
                    }
                })
            }


        } else {
            val args = HashMap<String, String>()

            //hard coded strings because these are actually in sharedse
            args["contentEntryUid"] = this.entryUuid.toString()
            view.runOnUiThread(kotlinx.coroutines.Runnable { view.showDownloadOptionsDialog(args) })
        }

    }

}