package com.ustadmobile.core.impl.locale

import dev.icerock.moko.resources.StringResource

/**
 * Represents a Term Entry in CourseTerminology
 */
data class TerminologyEntry(
    /**
     * The key used in the json as per TerminologyKeys
     */
    val id: String,
    /**
     * The StringResource default e.g. MR.student etc.
     */
    val stringResource: StringResource,

    /**
     * The string to show the user e.g. "Student"
     */
    var term: String?,

    /**
     * Used to display errors when this is used as part of the UiState on CourseTerminologyEdit
     */
    var errorMessage: String? = null
)