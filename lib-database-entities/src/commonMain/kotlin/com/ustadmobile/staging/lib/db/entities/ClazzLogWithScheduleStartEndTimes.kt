package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ClazzLogWithScheduleStartEndTimes : ClazzLog() {

    //Start time
    var sceduleStartTime: Long = 0

    //End time
    var scheduleEndTime: Long = 0

    //Frequency
    var scheduleFrequency: Int = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ClazzLogWithScheduleStartEndTimes

        if (sceduleStartTime != other.sceduleStartTime) return false
        if (scheduleEndTime != other.scheduleEndTime) return false
        if (scheduleFrequency != other.scheduleFrequency) return false
        if (clazzLogDone != other.clazzLogDone) return false
        if (clazzLogNumAbsent != other.clazzLogNumAbsent) return false
        if (clazzLogNumPartial != other.clazzLogNumPartial) return false
        if (clazzLogNumPresent != other.clazzLogNumPresent) return false
        if (clazzLogUid != other.clazzLogUid) return false
        if (clazzLogCancelled != other.clazzLogCancelled) return false
        if (logDate != other.logDate) return false

        return true
    }

    override fun hashCode(): Int {
        var result = sceduleStartTime.hashCode()
        result = 31 * result + scheduleEndTime.hashCode()
        result = 31 * result + scheduleFrequency
        return result
    }


}
