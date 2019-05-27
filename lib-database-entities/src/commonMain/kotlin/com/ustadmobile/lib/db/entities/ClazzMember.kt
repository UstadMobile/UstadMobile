package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */
@UmEntity(tableId = 11)
@Entity
class ClazzMember() : SyncableEntity {

    /**
     * The personUid field of the related Person entity
     *
     * @param clazzMemberUid
     */
    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var clazzMemberUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var clazzMemberPersonUid: Long = 0

    @UmIndexField
    @ColumnInfo(index = true)
    var clazzMemberClazzUid: Long = 0

    var dateJoined: Long = 0

    var dateLeft: Long = 0

    var role: Int = 0

    override var masterChangeSeqNum: Long = 0

    override var localChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var clazzMemberLocalChangeSeqNum: Long = 0

    @UmSyncMasterChangeSeqNum
    var clazzMemberMasterChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
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
