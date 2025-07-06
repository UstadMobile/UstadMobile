package com.ustadmobile.core.viewmodel.contententry.detailattemptlisttab

/**
 * Attempts list screens PersonAndPictureAndNumAttempts and SessionTimeAndProgressInfo both provide
 * successful and completion booleans to for success (nullable) and completion. This interface makes
 * it possible to created shared extension functions (e.g. for a given combination to a string
 * resource)
 */
data class StatementSummaryEntity(
    val successful: Boolean?,
    val completed: Boolean,
)