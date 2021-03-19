package com.ustadmobile.core.io

import kotlinx.serialization.Serializable

@Serializable
data class UploadSessionParams(val md5sRequired: List<String>, val startFrom: Long)
