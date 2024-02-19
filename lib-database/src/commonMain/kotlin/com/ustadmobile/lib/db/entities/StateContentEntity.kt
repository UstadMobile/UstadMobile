package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.StateContentEntity.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(
    tableId = StateContentEntity.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
 Trigger(
     name = "statecontententity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
     sqlStatements = [ TRIGGER_UPSERT ]
 )
))
//TODO
class StateContentEntity {

    @PrimaryKey(autoGenerate = true)
    var stateContentUid: Long = 0

    var stateContentStateUid: Long = 0

    var stateContentKey: String? = null

    var stateContentValue: String? = null

    var isIsactive: Boolean = false

    @MasterChangeSeqNum
    var stateContentMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var stateContentLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var stateContentLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var stateContentLct: Long = 0

    constructor(key: String, stateUid: Long, valueOf: String, isActive: Boolean) {
        this.stateContentKey = key
        this.stateContentValue = valueOf
        this.stateContentStateUid = stateUid
        this.isIsactive = isActive
    }

    constructor() {

    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || this::class != o::class) return false

        val that = o as StateContentEntity?

        if (stateContentUid != that!!.stateContentUid) return false
        if (stateContentStateUid != that.stateContentStateUid) return false
        if (isIsactive != that.isIsactive) return false
        return if (stateContentKey != that.stateContentKey) false else stateContentValue == that.stateContentValue
    }

    override fun hashCode(): Int {
        var result = (stateContentUid xor stateContentUid.ushr(32)).toInt()
        result = 31 * result + (stateContentStateUid xor stateContentStateUid.ushr(32)).toInt()
        result = 31 * result + if (stateContentKey != null) stateContentKey!!.hashCode() else 0
        result = 31 * result + if (stateContentValue != null) stateContentValue!!.hashCode() else 0
        result = 31 * result + if (isIsactive) 1 else 0
        return result
    }

    companion object {

        const val TABLE_ID = 72
    }
}