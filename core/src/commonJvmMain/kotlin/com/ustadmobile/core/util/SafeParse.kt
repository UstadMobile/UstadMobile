package com.ustadmobile.core.util

import com.google.gson.Gson
import kotlinx.serialization.DeserializationStrategy
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance
import kotlinx.serialization.SerializationStrategy


actual inline fun <reified T> safeParse(di: DI, strategy: DeserializationStrategy<T>, str: String) : T {
    val gson : Gson = di.direct.instance()
    return gson.fromJson(str, T::class.java)
}

actual inline fun <reified T> safeStringify(di: DI, strategy: SerializationStrategy<T>, entity: T) : String {
    val gson : Gson = di.direct.instance()
    return gson.toJson(entity)
}
