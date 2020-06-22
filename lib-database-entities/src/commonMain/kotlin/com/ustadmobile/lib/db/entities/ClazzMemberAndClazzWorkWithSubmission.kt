package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWorkSubmission and Person
 */
@Serializable
class ClazzMemberAndClazzWorkWithSubmission : ClazzMember() {

    @Embedded
    var clazzWork : ClazzWorkWithMetrics? = null

    @Embedded
    var clazzWorkSubmission: ClazzWorkSubmission? = null

    @Embedded
    var clazzWorkSubmissionPerson: Person ? = null
}
