package com.ustadmobile.core.util.ext

import web.dom.document

actual fun String.htmlToPlainText(): String {
    val element = document.createElement("div")
    element.innerHTML = this
    return element.innerText
}

