package com.ustadmobile.core.contentjob

import com.ustadmobile.lib.db.entities.ContentEntryPicture2
import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.serialization.Serializable

@Serializable
data class MetadataResult(
    val entry: ContentEntryWithLanguage,

    /**
     * The pluginId that provided this metadata
     */
    val importerId: Int,

    /**
     * The original filename as it was uploaded / selected by the user. This is required when the
     * uri provided (e.g. upload-tmp files that don't include the original name and Android content
     * URIs where the filename is retrieved via Android-specific functions).
     */
    val originalFilename: String? = null,

    val picture: ContentEntryPicture2? = null,
) {

    companion object {

        const val UPLOAD_TMP_PROTO_NAME = "upload-tmp"

        const val UPLOAD_TMP_LOCATOR_PREFIX = "$UPLOAD_TMP_PROTO_NAME:///"

    }

}