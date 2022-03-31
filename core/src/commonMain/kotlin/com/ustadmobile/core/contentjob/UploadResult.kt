package com.ustadmobile.core.contentjob

import kotlinx.serialization.Serializable

@Serializable
class UploadResult(val status: Int, val contentEntryUid: Long) {
}
