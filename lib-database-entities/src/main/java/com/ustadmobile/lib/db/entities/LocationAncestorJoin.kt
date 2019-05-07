package com.ustadmobile.lib.db.entities

import com.ustadmobile.lib.database.annotation.UmEntity
import com.ustadmobile.lib.database.annotation.UmPrimaryKey

@UmEntity
class LocationAncestorJoin {

    @UmPrimaryKey(autoIncrement = true)
    var locationAncestorId: Long = 0

    var locationAncestorChildLocationUid: Long = 0

    var locationAncestorAncestorLocationUid: Long = 0

    constructor()

    constructor(locationAncestorChildLocationUid: Long, locationAncestorAncestorLocationUid: Long) {
        this.locationAncestorChildLocationUid = locationAncestorChildLocationUid
        this.locationAncestorAncestorLocationUid = locationAncestorAncestorLocationUid
    }
}
