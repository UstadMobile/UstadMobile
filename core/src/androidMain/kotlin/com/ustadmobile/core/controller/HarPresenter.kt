package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.core.view.HarView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

actual class HarPresenter actual constructor(context: Any, arguments: Map<String, String?>, view: HarView, isDownloadEnabled: Boolean, repository: UmAppDatabase, localHttp: String)
    : HarPresenterCommon(context, arguments, view, isDownloadEnabled, repository, localHttp) {

    lateinit var harWebViewClient: HarWebViewClient

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        GlobalScope.launch {
            containerDeferred.await()
            view.runOnUiThread(kotlinx.coroutines.Runnable {
                  harWebViewClient = HarWebViewClient(harContainer)
                  (view as HarAndroidView).setChromeClient(harWebViewClient)
              })
        }

    }
}