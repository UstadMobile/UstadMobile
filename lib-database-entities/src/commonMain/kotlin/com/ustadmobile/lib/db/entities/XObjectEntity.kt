package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.XObjectEntity.Companion.TABLE_ID

@UmEntity(tableId = TABLE_ID)
@Entity
class XObjectEntity {

    @UmPrimaryKey(autoGenerateSyncable = true)
    @PrimaryKey
    var xObjectUid: Long = 0

    var objectType: String? = null

    var objectId: String? = null

    var definitionType: String? = null

    var interactionType: String? = null

    var correctResponsePattern: String? = null

    @UmSyncMasterChangeSeqNum
    var xObjectMasterChangeSeqNum: Long = 0

    @UmSyncLocalChangeSeqNum
    var xObjectocalChangeSeqNum: Long = 0

    @UmSyncLastChangedBy
    var xObjectLastChangedBy: Int = 0

    constructor() {

    }

    constructor(id: String?, objectType: String?, type: String?, interactionType: String?, responsePattern: String?) {
        this.objectId = id
        this.objectType = objectType
        this.definitionType = type
        this.interactionType = interactionType
        this.correctResponsePattern = responsePattern
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || this::class != o::class) return false

        val that = o as XObjectEntity?

        if (xObjectUid != that!!.xObjectUid) return false
        if (if (objectType != null) objectType != that.objectType else that.objectType != null)
            return false
        if (if (objectId != null) objectId != that.objectId else that.objectId != null)
            return false
        if (if (definitionType != null) definitionType != that.definitionType else that.definitionType != null)
            return false
        if (if (interactionType != null) interactionType != that.interactionType else that.interactionType != null)
            return false
        return if (correctResponsePattern != null) correctResponsePattern == that.correctResponsePattern else that.correctResponsePattern == null
    }

    override fun hashCode(): Int {
        var result = (xObjectUid xor xObjectUid.ushr(32)).toInt()
        result = 31 * result + if (objectType != null) objectType!!.hashCode() else 0
        result = 31 * result + if (objectId != null) objectId!!.hashCode() else 0
        result = 31 * result + if (definitionType != null) definitionType!!.hashCode() else 0
        result = 31 * result + if (interactionType != null) interactionType!!.hashCode() else 0
        result = 31 * result + if (correctResponsePattern != null) correctResponsePattern!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 64
    }
}

