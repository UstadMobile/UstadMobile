package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(tableId = AgentEntity.TABLE_ID, tracker = AgentEntityReplicate::class)
@Triggers(arrayOf(
 Trigger(name = "agententity_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         sqlStatements = [
         "REPLACE INTO AgentEntity(agentUid, agentMbox, agentMbox_sha1sum, agentOpenid, agentAccountName, agentHomePage, agentPersonUid, statementMasterChangeSeqNum, statementLocalChangeSeqNum, statementLastChangedBy, agentLct) VALUES (NEW.agentUid, NEW.agentMbox, NEW.agentMbox_sha1sum, NEW.agentOpenid, NEW.agentAccountName, NEW.agentHomePage, NEW.agentPersonUid, NEW.statementMasterChangeSeqNum, NEW.statementLocalChangeSeqNum, NEW.statementLastChangedBy, NEW.agentLct) " +
         "/*psql ON CONFLICT (agentUid) DO UPDATE SET agentMbox = EXCLUDED.agentMbox, agentMbox_sha1sum = EXCLUDED.agentMbox_sha1sum, agentOpenid = EXCLUDED.agentOpenid, agentAccountName = EXCLUDED.agentAccountName, agentHomePage = EXCLUDED.agentHomePage, agentPersonUid = EXCLUDED.agentPersonUid, statementMasterChangeSeqNum = EXCLUDED.statementMasterChangeSeqNum, statementLocalChangeSeqNum = EXCLUDED.statementLocalChangeSeqNum, statementLastChangedBy = EXCLUDED.statementLastChangedBy, agentLct = EXCLUDED.agentLct*/"
         ])
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

    @ReplicationVersionId
    @LastChangedTime
    var agentLct: Long = 0

    companion object {

        const val TABLE_ID = 68
    }
}
