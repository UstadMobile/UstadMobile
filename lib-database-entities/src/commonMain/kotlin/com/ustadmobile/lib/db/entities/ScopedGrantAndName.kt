package com.ustadmobile.lib.db.entities

import androidx.room.Embedded

class ScopedGrantAndName {

    @Embedded
    var scopedGrant: ScopedGrant? = null

    var name: String? = null

}