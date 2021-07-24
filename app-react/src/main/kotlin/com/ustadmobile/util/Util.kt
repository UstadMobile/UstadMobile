package com.ustadmobile.util

import com.ustadmobile.core.impl.UstadMobileSystemImpl
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlin.js.Promise

object Util {

    fun <T> loadDataAsList(sourcePath: String, strategy: DeserializationStrategy<List<T>>): List<T> {
        val data = UstadMobileSystemImpl.instance.getAppPref(sourcePath, this)
        return if(data != null) Json.decodeFromString(strategy,data).toMutableList() else listOf()
    }

    suspend fun loadAssetsAsText(fileName: String) : String {
        val res = (window.fetch(fileName) as Promise<dynamic>).await()
        val data = (res.text() as Promise<dynamic>).await()
        return data as String
    }

    suspend fun <T> loadFileContentAsMap(fileName: String) : T{
        val res = (window.fetch(fileName) as Promise<dynamic>).await()
        val data = (res.json() as Promise<dynamic>).await()
        return (js("Object.entries") as (dynamic) -> Array<Array<Any?>>)
            .invoke(data)
            .map { entry -> entry[0] as String to entry[1] }.toMap() as T
    }

    fun copyToClipboard(text: String, copyHandler:()-> Unit){
        val secure = js("typeof(navigator.clipboard)!='undefined' " +
                "&& window.isSecureContext").toString().toBoolean()
        if(secure){
            window.navigator.clipboard.writeText(text).then {
                copyHandler()
            }
        }else{
            val element = document.createElement("textarea")
            val textArea = element.asDynamic()
            textArea.value = text
            textArea.style.position = "fixed"
            textArea.style.left = "-999999px"
            textArea.style.top = "-999999px"
            document.body?.appendChild(textArea)
            textArea.focus()
            textArea.select()
            val copied = document.execCommand("copy")
            if(copied){
                copyHandler()
            }
            textArea.remove()
        }
    }

    const val ASSET_ENTRY = "assets/entry_placeholder.jpeg"

    const val ASSET_ACCOUNT = "assets/account.jpg"

    const val ASSET_BOOK = "assets/book.png"

    const val ASSET_FOLDER = "assets/folder.png"



}