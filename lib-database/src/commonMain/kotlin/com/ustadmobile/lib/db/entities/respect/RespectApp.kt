package com.ustadmobile.lib.db.entities.respect

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.ReplicateLastModified
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_WHERE_NEWER
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import com.ustadmobile.lib.db.entities.respect.RespectApp.Companion.TABLE_ID
import kotlinx.serialization.Serializable

@Entity
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW
)
@Triggers(
    arrayOf(
        Trigger(
            name = "respectapp_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
            sqlStatements = [TRIGGER_UPSERT],
        )
    )
)
@Serializable
data class RespectApp(
    @PrimaryKey
    var raUid: Long = 0,
    @ReplicateLastModified
    @ReplicateEtag
    var raLastModified: Long = 0,
    var raAppId: String = "",
    var raName: String = "",
    var raDescription: String = "",
    var raIconUrl: String = "",
    var raLearningUnitsUrl: String = "",
    var raDefaultLaunchUri: String = "",
    var raAndroidPackageName: String = "",
) {

    companion object {

        const val TABLE_ID = 4201

    }

}
