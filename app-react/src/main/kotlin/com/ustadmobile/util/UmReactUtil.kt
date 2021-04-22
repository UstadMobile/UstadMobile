package com.ustadmobile.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.css.pct
import kotlinx.css.px
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import react.RErrorInfo
import kotlin.js.Promise

object UmReactUtil {

    val drawerWidth = 240.px

    val zeroPx = 0.px

    var fullWidth = 100.pct

    var placeHolderImage = "https://www.openhost.co.za/download/bootmin/img/avatar_lg.jpg"

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

    suspend fun <T> loadListFromFiles(fileName: String) : T{
        val res = (window.fetch(fileName) as Promise<dynamic>).await()
        val data = (res.json() as Promise<T>).await()
        return (js("Object.entries") as (dynamic) -> Array<Array<T?>>)
            .invoke(data)
            .map { entry -> entry[1] }.toList() as T
    }

    suspend fun <T> loadList(sourcePath: String,strategy: DeserializationStrategy<List<T>>): List<T> {
        val res = (window.fetch(sourcePath) as Promise<dynamic>).await()
        val data = (res.text() as Promise<String>).await()
        return Json.decodeFromString(strategy,data).toMutableList()
    }
}