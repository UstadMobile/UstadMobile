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
@SyncableEntity(tableId = 200)
@Serializable
open class SchoolMember {

    @PrimaryKey(autoGenerate = true)
    var schoolMemberUid: Long = 0

    @ColumnInfo(index = true)
    var schoolMemberPersonUid: Long = 0

    @ColumnInfo(index = true)
    var schoolMemberSchoolUid: Long = 0

    var schoolMemberJoinDate : Long = 0

    var schoolMemberLeftDate : Long = 0

    var schoolMemberRole: Int = 0

    var schoolMemberActive: Boolean = true

    @LocalChangeSeqNum
    var schoolMemberLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var schoolMemberMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var schoolMemberLastChangedBy: Int = 0


    constructor(){
        schoolMemberActive = true
        schoolMemberLeftDate = Long.MAX_VALUE
    }

    companion object {


    }
}
