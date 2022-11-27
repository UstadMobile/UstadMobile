package com.ustadmobile.mui.ext

import kotlinx.css.CssBuilder
import kotlinx.js.Object

/**
 * This is only a simple jsObjectToCss converter. So far it can only handle
 * a jsObject that is one layer deep, or two layers if the second layer is a
 * media query (used initially just to convert the currentTheme.mixins.toolbar)
 */
fun CssBuilder.toolbarJsCssToPartialCss(jsObject: Object) {

    fun addPxSuffixIfNeeded(key: String, value: String): String {
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
                        put(it, addPxSuffixIfNeeded(it, value2))
                    }
                }
            } else {
                console.error("Don't know how to handle non query sub-object")
            }
        } else {
            put(it, addPxSuffixIfNeeded(it, value))
        }
    }
}