package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_ETAG_NOT_EQUALS
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT
import kotlinx.serialization.Serializable

/**
 * StatementEntityJson is split into a separate entity so that the statement data on StatementEntity
 * we often need (e.g. score, duration, etc) can be retrieved without needing to retrieve the
 * Statement's full JSON string.
 *
 * E.g. when running the course progress query, we might need to retrieve a few statements per
 * courseBlock per student, retrieving the data for 50 students could entail fetching 2000
 * statements.
 *
 * @param fullStatement the JSON for the statement as per the 'exact' representation spec of xAPI
 *        as per statement immutability rules :
 *        https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#231-statement-immutability.
 *        The canonical JSON will be generated based on the entity fields.
 */
@Entity(
    primaryKeys = ["stmtJsonIdHi", "stmtJsonIdLo"]
)
@Serializable
@ReplicateEntity(
    tableId = StatementEntityJson.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "statemententityjson_remote_insert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_ETAG_NOT_EQUALS,
            sqlStatements = [TRIGGER_UPSERT],
        )
    )
)
data class StatementEntityJson(
    var stmtJsonIdHi: Long = 0,
    var stmtJsonIdLo: Long = 0,
    @ReplicateEtag
    var stmtEtag: Long = 1,
    var fullStatement: String? = null,
) {
    companion object {

        const val TABLE_ID = 602

    }
}
