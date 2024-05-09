package com.ustadmobile.core.domain.interop.oneroster.model

import com.ustadmobile.core.domain.interop.timestamp.format8601Timestamp
import kotlinx.serialization.Serializable
import com.ustadmobile.lib.db.entities.Clazz as ClazzEntity

/**
 * As per:
 * https://www.imsglobal.org/oneroster-v11-final-specification#_Toc480452030
 * Section 4.3
 *
 * Represented in the database by the Clazz entity. In OneRoster there is a One:Many relationship
 * between "Course" and "Class" e.g. where a Course is a given set of learning materials/objectives
 * and a class is an occurence (e.g. a specific group of a students for a specific term etc). This
 * relationship is not currently implemented.
 */
@Serializable
data class Clazz(
    val sourcedId: String,
    val status: Status,
    val dateLastModified: String,
    val title: String,
)

fun ClazzEntity.toOneRosterClass() = Clazz(
    sourcedId = this.clazzUid.toString(),
    status = Status.ACTIVE,
    dateLastModified = format8601Timestamp(clazzLct),
    title = clazzName ?: ""
)
