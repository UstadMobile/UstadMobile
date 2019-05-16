package com.ustadmobile.lib.db2

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TestEntity(@PrimaryKey var uid: Int, var name: String)