package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.VerbEntity.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Serializable
class VerbDisplay {

    var verbUid: Long = 0
    var urlId: String? = null
    var display: String? = null
}

@Entity
//@SyncableEntity(tableId = TABLE_ID,
//    notifyOnUpdate = ["""
//        SELECT DISTINCT UserSession.usClientNodeId AS deviceId, ${VerbEntity.TABLE_ID} AS tableId
//        FROM UserSession"""])
@Serializable
@ReplicateEntity(tableId = TABLE_ID, tracker = VerbEntityTracker::class)
class VerbEntity() {

    constructor(uid: Long, url: String?) : this(){
        verbUid = uid
        urlId = url
    }

    @PrimaryKey(autoGenerate = true)
    var verbUid: Long = 0

    var urlId: String? = null

    var verbInActive: Boolean = false

    @MasterChangeSeqNum
    var verbMasterChangeSeqNum: Long = 0

    @LocalChangeSeqNum
    var verbLocalChangeSeqNum: Long = 0

    @LastChangedBy
    var verbLastChangedBy: Int = 0

    @LastChangedTime
    @ReplicationVersionId
    var verbLct: Long = 0

    companion object {

        const val TABLE_ID = 62

        const val VERB_PROGRESSED_URL = "http://adlnet.gov/expapi/verbs/progressed"

        const val VERB_PROGRESSED_UID = 10000L

        const val VERB_COMPLETED_URL = "http://adlnet.gov/expapi/verbs/completed"

        const val VERB_COMPLETED_UID = 10001L

        const val VERB_PASSED_URL = "http://adlnet.gov/expapi/verbs/passed"

        const val VERB_PASSED_UID = 10002L

        const val VERB_FAILED_URL = "http://adlnet.gov/expapi/verbs/failed"

        const val VERB_FAILED_UID = 10003L

        const val VERB_SATISFIED_URL = "https://w3id.org/xapi/adl/verbs/satisfied"

        const val VERB_SATISFIED_UID = 10004L

        const val VERB_ATTEMPTED_URL = "http://adlnet.gov/expapi/verbs/attempted"

        const val VERB_ATTEMPTED_UID = 10005L

        const val VERB_INTERACTED_URL = "http://adlnet.gov/expapi/verbs/interacted"

        const val VERB_INTERACTED_UID = 10006L

        const val VERB_ANSWERED_URL = "http://adlnet.gov/expapi/verbs/answered"

        const val VERB_ANSWERED_UID = 10007L

        val FIXED_UIDS = mapOf(VERB_PROGRESSED_URL to VERB_PROGRESSED_UID,
                VERB_COMPLETED_URL to VERB_COMPLETED_UID,
                VERB_PASSED_URL to VERB_PASSED_UID,
                VERB_FAILED_URL to VERB_FAILED_UID,
                VERB_SATISFIED_URL to VERB_SATISFIED_UID,
                VERB_ANSWERED_URL to VERB_ANSWERED_UID,
                VERB_ATTEMPTED_URL to VERB_ATTEMPTED_UID,
                VERB_INTERACTED_URL to VERB_INTERACTED_UID)

    }

}
