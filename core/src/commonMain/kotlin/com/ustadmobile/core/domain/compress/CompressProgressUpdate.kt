package com.ustadmobile.core.domain.compress

data class CompressProgressUpdate(
    val fromUri: String,
    val completed: Long,
    val total: Long
)
