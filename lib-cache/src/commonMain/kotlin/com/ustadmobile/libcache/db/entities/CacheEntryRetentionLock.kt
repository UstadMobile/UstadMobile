package com.ustadmobile.libcache.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class CacheEntryRetentionLock(
    @PrimaryKey(autoGenerate = true)
    var cerlId: Int = 0,
    var cerlCeId: Int = 0,
) {
}
