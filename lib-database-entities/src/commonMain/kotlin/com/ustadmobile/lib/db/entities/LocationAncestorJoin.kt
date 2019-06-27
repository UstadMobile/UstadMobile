package com.ustadmobile.lib.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class LocationAncestorJoin() {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var locationAncestorId: Long = 0

    var locationAncestorChildLocationUid: Long = 0

    var locationAncestorAncestorLocationUid: Long = 0

    constructor(locationAncestorChildLocationUid: Long, locationAncestorAncestorLocationUid: Long) : this() {
        this.locationAncestorChildLocationUid = locationAncestorChildLocationUid
        this.locationAncestorAncestorLocationUid = locationAncestorAncestorLocationUid
    }
}
