package com.ustadmobile.wrappers.linkify

import js.objects.jso
import react.FC
import react.Props
import web.events.Event
import web.html.HTMLElement
import kotlin.js.json

val LinkifyPreview = FC<Props> {
    Linkify {
        options = jso {
            attributes = json(
                "onClick" to { evt: Event ->
                    evt.preventDefault()
                    val targetEl = evt.target as? HTMLElement
                    val href = targetEl?.getAttribute("href")

                    if(href != null){
                        println(href)
                    }
                }
            )
        }

        + "hello http://www.link.com/ link"
    }
}
