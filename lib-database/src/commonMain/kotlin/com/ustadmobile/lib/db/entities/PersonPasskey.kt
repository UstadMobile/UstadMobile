package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.PersonPasskey.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(arrayOf(
    Trigger(
        name = "person_passkey",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
        sqlStatements = [TRIGGER_UPSERT],
    )
))
@Serializable
data class PersonPasskey(
    @PrimaryKey(autoGenerate = true)
    var personPasskeyUid: Long = 0,

    var personUid: Long = 0,

    var attestationObj: String? = null,

    var clientDataJson: String? = null,

    var originString: String? = null,

    var rpid: String? = null,

    var id: String? = null,

    var challengeString: String? = null,

    var publicKey: String? = null,

    var isRevoked: Int = NOT_REVOKED,

    @ReplicateLastModified
    @ReplicateEtag
    var passkeyLct: Long = 0

){
    companion object {

        const val TABLE_ID = 892
        const val NOT_REVOKED = 0
        const val REVOKED = 1

    }
}