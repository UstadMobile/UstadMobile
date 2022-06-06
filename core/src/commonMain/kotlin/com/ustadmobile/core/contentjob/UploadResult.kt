package com.ustadmobile.core.contentjob

import kotlinx.serialization.Serializable

/**
 *
 */
@Serializable
class UploadResult(
    val status: Int,
    val metadataResult: MetadataResult?,
) {

    companion object {

        const val UPLOAD_TMP_PROTO_NAME = "upload-tmp"

        const val UPLOAD_TMP_LOCATOR_PREFIX = "$UPLOAD_TMP_PROTO_NAME:///"

    }

}
