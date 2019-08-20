package com.ustadmobile.core.view

import kotlin.js.JsName


interface ContentWithOptionsView : UstadView {

    @JsName("createNewFolder")
    fun createNewFolder(arguments: HashMap<String, String?>)

    @JsName("startFileBrowser")
    fun startFileBrowser(arguments: HashMap<String, String?>)

    @JsName("createNewContent")
    fun createNewContent(arguments: HashMap<String, String?>)

    @JsName("importContentFromLink")
    fun importContentFromLink(arguments: HashMap<String, String?>)
}