package com.ustadmobile.core.util.ext

import web.dom.document

actual fun String.htmlToPlainText(): String {
    val element = document.createElement("div")

    //to ensure that we get the word count right... so that words on a new line are counted as new words
    element.innerHTML = this.replace("<br/>", " ").replace("<br>", " ")
        .replace("</p><p>", " ")
    return element.innerText
}


actual fun String.requireFileSeparatorSuffix(): String = requirePostfix("/")