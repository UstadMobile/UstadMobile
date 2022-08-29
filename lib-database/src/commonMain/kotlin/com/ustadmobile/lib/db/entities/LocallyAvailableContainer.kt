package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class LocallyAvailableContainer(@PrimaryKey val laContainerUid: Long = 0L)