package com.ustadmobile.core.view

import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.lib.db.entities.Container

interface WebChunkView : UstadView {

    fun mountChunk(webChunkPath: Container, callback: UmCallback<String>)

    fun loadUrl(url: String)

    fun showError(message: String)

    fun setToolbarTitle(title: String)

    fun showErrorWithAction(string: String, get_app: Int, mimeType: String)

    companion object {

        const val VIEW_NAME = "webChunk"
        const val ARG_CONTENT_ENTRY_ID = "entryId"
        const val ARG_CONTAINER_UID = "containerUid"
    }
}
