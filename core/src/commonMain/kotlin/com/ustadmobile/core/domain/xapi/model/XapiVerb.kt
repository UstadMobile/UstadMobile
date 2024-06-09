package com.ustadmobile.core.domain.xapi.model

import com.ustadmobile.core.domain.xapi.xapiRequireValidIRI
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.lib.db.entities.xapi.VerbEntity
import com.ustadmobile.lib.db.entities.xapi.VerbLangMapEntry
import kotlinx.serialization.Serializable


const val VERB_COMPLETED = "http://adlnet.gov/expapi/verbs/completed"

const val VERB_PROGRESSED = "http://adlnet.gov/expapi/verbs/progressed"

@Serializable
data class XapiVerb(
    val id: String? = null,

    val display: Map<String, String>? = null,
)

data class VerbEntities(
    val verbEntity: VerbEntity,
    val verbLangMapEntries: List<VerbLangMapEntry>,
)

fun XapiVerb.toVerbEntities(
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
