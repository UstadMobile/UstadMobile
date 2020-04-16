
package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import kotlinx.serialization.Serializable


@SyncableEntity(tableId = 53)
@Entity
@Serializable
open class AuditLog() {

    @PrimaryKey(autoGenerate = true)
    var auditLogUid: Long = 0

    @MasterChangeSeqNum
    var auditLogMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var auditLogLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var auditLogLastChangedBy: Int = 0

    var auditLogActorPersonUid: Long = 0

    var auditLogTableUid: Int = 0

    var auditLogEntityUid: Long = 0

    var auditLogDate: Long = 0

    var notes: String? = null

    constructor(personUid: Long, table: Int, entityUid: Long): this() {
        this.auditLogActorPersonUid = personUid
        this.auditLogTableUid = table
        this.auditLogEntityUid = entityUid
        this.auditLogDate = 0
    }




}
