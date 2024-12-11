package com.ustadmobile.appconfigdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import kotlinx.serialization.Serializable

/**
 * @param lsiUid the XX64 hash of lsUrl
 * @param lsiUrl the full url e.g. https://subdomain.example.org/ . MUST end with a trailing slash
 */
@Serializable
@ReplicateEntity(
    tableId = LearningSpaceInfo.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)

@Triggers(arrayOf(
    Trigger(
        name = "learningspaceinfo_remote_insert",
        order = Trigger.Order.INSTEAD_OF,
        on = Trigger.On.RECEIVEVIEW,
        events = [Trigger.Event.INSERT],
        conditionSql = "SELECT %NEW_LAST_MODIFIED_GREATER_THAN_EXISTING%",
        sqlStatements = [ "%UPSERT%" ],
    )
))
@Entity
data class LearningSpaceInfo(
    @PrimaryKey
    var lsiUid: Long = 0,

    var lsiUrl: String = "",

    var lsiName: String = "",

    var lsiDescription: String = "",

    @ReplicateLastModified
    @ReplicateEtag
    var lsiLastModified: Long = 0,

    var lsiStored: Long = 0,
) {
    companion object {
        const val TABLE_ID = 2
    }
}
