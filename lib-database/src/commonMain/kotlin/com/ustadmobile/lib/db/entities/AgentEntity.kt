package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = AgentEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(arrayOf(
 Trigger(name = "agententity_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [TRIGGER_UPSERT_WHERE_NEWER]
 )
 )
)
@Serializable
class AgentEntity {

    @PrimaryKey(autoGenerate = true)
    var agentUid: Long = 0

    var agentMbox: String? = null

    var agentMbox_sha1sum: String? = null

    var agentOpenid: String? = null

    var agentAccountName: String? = null

    var agentHomePage: String? = null

    var agentPersonUid: Long = 0

    @MasterChangeSeqNum
    var statementMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var statementLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var statementLastChangedBy: Int = 0

    @ReplicateEtag
    @ReplicateLastModified
    var agentLct: Long = 0

    companion object {

        const val TABLE_ID = 68
    }
}
