package com.ustadmobile.core.domain.person.bulkadd

class BulkAddPersonException(
    message: String? = null,
    val errors: List<BulkAddPersonsDataError>,
): IllegalArgumentException(message)

