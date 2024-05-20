package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xapi.xapiRequireValidIRI
import com.ustadmobile.core.domain.xxhash.XXStringHasher
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

fun Verb.toVerbEntities(
    xxHasher: XXStringHasher,
): VerbEntities {
    val verbIri = xapiRequireValidIRI(id)
    val verbUid = xxHasher.hash(verbIri)

    return VerbEntities(
        verbEntity = VerbEntity(
            verbUid = verbUid,
            verbUrlId = id,
        ),
        verbLangMapEntries = display?.entries?.map {
            VerbLangMapEntry(
                vlmeVerbUid = verbUid,
                vlmeLangHash = xxHasher.hash(it.key),
                vlmeEntryString = it.value,
                vlmeLangCode = it.key,
            )
        } ?: emptyList(),
    )
}
