package com.ustadmobile.util

import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlin.js.Promise

object UmUtil {

    /**
     * Check if the device theme setting is current on dark mode.
     * @return TRUE if is in dark mode otherwise FALSE.
     */
    fun isDarkModeEnabled(): Boolean{
        return window.matchMedia("(prefers-color-scheme: dark)").matches
    }

    suspend fun <T> loadLocalFiles(fileName: String) : T{
        val res = (window.fetch(fileName) as Promise<dynamic>).await()
        val data = (res.json() as Promise<dynamic>).await()
        return (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
            .invoke(data)
            .map { entry -> entry[0] as String to entry[1] }.toMap() as T
    }
}