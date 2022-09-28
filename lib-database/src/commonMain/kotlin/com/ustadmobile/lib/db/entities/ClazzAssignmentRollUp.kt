package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ustadmobile.lib.db.entities.StatementEntity.Companion.RESULT_UNSET
import kotlinx.serialization.Serializable

@Entity(indices = [Index(value = ["cachePersonUid", "cacheContentEntryUid","cacheClazzAssignmentUid"],
        unique = true)])
@Serializable
class ClazzAssignmentRollUp {

    @PrimaryKey(autoGenerate = true)
    var cacheUid: Long = 0

    var cachePersonUid: Long = 0

    var cacheContentEntryUid: Long = 0

    var cacheClazzAssignmentUid: Long = 0

    var cacheStudentScore: Int = 0

    var cacheMaxScore: Int = 0

    @ColumnInfo(defaultValue = "0")
    var cacheFinalWeightScoreWithPenalty: Float = 0f

    @ColumnInfo(defaultValue = "0")
    var cacheWeight: Int = 0

    var cacheProgress: Int = 0

    var cacheContentComplete: Boolean = false

    var cacheSuccess: Byte = RESULT_UNSET

    var cachePenalty: Int = 0

    var lastCsnChecked: Long = 0

}