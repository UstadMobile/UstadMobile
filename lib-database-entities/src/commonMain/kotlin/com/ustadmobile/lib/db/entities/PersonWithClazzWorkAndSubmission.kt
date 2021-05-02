package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWorkSubmission and Person
 */
@Serializable
class PersonWithClazzWorkAndSubmission : Person() {

    @Embedded
    var clazzWork: ClazzWork? = null

    @Embedded
    var submission: ClazzWorkSubmission? = null

}
