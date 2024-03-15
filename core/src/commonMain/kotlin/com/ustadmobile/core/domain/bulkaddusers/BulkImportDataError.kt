package com.ustadmobile.core.domain.bulkaddusers

data class BulkImportDataError(
    val lineNum: Int,
    val colName: String,
    val invalidValue: String?,
)
