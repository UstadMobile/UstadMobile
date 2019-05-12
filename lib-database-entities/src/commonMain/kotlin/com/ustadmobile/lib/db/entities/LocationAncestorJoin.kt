package com.ustadmobile.lib.db.entities

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
@Entity
class LocationAncestorJoin {

    @UmPrimaryKey(autoIncrement = true)
    @PrimaryKey(autoGenerate = true)
    var locationAncestorId: Long = 0

    var locationAncestorChildLocationUid: Long = 0

    var locationAncestorAncestorLocationUid: Long = 0

    constructor()

    constructor(locationAncestorChildLocationUid: Long, locationAncestorAncestorLocationUid: Long) {
        this.locationAncestorChildLocationUid = locationAncestorChildLocationUid
        this.locationAncestorAncestorLocationUid = locationAncestorAncestorLocationUid
    }
}
