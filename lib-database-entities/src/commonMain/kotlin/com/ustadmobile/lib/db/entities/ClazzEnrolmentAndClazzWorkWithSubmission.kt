package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWorkSubmission and Person
 */
@Serializable
class ClazzEnrolmentAndClazzWorkWithSubmission : ClazzEnrolment() {

    @Embedded
    var clazzWork: ClazzWork? = null

    @Embedded
    var submission: ClazzWorkSubmission? = null

    @Embedded
    var person: Person ? = null

}
