package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.AgentEntity.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
class AgentEntity {

    @UmPrimaryKey(autoGenerateSyncable = true)
    var agentUid: Long = 0

    var agentMbox: String? = null

    var agentMbox_sha1sum: String? = null

    var agentOpenid: String? = null

    var agentAccountName: String? = null

    var agentHomePage: String? = null

    var agentPersonUid: Long = 0

    @UmSyncMasterChangeSeqNum
    var statementMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var statementLocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var statementLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 68
    }
}
