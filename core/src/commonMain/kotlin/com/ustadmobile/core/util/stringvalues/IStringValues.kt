package com.ustadmobile.core.util.stringvalues

import kotlinx.serialization.Serializable

/**
 * Interface that is used to "cheaply" wrap key-value sets (e.g. HttpHeaders) from multiple sources
 * to apply common logic (e.g. headers from Ktor server, OkHttp, lib-cache, etc) without copying all
 * headers
 *
 * keys are always case-insensitive
 */
@Serializable(with = StringValuesSerializer::class)
interface IStringValues {

    operator fun get(key: String): String?

    fun getAll(key: String): List<String>

    fun names(): Set<String>

    companion object {

        fun empty(): IStringValues = MapStringValues(emptyMap())

        fun contentType(contentType: String) = MapStringValues(
            mapOf(
                "content-type" to listOf(contentType),
            )
        )

    }

}
