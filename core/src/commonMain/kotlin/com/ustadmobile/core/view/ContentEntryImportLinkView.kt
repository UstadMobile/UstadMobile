package com.ustadmobile.core.view

interface ContentEntryImportLinkView : UstadView {

    fun showUrlStatus(isValid: Boolean, message: String)

    fun displayUrl(url: String)

}