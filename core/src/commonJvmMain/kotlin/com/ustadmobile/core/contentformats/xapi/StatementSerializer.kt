package com.ustadmobile.core.contentformats.xapi

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.ustadmobile.core.domain.xapi.model.Actor
import com.ustadmobile.core.domain.xapi.model.Result
import com.ustadmobile.core.domain.xapi.model.Statement
import com.ustadmobile.core.domain.xapi.model.Verb
import com.ustadmobile.core.domain.xapi.model.XContext
import com.ustadmobile.core.domain.xapi.model.XObject

import java.lang.reflect.Type

class StatementSerializer : JsonSerializer<Statement> {


    override fun serialize(src: Statement, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {

        val jsonObject = JsonObject()
        jsonObject.add("actor", context.serialize(src.actor, Actor::class.java))
        jsonObject.add("verb", context.serialize(src.verb, Verb::class.java))
        jsonObject.add("result", context.serialize(src.result, Result::class.java))
        jsonObject.add("context", context.serialize(src.context, XContext::class.java))
        jsonObject.addProperty("timestamp", src.timestamp)
        jsonObject.addProperty("stored", src.stored)
        jsonObject.add("authority", context.serialize(src.authority, Actor::class.java))
        jsonObject.addProperty("version", src.version)
        jsonObject.addProperty("id", src.id)
        jsonObject.add("attachments", context.serialize(src.attachments,
            StatementDeserializer.listType
        ))
        jsonObject.addProperty("objectType", src.objectType)

        if (src.subStatement != null) {
            jsonObject.add("object", context.serialize(src.subStatement, Statement::class.java))
        } else {
            jsonObject.add("object", context.serialize(src.`object`, XObject::class.java))
        }

        return jsonObject
    }
}
