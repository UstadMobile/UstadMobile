package com.ustadmobile.core.util

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import org.kodein.di.DI
import kotlin.reflect.KClass

/**
 * This function is temporarily required because of Kotlinx serialization's flakiness. It can throw
 * errors complaining that a field was unexpected, even when it is part of the compiled class. The
 * error would disappear after a clean and rebuild with no other changes.
 */
expect inline fun <reified T> safeParse(di: DI, strategy: DeserializationStrategy<T>, str: String) : T

expect inline fun <reified T> safeStringify(di: DI, strategy: SerializationStrategy<T>, entity: T) : String

expect fun <T: Any> safeParseList(di : DI, strategy: DeserializationStrategy<List<T>>,
                                            klass: KClass<T>, str: String): List<T>
