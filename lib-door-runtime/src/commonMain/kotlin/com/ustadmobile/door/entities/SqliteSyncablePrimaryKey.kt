package com.ustadmobile.door.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * SQLite sequence numbers will automatically jump to the highest value seen on a table, even if the
 * range for this device is below that.
 */
@Entity
data class SqliteSyncablePrimaryKey(
        /**
         * TableId as per SyncableEntity annotation
         */
        @PrimaryKey
        var sspTableId: Int,

        /**
         * The next primary key to use
         */
        var sspNextPrimaryKey: Int)
