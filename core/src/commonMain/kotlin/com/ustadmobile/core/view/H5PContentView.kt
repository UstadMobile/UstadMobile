package com.ustadmobile.core.view

import kotlin.js.JsName

/**
 * Created by mike on 2/15/18.
 */

interface H5PContentView : UstadView {

    @JsName("setContentTitle")
    fun setContentTitle(title: String)

    @JsName("setContentHtml")
    fun setContentHtml(baseUrl: String, html: String)

    companion object {

        const val VIEW_NAME = "H5PContent"

        const val ANDROID_ASSETS_PATH = "/android-assets/"
    }


}
