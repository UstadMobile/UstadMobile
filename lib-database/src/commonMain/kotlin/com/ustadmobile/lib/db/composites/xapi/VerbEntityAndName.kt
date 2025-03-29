package com.ustadmobile.lib.db.composites.xapi

import androidx.room.Embedded
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.lib.db.entities.xapi.VerbLangMapEntry
import kotlinx.serialization.Serializable

@Serializable
data class VerbEntityAndName(
    @Embedded
    var verbEntity: VerbEntity = VerbEntity(),
    @Embedded
    var verbName: VerbLangMapEntry? = null,
)
