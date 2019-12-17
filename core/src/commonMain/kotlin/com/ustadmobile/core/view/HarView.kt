package com.ustadmobile.core.view

import kotlin.js.JsName

interface HarView : UstadView {

    @JsName("loadUrl")
    fun loadUrl(url: String)

    @JsName("showError")
    fun showError(message: String)

    @JsName("setToolbarTitle")
    fun setToolbarTitle(title: String)

    @JsName("showErrorWithAction")
    fun showErrorWithAction(message: String, actionMessageId: Int, mimeType: String)

    companion object {

        const val VIEW_NAME = "HarView"
        const val ARG_CONTENT_ENTRY_ID = "entryId"
        const val ARG_CONTAINER_UID = "containerUid"
    }

}