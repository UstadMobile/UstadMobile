package com.ustadmobile.centraldb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @param lsUid the XX64 hash of lsUrl
 * @param lsUrl the full url e.g. https://subdomain.example.org/ . MUST end with a trailing slash
 */
@Entity
data class LearningSpaceInfo(
    @PrimaryKey
    var lsUid: Long = 0,
    var lsUrl: String = "",
    var lsName: String = "",
    var lsDescription: String = "",
    var lsLastModified: Long = 0,
    var lsStored: Long = 0,
)
