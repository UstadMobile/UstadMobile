package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(indices = [Index(value = ["cachePersonUid", "cacheContentEntryUid","cacheClazzAssignmentUid"],
        unique = true)])
@Serializable
class CacheClazzAssignment {

    @PrimaryKey(autoGenerate = true)
    var cacheUid: Long = 0

    var cachePersonUid: Long = 0

    var cacheContentEntryUid: Long = 0

    var cacheClazzAssignmentUid: Long = 0

    var cacheStudentScore: Int = 0

    var cacheMaxScore: Int = 0

    var cacheProgress: Int = 0

    var cacheContentComplete: Boolean = false

    var lastCsnChecked: Long = 0

}