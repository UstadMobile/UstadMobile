package com.ustadmobile.mui.components

import js.core.jso
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useContext
import react.useEffect
import react.useRef
import web.html.HTMLDivElement

external interface UstadRawHtmlProps: Props {

    var html: String

    var style: react.CSSProperties?

    var id: String?

}

val UstadRawHtml = FC<UstadRawHtmlProps> { props ->
    //Add https://github.com/cure53/DOMPurify
    val divRef = useRef<HTMLDivElement>(null)
    val linkOpener = useContext(LinkOpenerContext)

    div {
        ref = divRef
        dangerouslySetInnerHTML = jso {
            __html = props.html
        }

        style = props.style
        id = props.id
    }

    useEffect(divRef.current) {
        divRef.current?.querySelectorAll("a")?.iterator()?.forEach { anchorEl ->
            if(!anchorEl.hasAttribute("ustadlink")) {
                anchorEl.addEventListener(
                    type = web.uievents.MouseEvent.Companion.CLICK,
                    callback = { evt ->
                        evt.preventDefault()
                        evt.stopPropagation()
                        val href = anchorEl.getAttribute("href")
                        href?.also { url ->
                            linkOpener?.invoke(url)
                        }
                    }
                )
                anchorEl.setAttribute("ustadlink", "ustadlink")
            }
        }
    }
}

