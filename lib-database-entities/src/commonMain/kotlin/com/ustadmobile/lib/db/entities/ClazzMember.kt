package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.ClazzMember.Companion.TABLE_ID

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */
@UmEntity(tableId = TABLE_ID)
@Entity
open class ClazzMember()  {

    /**
     * The personUid field of the related Person entity
     *
     * @param clazzMemberUid
     */
    @PrimaryKey(autoGenerate = true)
    var clazzMemberUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var clazzMemberPersonUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var clazzMemberClazzUid: Long = 0

    var clazzMemberDateJoined: Long = 0

    var clazzMemberDateLeft: Long = 0

    var clazzMemberRole: Int = 0

    var clazzMemberAttendancePercentage: Float = 0.toFloat()

    var clazzMemberActive: Boolean = false

    @UmSyncLocalChangeSeqNum
    var clazzMemberLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var clazzMemberMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var clazzMemberLastChangedBy: Int = 0

    constructor(clazzUid: Long, personUid: Long) : this() {
        this.clazzMemberClazzUid = clazzUid
        this.clazzMemberPersonUid = personUid
        this.clazzMemberActive = true
    }


    constructor(clazzUid: Long, personUid: Long, role: Int):this() {
        this.clazzMemberClazzUid = clazzUid
        this.clazzMemberPersonUid = personUid
        this.clazzMemberRole = role
        this.clazzMemberActive = true
    }

    companion object {

        const val ROLE_STUDENT = 1

        const val ROLE_TEACHER = 2

        const val TABLE_ID = 13
    }
}
