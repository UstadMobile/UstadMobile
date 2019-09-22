package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
class LocationAncestorJoin() {

    @PrimaryKey(autoGenerate = true)
    var locationAncestorId: Long = 0

    var locationAncestorChildLocationUid: Long = 0

    var locationAncestorAncestorLocationUid: Long = 0

    constructor(locationAncestorChildLocationUid: Long, locationAncestorAncestorLocationUid: Long) : this() {
        this.locationAncestorChildLocationUid = locationAncestorChildLocationUid
        this.locationAncestorAncestorLocationUid = locationAncestorAncestorLocationUid
    }
}
