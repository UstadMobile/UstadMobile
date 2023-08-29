package com.ustadmobile.core.view

import com.ustadmobile.lib.db.entities.ContentEntry
import dev.icerock.moko.resources.StringResource
import kotlin.js.JsName

interface HarView : UstadView {

    var entry: ContentEntry?

    @JsName("loadUrl")
    fun loadUrl(url: String)

    @JsName("showErrorWithAction")
    fun showErrorWithAction(message: String, actionMessageId: StringResource, mimeType: String)

    companion object {

        const val VIEW_NAME = "HarView"
        const val ARG_CONTENT_ENTRY_ID = "entryId"
        const val ARG_CONTAINER_UID = "containerUid"
    }

}