package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.core.view.HarView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@ExperimentalStdlibApi
actual class HarContentPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                    view: HarView, db: UmAppDatabase,
                                                    repository: UmAppDatabase,
                                                    localHttp: String)
    : HarContentPresenterCommon(context, arguments, view,db, repository, localHttp) {

    lateinit var harWebViewClient: HarWebViewClient

    override fun onCreate(savedState: Map<String, String>?) {
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