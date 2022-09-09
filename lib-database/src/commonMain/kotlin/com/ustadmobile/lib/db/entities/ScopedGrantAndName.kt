package com.ustadmobile.lib.db.entities

import androidx.room.Embedded
import kotlinx.serialization.Serializable

@Serializable
class ScopedGrantAndName {

    @Embedded
    var scopedGrant: ScopedGrant? = null

    var name: String? = null

}