package com.ustadmobile.core.domain.person.bulkadd

class BulkAddPersonException(
    message: String? = null,
    val errors: List<BulkAddPersonsDataError>,
): IllegalArgumentException(message) {
    override fun toString(): String {
        return "${message ?:""} ${errors.joinToString()}"
    }
}


