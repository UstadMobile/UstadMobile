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
}
