package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.MR
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.lib.db.entities.CourseTerminology
import dev.icerock.moko.resources.StringResource
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
 *  uiState.terminologyStrings[MR.strings.students] ?: stringResource(R.string.students)
 *
 * This is valid for the Message ID listed in TerminologyKeys
 *
 */
class CourseTerminologyStrings(
    private val terminologyEntries: List<TerminologyEntry>
) {

    constructor(
        courseTerminology: CourseTerminology,
        systemImpl: UstadMobileSystemImpl,
        json: Json,
    ) : this(courseTerminology.toTerminologyEntries(json, systemImpl))


    operator fun get(messageId: StringResource) : String? {
        return terminologyEntries.firstOrNull { it.stringResource == messageId }?.term
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