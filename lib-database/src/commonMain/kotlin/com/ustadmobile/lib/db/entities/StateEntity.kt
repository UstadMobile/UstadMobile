package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId = StateEntity.TABLE_ID, tracker = StateEntityReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "stateentity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO StateEntity(stateUid, stateId, agentUid, activityId, registration, isIsactive, timestamp, stateMasterChangeSeqNum, stateLocalChangeSeqNum, stateLastChangedBy, stateLct) 
         VALUES (NEW.stateUid, NEW.stateId, NEW.agentUid, NEW.activityId, NEW.registration, NEW.isIsactive, NEW.timestamp, NEW.stateMasterChangeSeqNum, NEW.stateLocalChangeSeqNum, NEW.stateLastChangedBy, NEW.stateLct) 
         /*psql ON CONFLICT (stateUid) DO UPDATE 
         SET stateId = EXCLUDED.stateId, agentUid = EXCLUDED.agentUid, activityId = EXCLUDED.activityId, registration = EXCLUDED.registration, isIsactive = EXCLUDED.isIsactive, timestamp = EXCLUDED.timestamp, stateMasterChangeSeqNum = EXCLUDED.stateMasterChangeSeqNum, stateLocalChangeSeqNum = EXCLUDED.stateLocalChangeSeqNum, stateLastChangedBy = EXCLUDED.stateLastChangedBy, stateLct = EXCLUDED.stateLct
         */"""
     ]
 )
))
class StateEntity() {

    @PrimaryKey(autoGenerate = true)
    var stateUid: Long = 0

    var stateId: String? = null

    var agentUid: Long = 0

    var activityId: String? = null

    var registration: String? = null

    var isIsactive: Boolean = false

    var timestamp: Long = 0

    @MasterChangeSeqNum
    var stateMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var stateLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var stateLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var stateLct: Long = 0

    constructor(activityId: String?, agentUid: Long, registration: String?, stateId: String?, isActive: Boolean, timestamp: Long) : this(){
        this.activityId = activityId
        this.agentUid = agentUid
        this.registration = registration
        this.isIsactive = isActive
        this.stateId = stateId
        this.timestamp = timestamp
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        val that = other as StateEntity?

        if (stateUid != that!!.stateUid) return false
        if (agentUid != that.agentUid) return false
        if (isIsactive != that.isIsactive) return false
        if (stateId != that.stateId) return false
        return if (activityId != that.activityId) false else registration == that.registration
    }

    override fun hashCode(): Int {
        var result = (stateUid xor stateUid.ushr(32)).toInt()
        result = 31 * result + if (stateId != null) stateId!!.hashCode() else 0
        result = 31 * result + (agentUid xor agentUid.ushr(32)).toInt()
        result = 31 * result + if (activityId != null) activityId!!.hashCode() else 0
        result = 31 * result + if (registration != null) registration!!.hashCode() else 0
        result = 31 * result + if (isIsactive) 1 else 0
        return result
    }

    companion object {

        const val TABLE_ID = 70
    }
}
