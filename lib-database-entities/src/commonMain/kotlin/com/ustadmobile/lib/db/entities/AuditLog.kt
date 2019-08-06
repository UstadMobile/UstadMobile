
package com.ustadmobile.lib.db.entities


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.annotation.SyncablePrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum

@UmEntity(tableId = 53)
@Entity
open class AuditLog() {

    @SyncablePrimaryKey
    @PrimaryKey(autoGenerate = true)
    var auditLogUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var auditLogMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var auditLogLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
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
