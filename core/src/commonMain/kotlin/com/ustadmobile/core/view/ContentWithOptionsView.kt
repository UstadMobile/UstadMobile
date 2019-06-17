package com.ustadmobile.core.view


interface ContentWithOptionsView : UstadView {

    fun createNewFolder(arguments: HashMap<String, String?>)

    fun startFileBrowser(arguments: HashMap<String, String?>)

    fun createNewContent(arguments: HashMap<String, String?>)
}