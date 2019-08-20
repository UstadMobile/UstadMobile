package com.ustadmobile.door

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SyncNode(@PrimaryKey val nodeClientId: Int, val master: Boolean = false)