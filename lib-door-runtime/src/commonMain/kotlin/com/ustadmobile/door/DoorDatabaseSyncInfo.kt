package com.ustadmobile.door

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DoorDatabaseSyncInfo(@PrimaryKey val pk: Int = 0,
                                var dbNodeId: Int = 0,
                                val dbVersion: Int = 0)
