package com.ustadmobile.core.contentjob

import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.serialization.Serializable

@Serializable
data class MetadataResult(
    val entry: ContentEntryWithLanguage,

    /**
     * The pluginId that provided this metadata
     */
    val pluginId: Int,

    /**
     * The import source as it will be displayed to the user e.g. "filename.epub" or "http://site.com/book.epub"
     */
    val displaySourceUrl: String? = null,
) {

    companion object {

        const val UPLOAD_TMP_PROTO_NAME = "upload-tmp"

        const val UPLOAD_TMP_LOCATOR_PREFIX = "$UPLOAD_TMP_PROTO_NAME:///"

    }

}