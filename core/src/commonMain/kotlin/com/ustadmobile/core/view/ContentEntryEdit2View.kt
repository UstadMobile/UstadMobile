package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry


interface ContentEntryEdit2View: UstadEditView<ContentEntry> {

    companion object {

        const val VIEW_NAME = "ContentEntryEdit2EditView"

        const val CONTENT_ENTRY_LEAF = "content_entry_leaf"

        const val CONTENT_TYPE = "content_type"

    }

}