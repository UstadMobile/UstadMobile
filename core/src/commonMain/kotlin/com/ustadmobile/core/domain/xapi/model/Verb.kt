package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.lib.db.entities.VerbEntity
import com.ustadmobile.lib.db.entities.VerbLangMapEntry
import kotlinx.serialization.Serializable

@Serializable
data class Verb(
    val id: String? = null,

    val display: Map<String, String>? = null,
)

data class VerbEntities(
    val verbEntity: VerbEntity,
    val verbLangMapEntries: List<VerbLangMapEntry>,
)


