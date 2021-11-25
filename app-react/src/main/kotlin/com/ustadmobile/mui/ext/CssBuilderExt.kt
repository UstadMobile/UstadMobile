package com.ustadmobile.mui.ext

import kotlinext.js.Object
import kotlinx.css.CssBuilder

fun CssBuilder.toolbarJsCssToPartialCss(jsObject: Object) {
    // TODO: Pretty rude and crude for now, if it is a height or width, put px on the end of the value
    fun addSuffix(key: String, value: String): String {
        return if (key.contains("height", true) || key.contains("width", true)) {
            value + "px"
        } else {
            value
        }
    }

    val keys = Object.keys(jsObject)
    keys.forEach {
        val value = jsObject.asDynamic()[it]
        if (value != null && jsTypeOf(value) == "object") {
            if (it.startsWith("@media", true)) {
                val query = it.substring(6).trim()
                media(query) {
                    val keys2 = Object.keys(value)
                    keys2.forEach {
                        val value2 = value[it]
                        put(it, addSuffix(it, value2))
                    }
                }
            } else {
                console.error("Don't know how to handle non query sub-object")
            }
        } else {
            put(it, addSuffix(it, value))
        }
    }
}