package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWorkSubmission and ClazzWork
 */
@Serializable
class ClazzWorkSubmissionWithClazzWork : ClazzWorkSubmission() {
    @Embedded
    var clazzWork: ClazzWork ? = null
}
