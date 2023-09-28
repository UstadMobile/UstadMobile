package com.ustadmobile.lib.db.composites

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.PersonGroup
import com.ustadmobile.lib.db.entities.PersonGroupMember
import com.ustadmobile.lib.db.entities.ScopedGrant
import kotlinx.serialization.Serializable

@Serializable
data class ScopedGrantAndGroupMember(
    @Embedded
    var scopedGrant: ScopedGrant? = null,
    @Embedded
    var personGroupMember: PersonGroupMember? = null,
    @Embedded
    var personGroup: PersonGroup? = null
)

