package com.ustadmobile.lib.annotationprocessor.core.db2

import androidx.room.PrimaryKey

data class ExampleEntity2(@PrimaryKey(autoGenerate = true) var uid: Long, var name: String, var someNumber: Long)