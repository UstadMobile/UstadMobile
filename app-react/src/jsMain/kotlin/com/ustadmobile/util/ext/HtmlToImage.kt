package com.ustadmobile.util.ext

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import kotlin.js.Date

@JsModule("html-to-image")
@JsNonModule
private external val htmlToImageModule: dynamic

fun exportToPng(id: String, title: String?){
    val task = htmlToImageModule.toPng(document.getElementById(id))
    val imgName = "chart-${title?.lowercase() ?: Date().getTime()}.png".replace("\\s".toRegex(), "-")
    val blobCallback: (data: dynamic) -> Unit =  {
        val link = document.createElement("a") as HTMLAnchorElement
        document.body?.appendChild(link)
        link.href = it
        link.name
        link.download = imgName
        link.click()
        document.body?.removeChild(link);
    }
    js("task.then(function(data){blobCallback(data)});")
}