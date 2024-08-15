package com.ustadmobile.centraldb.entities

import androidx.room.PrimaryKey

/**
 * Learning Space
 */
class LearningSpaceConfig(
    @PrimaryKey
    var lscUid: Long = 0,
    var lscUrl: String = "",
    var lscDbUrl: String = "",
    var lscDbUsername: String = "",
    var lscDbPassword: String = "",
)
