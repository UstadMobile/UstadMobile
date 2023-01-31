package com.ustadmobile.core.impl.locale

import com.ustadmobile.core.controller.TerminologyKeys
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.encodeStringMapToString
import com.ustadmobile.lib.db.entities.CourseTerminology
import kotlinx.serialization.json.Json
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CourseTerminologyStringsTest {

    val json = Json { encodeDefaults = true }
    val systemImpl = mock<UstadMobileSystemImpl> {
        on { getString(any()) }.thenAnswer { "${it.arguments.first()}" }
    }

    @Test
    fun givenTerminologyStrings_whenKeyPresent_thenWillOverride() {
        val terminology = CourseTerminology().apply {
            ctTerminology = json.encodeStringMapToString(mapOf(
                TerminologyKeys.STUDENT_KEY to "course participant"
            ))
        }

        val terminologyStrings = CourseTerminologyStrings(terminology, systemImpl, json)

        assertEquals("course participant", terminologyStrings[MessageID.student],
            "Terminology string will be overriden if present")
    }

    @Test
    fun givenEmptyTerminologyStrings_whenKeyNotPresent_thenWillBeNull() {
        val terminologyStrings = CourseTerminologyStrings(emptyList())

        assertNull(terminologyStrings[MessageID.student])
    }

}