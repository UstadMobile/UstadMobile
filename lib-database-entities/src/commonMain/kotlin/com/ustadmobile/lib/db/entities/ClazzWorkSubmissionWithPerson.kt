package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWorkSubmission and Person
 */
@Serializable
class ClazzWorkSubmissionWithPerson : ClazzWorkSubmission() {
    @Embedded
    var clazzWorkSubmissionPerson: Person ? = null
}
