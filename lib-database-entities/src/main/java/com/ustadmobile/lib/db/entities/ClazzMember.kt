package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmIndexField
import com.ustadmobile.lib.database.annotation.UmPrimaryKey
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

/**
 * This class mediates the relationship between a person and a clazz. A member can be a teacher,
 * or a student. Each member has a joining date, and a leaving date.
 */
@UmEntity(tableId = 11)
@Entity
open class ClazzMember(
        /**
         * The personUid field of the related Person entity
         *
         * @param clazzMemberUid
         */
        @field:UmPrimaryKey(autoGenerateSyncable = true)
        @field:PrimaryKey
        var clazzMemberUid: Long = 0,

        @field:UmIndexField
        @field:ColumnInfo(index = true)
        var clazzMemberPersonUid: Long = 0,

        @field:UmIndexField
        @field:ColumnInfo(index = true)
        var clazzMemberClazzUid: Long = 0,

        var dateJoined: Long = 0,

        var dateLeft: Long = 0,

        var role: Int = 0,

        @field:UmSyncLocalChangeSeqNum
        var clazzMemberLocalChangeSeqNum: Long = 0,

        @field:UmSyncMasterChangeSeqNum
        var clazzMemberMasterChangeSeqNum: Long = 0,

        @field:UmSyncLastChangedBy
        var clazzMemberLastChangedBy: Int = 0
){

    constructor(): this(clazzMemberUid = 0)

    constructor(clazzUid: Long, personUid: Long): this(clazzMemberUid = 0,
            clazzMemberClazzUid = clazzUid, clazzMemberPersonUid = personUid)

    companion object {

        const val ROLE_STUDENT = 1

        const val ROLE_TEACHER = 2
    }
}
