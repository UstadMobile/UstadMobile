package com.ustadmobile.core.util.stringvalues

/**
 * Interface that is used to "cheaply" wrap key-value sets (e.g. HttpHeaders) from multiple sources
 * to apply common logic (e.g. headers from Ktor server, OkHttp, lib-cache, etc) without copying all
 * headers
 *
 * keys are always case-insensitive
 */
interface IStringValues {

    operator fun get(key: String): String?

    fun getAll(key: String): List<String>

    fun names(): Set<String>

}
