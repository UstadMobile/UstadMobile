package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UMStorageDir
import kotlin.js.JsName

interface ContentEntryExportView : UstadView{

    @JsName("updateExportProgress")
    fun updateExportProgress(progress: Int)

    @JsName("checkFilePermissions")
    fun checkFilePermissions()

    @JsName("setDialogMessage")
    fun setDialogMessage(title: String)

    @JsName("dismissDialog")
    fun dismissDialog()

    @JsName("prepareProgressView")
    fun prepareProgressView(show: Boolean)

    @JsName("setUpStorageOptions")
    fun setUpStorageOptions(storageOptions: List<UMStorageDir>)


    companion object {
        const val VIEW_NAME = "ContentEntryExport"

        const val ARG_CONTENT_ENTRY_TITLE = "entryTitle"
    }
}