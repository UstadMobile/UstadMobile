package com.ustadmobile.appconfigdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Learning Space Configuration
 *
 * @param lscDbUrl the learning space url e.g. https://spacename.example.org/
 * @param lscUrl the JDBC URL to be used for the learning space database instance
 *        (e.g. UmAppDatabase)
 * @param lscDbUsername the learning space JDBC database username (if any)
 * @param lscUid the learning space JDBC password (if any)
 */
@Entity
class LearningSpaceConfig(
    @PrimaryKey
    var lscUid: Long = 0,
    var lscUrl: String = "",
    var lscDbUrl: String = "",
    var lscDbUsername: String? = null,
    var lscDbPassword: String? = null,
)
