package com.ustadmobile.libuicompose.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalInspectionMode
import com.ustadmobile.core.util.ext.toTerminologyEntries
import com.ustadmobile.lib.db.entities.CourseTerminology
import com.ustadmobile.core.impl.locale.TerminologyEntry
import dev.icerock.moko.resources.StringResource
import org.kodein.di.direct
import org.kodein.di.instance
import dev.icerock.moko.resources.compose.stringResource
import org.kodein.di.compose.localDI

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
    val termJsonStr = courseTerminology?.ctTerminology

    //As per https://developermemos.com/posts/checking-composable-render-preview
    // DI will not be accessible on preview mode, so we want to avoid attempting to use it there.
    val isPreview = LocalInspectionMode.current

    return if(isPreview) {
        emptyList()
    }else {
        val di = localDI()
        remember(termJsonStr) {
            courseTerminology.toTerminologyEntries(
                json = di.direct.instance(),
                systemImpl = null,
            )
        }
    }
}

/**
 * Get the correct string to use for a given CourseTerminology and StringResource. If the string is
 * set in the CourseTerminology the CourseTerminology will be used, otherwise the default
 * string will be used.
 *
 * @param terminologyEntries terminology entries e.g. as per rememberCourseTerminologyEntries
 * @param stringResource the StringResource to lookup e.g. MR.strings.teacher MR.strings.student etc.
 *
 */
@Composable
fun courseTerminologyEntryResource(
    terminologyEntries: List<TerminologyEntry>,
    stringResource: StringResource
): String {
    return terminologyEntries.firstOrNull { it.stringResource ==  stringResource }?.term
        ?: stringResource(resource = stringResource)
}
