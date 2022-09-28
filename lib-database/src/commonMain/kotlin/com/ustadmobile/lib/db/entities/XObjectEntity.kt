package com.ustadmobile.lib.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import kotlinx.serialization.Serializable

@Entity
@Serializable
@ReplicateEntity(tableId =  XObjectEntity.TABLE_ID, tracker = XObjectEntityReplicate::class)
@Triggers(arrayOf(
 Trigger(
     name = "xobjectentity_remote_insert",
     order = Trigger.Order.INSTEAD_OF,
     on = Trigger.On.RECEIVEVIEW,
     events = [Trigger.Event.INSERT],
     sqlStatements = [
         """REPLACE INTO XObjectEntity(xObjectUid, objectType, objectId, definitionType, interactionType, correctResponsePattern, objectContentEntryUid, objectStatementRefUid, xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum, xObjectLastChangedBy, xObjectLct) 
         VALUES (NEW.xObjectUid, NEW.objectType, NEW.objectId, NEW.definitionType, NEW.interactionType, NEW.correctResponsePattern, NEW.objectContentEntryUid, NEW.objectStatementRefUid, NEW.xObjectMasterChangeSeqNum, NEW.xObjectocalChangeSeqNum, NEW.xObjectLastChangedBy, NEW.xObjectLct) 
         /*psql ON CONFLICT (xObjectUid) DO UPDATE 
         SET objectType = EXCLUDED.objectType, objectId = EXCLUDED.objectId, definitionType = EXCLUDED.definitionType, interactionType = EXCLUDED.interactionType, correctResponsePattern = EXCLUDED.correctResponsePattern, objectContentEntryUid = EXCLUDED.objectContentEntryUid,objectStatementRefUid =  EXCLUDED.objectStatementRefUid, xObjectMasterChangeSeqNum = EXCLUDED.xObjectMasterChangeSeqNum, xObjectocalChangeSeqNum = EXCLUDED.xObjectocalChangeSeqNum, xObjectLastChangedBy = EXCLUDED.xObjectLastChangedBy, xObjectLct = EXCLUDED.xObjectLct
         */"""
     ]
 )
))
class XObjectEntity {

    @PrimaryKey(autoGenerate = true)
    var xObjectUid: Long = 0

    var objectType: String? = null

    var objectId: String? = null

    var definitionType: String? = null

    var interactionType: String? = null

    var correctResponsePattern: String? = null

    var objectContentEntryUid: Long = 0

    @ColumnInfo(defaultValue = "0")
    var objectStatementRefUid: Long = 0

    @MasterChangeSeqNum
    var xObjectMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var xObjectocalChangeSeqNum: Long = 0

    @LastChangedBy
    var xObjectLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var xObjectLct: Long = 0

    constructor() {

    }

    constructor(id: String?, objectType: String?, type: String?, interactionType: String?, responsePattern: String?, objectContentEntryUid: Long = 0, statementRefUid: Long = 0) {
        this.objectId = id
        this.objectType = objectType
        this.definitionType = type
        this.interactionType = interactionType
        this.correctResponsePattern = responsePattern
        this.objectContentEntryUid = objectContentEntryUid
        this.objectStatementRefUid = statementRefUid
    }


    override fun hashCode(): Int {
        var result = (xObjectUid xor xObjectUid.ushr(32)).toInt()
        result = 31 * result + (objectType?.hashCode() ?: 0)
        result = 31 * result + (objectId?.hashCode() ?: 0)
        result = 31 * result + (definitionType?.hashCode() ?: 0)
        result = 31 * result + (interactionType?.hashCode() ?: 0)
        result = 31 * result + (correctResponsePattern?.hashCode() ?: 0)
        result = 31 * result + objectContentEntryUid.hashCode()
        result = 31 * result + objectStatementRefUid.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as XObjectEntity

        if (xObjectUid != other.xObjectUid) return false
        if (objectType != other.objectType) return false
        if (objectId != other.objectId) return false
        if (definitionType != other.definitionType) return false
        if (interactionType != other.interactionType) return false
        if (correctResponsePattern != other.correctResponsePattern) return false
        if (objectContentEntryUid != other.objectContentEntryUid) return false
        if(objectStatementRefUid != other.objectStatementRefUid) return false

        return true
    }

    companion object {

        const val TABLE_ID = 64
    }
}

