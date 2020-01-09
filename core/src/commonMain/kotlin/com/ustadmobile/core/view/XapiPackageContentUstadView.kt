package com.ustadmobile.core.view

import kotlin.js.JsName

/**
 * Created by mike on 9/13/17.
 */

interface XapiPackageContentView : UstadView, UstadViewWithSnackBar {

    @JsName("setTitle")
    fun setTitle(title: String)

    @JsName("loadUrl")
    fun loadUrl(url: String)

    companion object {

        const val VIEW_NAME = "XapiContent"
    }

}
