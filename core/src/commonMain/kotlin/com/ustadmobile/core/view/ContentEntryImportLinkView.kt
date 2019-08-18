package com.ustadmobile.core.view

interface ContentEntryImportLinkView : UstadView {

    fun showUrlStatus(isValid: Boolean, message: String)

    fun displayUrl(url: String)

    fun returnResult()

    companion object {

        const val CONTENT_ENTRY_PARENT_UID = "ContentEntryParentUid"

        const val VIEW_NAME = "ContentEntryImportLinkView"

        const val END_POINT_URL = "endpointUrl"

    }

}