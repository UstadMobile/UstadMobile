package com.ustadmobile.core.view

import kotlin.js.JsName

interface UstadViewWithProgress {
    @JsName("showBaseProgressBar")
    fun showBaseProgressBar(showProgress: Boolean)

}