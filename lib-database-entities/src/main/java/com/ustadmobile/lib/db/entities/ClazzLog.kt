package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */
@UmEntity
@Entity
open class ClazzLog(
    @field:UmPrimaryKey(autoIncrement = true)
    @field:PrimaryKey(autoGenerate = true)
    var clazzLogUid: Long = 0,

    var clazzLogClazzUid: Long = 0,

    var logDate: Long = 0,

    var timeRecorded: Long = 0,

    var masterChangeSeqNum: Long = 0,

    var localChangeSeqNum: Long = 0
) {

    constructor(): this(clazzLogUid = 0)
}
