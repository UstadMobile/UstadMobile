package com.ustadmobile.core.controller

import com.ustadmobile.core.view.H5PContentView
import kotlin.browser.localStorage

actual abstract class H5PContentPresenterBase actual constructor(context: Any, arguments: Map<String, String?>, view: H5PContentView): UstadBaseController<H5PContentView>(context, arguments, view){
    actual suspend fun mountH5PDist(): String {
        return "${localStorage.getItem("doordb.endpoint.url")}H5PResources/dist"
    }

}