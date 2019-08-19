package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */
@Entity
@SyncableEntity(tableId = 11)
class ClazzMember()  {

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

    var dateJoined: Long = 0

    var dateLeft: Long = 0

    var role: Int = 0

    @LocalChangeSeqNum
    var clazzMemberLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var clazzMemberMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var clazzMemberLastChangedBy: Int = 0

    constructor(clazzUid: Long, personUid: Long) : this() {
        this.clazzMemberClazzUid = clazzUid
        this.clazzMemberPersonUid = personUid
    }

    companion object {

        const val ROLE_STUDENT = 1

        const val ROLE_TEACHER = 2
    }
}
