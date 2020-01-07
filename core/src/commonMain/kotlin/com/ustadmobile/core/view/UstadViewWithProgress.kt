package com.ustadmobile.core.view

import kotlin.js.JsName

interface UstadViewWithProgress : UstadView {
    @JsName("showBaseProgressBar")
    fun showBaseProgressBar(showProgress: Boolean)

}