package com.ustadmobile.port.android.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.lib.db.entities.TerminologyEntry
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.direct
import org.kodein.di.instance

/**
 * CourseTerminology is used to provide alternative words for student, teacher etc. See CourseTerminology
 *
 * First the terminology should be parsed (once), then it can be used throughout components as follows
 * e.g. to create a Text for the word to be used for "Teacher" on this course
 *
 * val terminologyEntries = rememberCourseTerminologyEntries(courseTerminologyEntity)
 * Text(courseTerminologyEntryResource(terminologyEntries, MessageID.teacher))
 *
 * @param courseTerminology The CourseTerminology being used
 */
@Composable
fun rememberCourseTerminologyEntries(
    courseTerminology: CourseTerminology?
): List<TerminologyEntry> {
    val di: DI by closestDI(LocalContext.current)
    val termJsonStr = courseTerminology?.ctTerminology
    return remember(termJsonStr) {
        courseTerminology.toTerminologyEntries(
            json = di.direct.instance(),
            systemImpl = null,
        )
    }
}

/**
 * Get the correct string to use for a given CourseTerminology and MessageID. If the string is
 * set in the CourseTerminology the CourseTerminology will be used, otherwise the default
 * string will be used.
 *
 * @param terminologyEntries terminology entries e.g. as per rememberCourseTerminologyEntries
 * @param messageId the MessageID to lookup e.g. MessageID.teacher MessageID.student etc.
 *
 */
@Composable
fun courseTerminologyEntryResource(
    terminologyEntries: List<TerminologyEntry>,
    messageId: Int
): String {
    return terminologyEntries.firstOrNull { it.messageId ==  messageId }?.term
        ?: messageIdResource(messageId)
}
