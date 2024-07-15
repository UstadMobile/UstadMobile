package com.ustadmobile.lib.db.entities.xapi

import androidx.room.Entity
import com.ustadmobile.door.annotation.ReplicateEntity
import com.ustadmobile.door.annotation.ReplicateEtag
import com.ustadmobile.door.annotation.Trigger
import com.ustadmobile.door.annotation.Triggers
import com.ustadmobile.lib.db.entities.TRIGGER_CONDITION_ETAG_NOT_EQUALS
import com.ustadmobile.lib.db.entities.TRIGGER_UPSERT

@ReplicateEntity(
    tableId = StatementContextActivityJoin.TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Triggers(
    arrayOf(
        Trigger(
            name = "statementcontextactivityjoin_remoteinsert",
            order = Trigger.Order.INSTEAD_OF,
            on = Trigger.On.RECEIVEVIEW,
            events = [Trigger.Event.INSERT],
            conditionSql = TRIGGER_CONDITION_ETAG_NOT_EQUALS,
            sqlStatements = [ TRIGGER_UPSERT ],
        )
    )
)
@Entity(
    primaryKeys = arrayOf("scajFromStatementIdHi", "scajFromStatementIdLo", "scajToHash")
)
/**
 *
 *
 * @param scajFromStatementIdHi the most significant bits of the statement uuid
 * @param scajFromStatementIdLo the least significant bits of the statement uuid
 * @param scajToHash Hash of "$scajContextType-$scajToActivityId" e.g. generates a hash that is unique
 * in the context of the statement
 * @param scajContextType Integer flag based on the contextActivity property e.g. parent, grouping,
 * category, or other
 * @param scajToActivityId the IRI id of the activity that is being referenced
 * @param scajToActivityUid for key that joins to the activity (ActivityEntity.activityUid)
 * @param scajEtag a constant etag - always simply 1, because a statement is immutable.
 */
data class StatementContextActivityJoin(
    var scajFromStatementIdHi: Long = 0,
    var scajFromStatementIdLo: Long = 0,
    var scajToHash: Long = 0,
    var scajContextType: Int = 0,
    var scajToActivityUid: Long = 0,
    var scajToActivityId: String? = null,
    @ReplicateEtag
    var scajEtag: Long = 1,
) {
    companion object {

        const val TYPE_PARENT = 1

        const val TYPE_GROUPING = 2

        const val TYPE_CATEGORY = 3

        const val TYPE_OTHER = 4

        const val TABLE_ID = 44044

    }
}