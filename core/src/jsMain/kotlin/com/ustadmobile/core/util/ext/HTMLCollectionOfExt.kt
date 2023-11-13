package com.ustadmobile.core.util.ext

import web.dom.Element
import web.html.HTMLCollectionOf


fun <T: Element> HTMLCollectionOf<T>.forEach(
    block: (T) -> Unit
) {
    val collection = this
    for(i in 0 until length) {
        block(collection[i])
    }
}
