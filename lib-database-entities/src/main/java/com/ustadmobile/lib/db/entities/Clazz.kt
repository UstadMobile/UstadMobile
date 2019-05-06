package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum
import com.ustadmobile.lib.db.entities.Clazz.Companion.TABLE_ID


@UmEntity(tableId = TABLE_ID)
@Entity
open class Clazz(
        @field:PrimaryKey @field:UmPrimaryKey(autoGenerateSyncable = true) var clazzUid: Long = 0,
        var clazzName: String? = null,
        var attendanceAverage: Float = 0.toFloat(),
        @field:UmSyncMasterChangeSeqNum var clazzMasterChangeSeqNum: Long = 0,
        @field:UmSyncLocalChangeSeqNum var clazzLocalChangeSeqNum: Long = 0,
        @UmSyncLastChangedBy var clazzLastChangedBy: Int = 0,
        var clazzLocationUid: Long = 0
) {

    constructor() : this(clazzUid = 0)

    constructor(clazzName: String): this(clazzUid = 0, clazzName = clazzName)

    constructor(clazzName: String, clazzLocationUid: Long): this(clazzUid = 0,
            clazzName = clazzName, clazzLocationUid =  clazzLocationUid)

    companion object {
        const val TABLE_ID = 6
    }
}
