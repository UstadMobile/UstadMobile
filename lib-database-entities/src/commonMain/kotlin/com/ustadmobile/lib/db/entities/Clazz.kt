package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.db.entities.Clazz.Companion.TABLE_ID


@Entity
@SyncableEntity(tableId = TABLE_ID)
open class Clazz() {

    @PrimaryKey(autoGenerate = true)
    var clazzUid: Long = 0

    var clazzName: String? = null

    var attendanceAverage: Float = 0.toFloat()

    @MasterChangeSeqNum
    var clazzMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var clazzLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzLastChangedBy: Int = 0

    var clazzLocationUid: Long = 0

    constructor(clazzName: String) : this() {
        this.clazzName = clazzName
    }

    constructor(clazzName: String, clazzLocationUid: Long) : this() {
        this.clazzName = clazzName
        this.clazzLocationUid = clazzLocationUid
    }

    companion object {

        const val TABLE_ID = 6
    }
}
