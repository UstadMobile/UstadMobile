package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.LastChangedBy
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.lib.database.annotation.*
import com.ustadmobile.lib.db.entities.VerbEntity.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Serializable
class VerbDisplay {

    var verbUid: Long = 0
    var urlId: String? = null
    var display: String? = null
}

@Entity
@SyncableEntity(tableId = TABLE_ID,
    notifyOnUpdate = ["""
        SELECT DISTINCT DeviceSession.dsDeviceId AS deviceId, ${VerbEntity.TABLE_ID} AS tableId
        FROM DeviceSession """])
@Serializable
class VerbEntity() {

    constructor(uid: Long, url: String?) : this(){
        verbUid = uid
        urlId = url
    }

    @PrimaryKey(autoGenerate = true)
    var verbUid: Long = 0

    var urlId: String? = null


    @MasterChangeSeqNum
    var verbMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var verbLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var verbLastChangedBy: Int = 0

    companion object {

        const val TABLE_ID = 62

        const val VERB_PROGRESSED_URL = "http://adlnet.gov/expapi/verbs/progressed"

        const val VERB_PROGRESSED_UID = 10000L

        const val VERB_COMPLETED_URL = "http://adlnet.gov/expapi/verbs/completed"

        const val VERB_COMPLETED_UID = 10001L

        val FIXED_UIDS = mapOf(VERB_PROGRESSED_URL to VERB_PROGRESSED_UID,
                VERB_COMPLETED_URL to VERB_COMPLETED_UID)

    }

}
