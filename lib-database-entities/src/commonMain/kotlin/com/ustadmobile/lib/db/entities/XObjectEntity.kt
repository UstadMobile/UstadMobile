package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.XObjectEntity.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@SyncableEntity(tableId = TABLE_ID)
@Serializable
class XObjectEntity {

    @PrimaryKey(autoGenerate = true)
    var xObjectUid: Long = 0

    var objectType: String? = null

    var objectId: String? = null

    var definitionType: String? = null

    var interactionType: String? = null

    var correctResponsePattern: String? = null

    var objectContentEntryUid: Long = 0

    @MasterChangeSeqNum
    var xObjectMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var xObjectocalChangeSeqNum: Long = 0

    @LastChangedBy
    var xObjectLastChangedBy: Int = 0

    constructor() {

    }

    constructor(id: String?, objectType: String?, type: String?, interactionType: String?, responsePattern: String?, objectContentEntryUid: Long = 0) {
        this.objectId = id
        this.objectType = objectType
        this.definitionType = type
        this.interactionType = interactionType
        this.correctResponsePattern = responsePattern
        this.objectContentEntryUid = objectContentEntryUid
    }


    override fun hashCode(): Int {
        var result = (xObjectUid xor xObjectUid.ushr(32)).toInt()
        result = 31 * result + (objectType?.hashCode() ?: 0)
        result = 31 * result + (objectId?.hashCode() ?: 0)
        result = 31 * result + (definitionType?.hashCode() ?: 0)
        result = 31 * result + (interactionType?.hashCode() ?: 0)
        result = 31 * result + (correctResponsePattern?.hashCode() ?: 0)
        result = 31 * result + objectContentEntryUid.hashCode()
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

        return true
    }

    companion object {

        const val TABLE_ID = 64
    }
}

