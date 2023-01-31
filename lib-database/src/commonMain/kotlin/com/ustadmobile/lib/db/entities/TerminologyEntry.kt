package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

/**
 * Represents a Term Entry in CourseTerminology
 */
@Serializable
data class TerminologyEntry(
    /**
     * The key used in the json as per TerminologyKeys
     */
    val id: String,
    /**
     * The MessageID default e.g. MessageID.student etc.
     */
    val messageId: Int,

    /**
     * The string to show the user e.g. "Student"
     */
    var term: String?,

    /**
     * Used to display errors when this is used as part of the UiState on CourseTerminologyEdit
     */
    var errorMessage: String? = null
)