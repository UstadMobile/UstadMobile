package com.ustadmobile.hooks

import com.ustadmobile.core.components.DIContext
import com.ustadmobile.core.impl.locale.StringProvider
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.core.impl.locale.TerminologyEntry
import dev.icerock.moko.resources.StringResource
import org.kodein.di.direct
import org.kodein.di.instance
import react.useMemo
import react.useRequiredContext

/**
 * Parse and remember the terminology entries. Same as rememberCourseTerminologyEntries on Jetpack.
 *
 * Can be used as follows on a component (e.g. to show text for teacher)
 *
 * val terminologyEntries = useCourseTerminologyEntries(courseTerminology)
 * Typography {
 *    + courseTerminologyResource(terminologyEntries, stringsXml, MR.strings.teacher)
 * }
 * @param courseTerminology the CourseTerminology entity
 */
fun useCourseTerminologyEntries(
    courseTerminology: CourseTerminology?
): List<TerminologyEntry> {
    val di = useRequiredContext(DIContext)
    val termJsonStr = courseTerminology?.ctTerminology

    return useMemo(termJsonStr) {
        courseTerminology.toTerminologyEntries(
            json = di.direct.instance(),
            systemImpl = null,
        )
    }
}

/**
 * Get the correct word to use for a given CourseTerminology. If a terminology has been set in the
 * TerminologyEntries, then it will be used. If not, this function will fallback to the default term.
 *
 * @param terminologyEntries the list of Terminology Entries as per useCourseTerminologyEntries
 * @param stringProvider the StringProvider being used
 * @param stringResource The MR.strings string resource constant e.g. MR.strings.teacher,
 * MR.strings.student etc. If this is null (e.g. because the value has not been looked up yet etc)
 * then the fallbackValue will be used
 * @param fallbackValue the string to use if the stringResource is null
 */
fun courseTerminologyResource(
    terminologyEntries: List<TerminologyEntry>,
    stringProvider: StringProvider,
    stringResource: StringResource?,
    fallbackValue: String = "",
): String {
    return terminologyEntries.firstOrNull { it.stringResource ==  stringResource }?.term
        ?: stringResource?.let { stringProvider[it] } ?: fallbackValue
}
