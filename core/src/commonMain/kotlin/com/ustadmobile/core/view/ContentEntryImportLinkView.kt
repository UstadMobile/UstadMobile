package com.ustadmobile.core.view

import com.ustadmobile.core.contentjob.MetadataResult

interface ContentEntryImportLinkView : UstadView {

    var validLink: Boolean

    var inProgress: Boolean

    fun finishWithResult(result: MetadataResult)

    companion object {

        const val CONTENT_ENTRY_PARENT_UID = "ContentEntryParentUid"

        const val VIEW_NAME = "ContentEntryImportLinkView"

        const val END_POINT_URL = "endpointUrl"

    }

}