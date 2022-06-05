package com.ustadmobile.core.util

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlin.reflect.KClass

/**
 * This function is temporarily required because of Kotlinx serialization's flakiness. It can throw
 * errors complaining that a field was unexpected, even when it is part of the compiled class. The
 * error would disappear after a clean and rebuild with no other changes.
 */
actual inline fun <reified T> safeParse(di: DI, strategy: DeserializationStrategy<T>, str: String): T {
    val json: Json = di.direct.instance()
    return json.decodeFromString(strategy,str)
}

actual inline fun <reified T> safeStringify(di: DI, strategy: SerializationStrategy<T>, entity: T): String {
    val json: Json = di.direct.instance()
    return json.encodeToString(strategy,entity)
}

actual fun <T : Any> safeParseList(di: DI, strategy: DeserializationStrategy<List<T>>, klass: KClass<T>, str: String): List<T> {
    val json: Json = di.direct.instance()
    return json.decodeFromString(strategy,str).toList()
}