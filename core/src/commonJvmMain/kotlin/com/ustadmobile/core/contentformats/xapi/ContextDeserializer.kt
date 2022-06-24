package com.ustadmobile.core.contentformats.xapi

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken

import java.lang.reflect.Type
import java.util.ArrayList

class ContextDeserializer : JsonDeserializer<ContextActivity> {

    private val listType = object : TypeToken<ArrayList<XObject>>() {

    }.type

    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ContextActivity {

        val contextActivity = ContextActivity()
        val jsonObject = json.asJsonObject
        val parentList = checkIfArrayOrObject(jsonObject, "parent", context)
        contextActivity.parent = parentList

        val groupingList = checkIfArrayOrObject(jsonObject, "grouping", context)
        contextActivity.grouping = groupingList

        val categoryList = checkIfArrayOrObject(jsonObject, "category", context)
        contextActivity.category = categoryList

        val otherList = checkIfArrayOrObject(jsonObject, "other", context)
        contextActivity.other = otherList

        return contextActivity
    }


    fun checkIfArrayOrObject(json: JsonObject, objectName: String, context: JsonDeserializationContext): List<XObject>? {

        if (json.has(objectName)) {
            val elem = json.get(objectName)
            if (elem != null && !elem.isJsonNull) {
                val objects = ArrayList<XObject>()
                if (elem.isJsonObject) {
                    val `object` = context.deserialize<XObject>(elem, XObject::class.java)
                    objects.add(`object`)
                    return objects
                } else {
                    return context.deserialize<List<XObject>>(elem, listType)
                }
            }
        }
        return null
    }
}
