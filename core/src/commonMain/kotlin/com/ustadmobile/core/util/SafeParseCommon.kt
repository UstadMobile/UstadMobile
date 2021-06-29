package com.ustadmobile.core.util

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import kotlin.reflect.KClass

/**
 * This is a non-inline version of safeStringify. For now, it is using Kotlinx serialization. It
 * is here simply to ensure we can delegate to platform specific functions if needed.
 */
fun <T: Any> safeStringify(di: DI, strategy: SerializationStrategy<T>, klass: KClass<T>, entity: T): String {
    return Json.encodeToString(strategy, entity)
}
