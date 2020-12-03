package com.ustadmobile.core.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.DeserializationStrategy
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlinx.serialization.SerializationStrategy
import kotlin.reflect.KClass


actual inline fun <reified T> safeParse(di: DI, strategy: DeserializationStrategy<T>, str: String) : T {
    val gson : Gson = di.direct.instance()
    return gson.fromJson(str, T::class.java)
}

actual inline fun <reified T> safeStringify(di: DI, strategy: SerializationStrategy<T>, entity: T) : String {
    val gson : Gson = di.direct.instance()
    return gson.toJson(entity)
}

actual fun <T : Any> safeParseList(di : DI, strategy: DeserializationStrategy<List<T>>,
                                             klass: KClass<T>, str: String): List<T> {
    val gson : Gson = di.direct.instance()
    return gson.fromJson(str, TypeToken.getParameterized(List::class.java, klass.java).type)
}
