package com.ustadmobile.mui.components

import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget
import com.ustadmobile.wrappers.linkify.Linkify
import js.objects.jso
import react.FC
import react.PropsWithChildren
import react.useContext
import web.events.Event
import web.html.HTMLElement
import kotlin.js.json

external interface UstadLinkifyProps: PropsWithChildren {

    var elementName: String?

}

val UstadLinkify = FC<UstadLinkifyProps> { props ->
    val linkOpener = useContext(OnClickLinkContext)

    Linkify {
        `as` = props.elementName
        options = jso {
            attributes = json(
                "onClick" to { evt: Event ->
                    evt.preventDefault()
                    val targetEl = evt.target as? HTMLElement
                    val href = targetEl?.getAttribute("href")
                    val target = targetEl?.getAttribute("target")?.let {
                        LinkTarget.of(it)
                    }

                    if(href != null){
                        linkOpener?.invoke(href, target ?: LinkTarget.DEFAULT)
                    }
                }
            )
        }

        + props.children
    }

}