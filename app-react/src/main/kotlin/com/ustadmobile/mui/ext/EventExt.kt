package com.ustadmobile.mui.ext

import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import web.events.Event

val Event.targetInputValue: String
    get() = (target as? HTMLInputElement)?.value ?: (target as? HTMLTextAreaElement)?.value ?: target.asDynamic().value.toString()