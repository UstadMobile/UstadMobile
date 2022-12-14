package com.ustadmobile.core.contentformats.xapi

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type
import java.util.ArrayList

class StatementDeserializer : JsonDeserializer<Statement> {


    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Statement {

        val statement = Statement()
        val jObject = json.asJsonObject

        statement.actor = context.deserialize(jObject.get("actor"), Actor::class.java)
        statement.verb = context.deserialize(jObject.get("verb"), Verb::class.java)
        statement.result = context.deserialize(jObject.get("result"), Result::class.java)
        statement.context = context.deserialize(jObject.get("context"), XContext::class.java)
        statement.timestamp = if (jObject.has("timestamp")) jObject.get("timestamp").asString else null
        statement.stored = if (jObject.has("stored")) jObject.get("stored").asString else null
        statement.authority = context.deserialize(jObject.get("authority"), Actor::class.java)
        statement.version = if (jObject.has("version")) jObject.get("version").asString else null
        statement.id = if (jObject.has("id")) jObject.get("id").asString else null
        statement.attachments = context.deserialize(jObject.get("attachments"), listType)
        statement.objectType = if (jObject.has("objectType")) jObject.get("objectType").asString else null

        val `object` = context.deserialize<XObject>(jObject.get("object"), XObject::class.java)
        if (`object`.objectType != null && `object`.objectType == "SubStatement") {
            statement.subStatement = context.deserialize(jObject.get("object"), Statement::class.java)
        } else {
            statement.`object` = `object`
        }

        return statement
    }

    companion object {

        val listType = object : TypeToken<ArrayList<Attachment>>() {

        }.type
    }
}
