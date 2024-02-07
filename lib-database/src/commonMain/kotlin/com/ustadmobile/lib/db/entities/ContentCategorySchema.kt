package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.door.annotation.*
import com.ustadmobile.lib.db.entities.ContentCategorySchema.Companion.TABLE_ID
import kotlinx.serialization.Serializable


/**
 * Represents a schema (list) of categories.
 */
@Entity
@Triggers(arrayOf(
     Trigger(
         name = "contentcategoryschema_remote_insert",
         order = Trigger.Order.INSTEAD_OF,
         on = Trigger.On.RECEIVEVIEW,
         events = [Trigger.Event.INSERT],
         conditionSql = TRIGGER_CONDITION_WHERE_NEWER,
         sqlStatements = [ TRIGGER_UPSERT ]
     )
 ))
@ReplicateEntity(
    tableId = TABLE_ID,
    remoteInsertStrategy = ReplicateEntity.RemoteInsertStrategy.INSERT_INTO_RECEIVE_VIEW,
)
@Serializable
class ContentCategorySchema() {

    @PrimaryKey(autoGenerate = true)
    var contentCategorySchemaUid: Long = 0

    var schemaName: String? = null

    var schemaUrl: String? = null

    @LocalChangeSeqNum
    var contentCategorySchemaLocalChangeSeqNum: Long = 0

    @MasterChangeSeqNum
    var contentCategorySchemaMasterChangeSeqNum: Long = 0

    @LastChangedBy
    var contentCategorySchemaLastChangedBy: Int = 0

    @ReplicateLastModified
    @ReplicateEtag
    var contentCategorySchemaLct: Long = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        val schema = other as ContentCategorySchema?

        if (contentCategorySchemaUid != schema!!.contentCategorySchemaUid) return false
        if (if (schemaName != null) schemaName != schema.schemaName else schema.schemaName != null)
            return false
        return if (schemaUrl != null) schemaUrl == schema.schemaUrl else schema.schemaUrl == null
    }

    override fun hashCode(): Int {
        var result = (contentCategorySchemaUid xor contentCategorySchemaUid.ushr(32)).toInt()
        result = 31 * result + if (schemaName != null) schemaName!!.hashCode() else 0
        result = 31 * result + if (schemaUrl != null) schemaUrl!!.hashCode() else 0
        return result
    }

    companion object {

        const val TABLE_ID = 2
    }
}
