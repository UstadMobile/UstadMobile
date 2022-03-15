package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry

interface WebChunkView : UstadView {

    var entry: ContentEntry?

    var url: String

    fun showNoAppFoundError(message: String, actionMessageId: Int, mimeType: String)

    companion object {

        const val VIEW_NAME = "WebChunkView"
    }
}
