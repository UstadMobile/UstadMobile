package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a "join" to CacheRetentionLock on UstadCache (which is one shared database)
 */
@Entity(
    indices = arrayOf(
        Index("cljTableId", "cljEntityUid", "cljUrl", name = "idx_clj_table_entity_url")
    )
)
data class CacheLockJoin(
    @PrimaryKey(autoGenerate = true)
    var cljId: Int = 0,
    var cljTableId: Int = 0,
    var cljEntityUid: Long = 0,
    var cljUrl: String = "",
    var cljLockId: Int = 0,
    var cljStatus: Int = 0,
    var cljType: Int = 0,
) {
    companion object {

        const val STATUS_PENDING_CREATION = 1

        const val STATUS_CREATED = 2

        const val STATUS_PENDING_DELETE = 3

        const val STATUS_ERROR = 4

        const val TYPE_SERVER_RETENTION = 1

    }
}
