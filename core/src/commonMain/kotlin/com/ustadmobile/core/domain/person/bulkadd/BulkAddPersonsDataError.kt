package com.ustadmobile.core.domain.person.bulkadd

data class BulkAddPersonsDataError(
    val lineNum: Int,
    val colName: String,
    val invalidValue: String?,
)
