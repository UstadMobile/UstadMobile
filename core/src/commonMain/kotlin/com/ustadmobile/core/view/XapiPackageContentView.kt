package com.ustadmobile.core.view

import kotlin.js.JsName

/**
 * Created by mike on 9/13/17.
 */

interface XapiPackageContentView : UstadView {

    @JsName("setTitle")
    var contentTitle: String


    @JsName("loadUrl")
    var urlToLoad: String

    companion object {

        const val VIEW_NAME = "XapiPackageContentView"
    }

}
