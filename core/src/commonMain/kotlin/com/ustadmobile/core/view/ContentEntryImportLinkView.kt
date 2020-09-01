package com.ustadmobile.core.view

interface ContentEntryImportLinkView : UstadView {

    fun showHideProgress(show: Boolean)

    var validLink: Boolean

    companion object {

        const val CONTENT_ENTRY_PARENT_UID = "ContentEntryParentUid"

        const val VIEW_NAME = "ContentEntryImportLinkView"

        const val END_POINT_URL = "endpointUrl"

    }

}