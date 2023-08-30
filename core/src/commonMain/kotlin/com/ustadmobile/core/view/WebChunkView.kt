package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import dev.icerock.moko.resources.StringResource

interface WebChunkView : UstadView {

    var entry: ContentEntry?

    var url: String

    fun showNoAppFoundError(message: String, actionMessageId: StringResource, mimeType: String)

    companion object {

        const val VIEW_NAME = "WebChunkView"
    }
}
