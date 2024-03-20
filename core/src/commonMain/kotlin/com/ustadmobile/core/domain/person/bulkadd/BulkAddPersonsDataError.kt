package com.ustadmobile.core.domain.person.bulkadd

import kotlinx.serialization.Serializable

@Serializable
data class BulkAddPersonsDataError(
    val lineNum: Int,
    val colName: String,
    val invalidValue: String?,
)
