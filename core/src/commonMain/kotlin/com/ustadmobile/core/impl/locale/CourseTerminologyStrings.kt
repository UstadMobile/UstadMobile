package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry
import kotlinx.serialization.json.Json

/**
 * A course can have it's own terminology e.g. to use the phrase "course participants" instead of
 * "students" in the context of teacher training etc. CourseTerminologyStrings is designed to make
 * it easier when using these terms in screens.
 *
 * CourseTerminologyStrings can be included as part of the UI state on relevant screens.
 *
 * e.g.
 *
 * val terminologyStrings = CourseTerminologyStrings(courseTerminology, systemImpl, json)
 *
 * Get the string to use for students (if overriden):
 *  uiState.terminologyStrings[MessageID.students] ?: stringResource(R.string.students)
 *
 * This is valid for the Message ID listed in TerminologyKeys
 *
 */
@kotlinx.serialization.Serializable
class CourseTerminologyStrings(
    private val terminologyEntries: List<TerminologyEntry>
) {

    constructor(
        courseTerminology: CourseTerminology,
        systemImpl: UstadMobileSystemImpl,
        json: Json,
    ) : this(courseTerminology.toTerminologyEntries(json, systemImpl))


    operator fun get(messageId: Int) : String? {
        return terminologyEntries.firstOrNull { it.messageId == messageId }?.term
    }

    override fun equals(other: Any?): Boolean {
        return (other as? CourseTerminologyStrings)?.terminologyEntries == terminologyEntries
    }

    override fun hashCode(): Int {
        return terminologyEntries.hashCode()
    }

    companion object {

        /**
         * This can be used in Jetpack compose and React previews.
         *
         *
         */
        @Suppress("unused")
        val PREVIEW_TERMINOLOGY = CourseTerminologyStrings(emptyList())

    }

}