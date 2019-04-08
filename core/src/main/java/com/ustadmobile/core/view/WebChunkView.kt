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

        val ARG_CHUNK_PATH = "chunkpath"
        val VIEW_NAME = "webChunk"
        val ARG_CONTENT_ENTRY_ID = "entryId"
        val ARG_CONTAINER_UID = "containerUid"
    }
}
