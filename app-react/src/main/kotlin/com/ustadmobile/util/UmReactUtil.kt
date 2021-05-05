package com.ustadmobile.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.css.pct
import kotlinx.css.px
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlin.js.Promise

object UmReactUtil {

    val drawerWidth = 240.px

    val zeroPx = 0.px

    var fullWidth = 100.pct

    /**
     * Check if the device theme setting is current on dark mode.
     * @return TRUE if is in dark mode otherwise FALSE.
     */
    fun isDarkModeEnabled(): Boolean{
        return window.matchMedia("(prefers-color-scheme: dark)").matches
    }

    suspend fun <T> loadMapFromLocalFile(fileName: String) : T{
        val res = (window.fetch(fileName) as Promise<dynamic>).await()
        val data = (res.json() as Promise<dynamic>).await()
        return (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
            .invoke(data)
            .map { entry -> entry[0] as String to entry[1] }.toMap() as T
    }

    fun <T> loadList(sourcePath: String,strategy: DeserializationStrategy<List<T>>): List<T> {
        val data = UstadMobileSystemImpl.instance.getAppPref(sourcePath, this)
        return if(data != null) Json.decodeFromString(strategy,data).toMutableList() else listOf()
    }
}