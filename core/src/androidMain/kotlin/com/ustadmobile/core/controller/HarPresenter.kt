package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.HarWebViewClient
import com.ustadmobile.core.view.HarAndroidView
import com.ustadmobile.core.view.HarView

actual class HarPresenter actual constructor(context: Any, arguments: Map<String, String?>, view: HarView)
    : HarPresenterCommon(context, arguments, view) {

    lateinit var harWebViewClient: HarWebViewClient

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)



        (view as HarAndroidView).setChromeClient(harWebViewClient)
    }
}