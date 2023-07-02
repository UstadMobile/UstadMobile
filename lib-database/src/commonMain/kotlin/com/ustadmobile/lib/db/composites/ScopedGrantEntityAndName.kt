package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.serialization.Serializable

@Serializable
data class ScopedGrantEntityAndName (
    @Embedded
    var scopedGrant: ScopedGrant? = null,

    var name: String? = null
)