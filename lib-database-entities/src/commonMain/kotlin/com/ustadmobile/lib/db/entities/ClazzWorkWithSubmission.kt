package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

/**
 * POJO representing ClazzWork and ClazzWorkSubmission
 */
@Serializable
class ClazzWorkWithSubmission : ClazzWork() {
    @Embedded
    var clazzWorkSubmission: ClazzWorkSubmission? = null
}
