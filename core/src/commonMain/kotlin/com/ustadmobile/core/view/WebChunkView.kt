package com.ustadmobile.core.view

import com.ustadmobile.core.container.ContainerManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import kotlin.js.JsName

interface WebChunkView : UstadView {

    var entry: ContentEntry?

    var containerManager: ContainerManager?

    @JsName("showNoAppFoundError")
    fun showNoAppFoundError(message: String, actionMessageId: Int, mimeType: String)

    companion object {

        const val VIEW_NAME = "WebChunk"
    }
}
