package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
open class WorkSpace {

    @PrimaryKey(autoGenerate = true)
    var uid: Long = 0

    var name: String? = null

    var guestLogin: Boolean = true

    var registrationAllowed: Boolean = true
}