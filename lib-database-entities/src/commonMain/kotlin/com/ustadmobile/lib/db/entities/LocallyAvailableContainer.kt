package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LocallyAvailableContainer(@PrimaryKey val laContainerUid: Long = 0L)