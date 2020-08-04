package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */

@Entity
@SyncableEntity(tableId = 65)
@Serializable
open class ClazzMember()  {

    /**
     * The personUid field of the related Person entity
     *
     * @param clazzMemberUid
     */
    @PrimaryKey(autoGenerate = true)
    var clazzMemberUid: Long = 0

    @ColumnInfo(index = true)
    var clazzMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var clazzMemberClazzUid: Long = 0

    var clazzMemberDateJoined: Long = 0

    /**
     * The date the student left this class (e.g. graduated or un-enrolled).
     * Long.MAX_VALUE = no leaving date (e.g. ongoing registration)
     */
    var clazzMemberDateLeft: Long = Long.MAX_VALUE

    var clazzMemberRole: Int = 0

    var clazzMemberAttendancePercentage: Float = 0.toFloat()

    var clazzMemberActive: Boolean = false

    @LocalChangeSeqNum
    var clazzMemberLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzMemberMasterChangeSeqNum: Long = 0

    @LastChangedBy
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

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

    companion object {

        const val ROLE_STUDENT = 1000

        const val ROLE_TEACHER = 1001

        const val TABLE_ID = 13
    }
}
