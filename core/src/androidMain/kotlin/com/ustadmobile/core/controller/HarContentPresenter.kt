package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.core.view.HarView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI


actual class HarContentPresenter actual constructor(context: Any, arguments: Map<String, String>,
                                                    view: HarView, localHttp: String,
                                                    di: DI)
    : HarContentPresenterCommon(context, arguments, view, localHttp, di) {

    lateinit var harWebViewClient: HarWebViewClient

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        presenterScope.launch {
            containerDeferred.await()

            harWebViewClient = HarWebViewClient(harContainer)
            (view as HarAndroidView).setChromeClient(harWebViewClient)
        }
    }
}