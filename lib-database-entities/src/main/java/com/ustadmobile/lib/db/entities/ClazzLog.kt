package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

/**
 * Represents one session (e.g. day) in the class log book. This is related to attendance records, but
 * could also be related to behavior logs etc. in the future.
 */
@UmEntity
class ClazzLog : SyncableEntity {

    @UmPrimaryKey(autoIncrement = true)
    var clazzLogUid: Long = 0

    var clazzLogClazzUid: Long = 0

    var logDate: Long = 0

    var timeRecorded: Long = 0

    override var masterChangeSeqNum: Long = 0

    override var localChangeSeqNum: Long = 0
}
