package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class LocationWithSubLocationCount : Location() {

    var subLocations: Int = 0
}
