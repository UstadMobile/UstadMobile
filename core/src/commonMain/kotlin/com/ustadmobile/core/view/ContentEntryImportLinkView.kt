package com.ustadmobile.core.view

import com.ustadmobile.core.contentformats.ImportedContentEntryMetaData

interface ContentEntryImportLinkView : UstadView {

    fun showHideProgress(show: Boolean)

    var validLink: Boolean

    fun finishWithResult(result: ImportedContentEntryMetaData)

    companion object {

        const val CONTENT_ENTRY_PARENT_UID = "ContentEntryParentUid"

        const val VIEW_NAME = "ContentEntryImportLinkView"

        const val END_POINT_URL = "endpointUrl"

    }

}