package com.ustadmobile.core.view

import kotlin.js.JsName


interface ContentWithOptionsView : UstadView {

    @JsName("showProgressDialog")
    fun showProgressDialog(show: Boolean)
}