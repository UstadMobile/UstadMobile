/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ustadmobile.core.tincan

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

import kotlin.jvm.JvmOverloads


/**
 *
 * @author mike
 */
class Activity @JvmOverloads constructor(id: String, internal var aType: String, internal var lang: String = "en") {

    /**
     * Gets the id of the activity
     *
     * @return ID of activity as per "id" attribute
     */
    var id: String
        internal set

    var desc: String? = null
        internal set

    /**
     * Gets the name of the activity as per it's child name tag
     *
     * @return Name of the activity
     */
    var name: String? = null
        internal set

    var launchUrl: String? = null
        internal set

    private var extensions: MutableMap<String,String>? = null

    /**
     * Returns a minimal xAPI statement which references the ID of this activity
     * @return
     */
    val activityJSON: JsonObject =JsonObject(mapOf("id" to JsonPrimitive(id)))

    init {
        this.id = id
    }

    /**
     * Sets an extension for this activity.
     * @param key
     * @param value
     */
    fun setExtension(key: String, value: String) {
        if (extensions.isNullOrEmpty()) {
            extensions =  mutableMapOf()
        }

        extensions!![key] = value
    }

    fun getExtension(key: String): String? {
        return if (extensions == null) {
            null
        } else if (!extensions!!.containsKey(key)) {
            null
        } else {
            extensions!![key].toString()
        }
    }


}
