package com.ustadmobile.core.api.oneroster.model

import kotlinx.serialization.json.Json
import kotlin.test.Test

class ClazzSerializerTest {

    @Test
    fun clazzSerialize() {
        val oneRosterClazz = Clazz(
            "", Status.ACTIVE, "", "Hello World"
        )

        val json = Json {
            encodeDefaults = true
        }

        val oneRosterJson = json.encodeToString(Clazz.serializer(), oneRosterClazz)
        println(oneRosterJson)
        val fromJson = json.decodeFromString(Clazz.serializer(), oneRosterJson)
        println(fromJson)
    }

}