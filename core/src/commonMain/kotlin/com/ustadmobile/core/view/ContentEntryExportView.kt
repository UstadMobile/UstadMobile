package com.ustadmobile.core.view

import kotlin.js.JsName

interface ContentEntryExportView : UstadView{

    @JsName("updateExportProgress")
    fun updateExportProgress(progress: Int)

    @JsName("checkFilePermissions")
    fun checkFilePermissions()

    fun setMessageText(title: String)

    fun dismissDialog()

    fun prepareProgressView(show: Boolean)

    companion object {
        const val VIEW_NAME = "ContentEntryExport"
        const val ARG_CONTENT_ENTRY_UID = "entryid"
        const val ARG_CONTENT_ENTRY_TITLE = "entryTitle"
    }
}