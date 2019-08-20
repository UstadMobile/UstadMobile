package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */
@Entity
class ClazzLog()  {

    @PrimaryKey(autoGenerate = true)
    var clazzLogUid: Long = 0

    var clazzLogClazzUid: Long = 0

    var logDate: Long = 0

    var timeRecorded: Long = 0

}
