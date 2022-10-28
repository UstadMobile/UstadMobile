package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a token provided to an external app
 */
@Entity
class AuthToken {

    @PrimaryKey(autoGenerate = true)
    var atUid: Long = 0

    var atAuth: String? = null

    var atIssuedTime: Long = 0

    var atExpiration: Long = Long.MAX_VALUE


}