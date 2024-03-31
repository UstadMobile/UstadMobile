package com.ustadmobile.mui.components

import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase.Companion.LinkTarget
import com.ustadmobile.wrappers.dompurify.sanitize
import js.objects.jso
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.useContext
import react.useEffect
import react.useMemo
import react.useRef
import web.events.addEventListener
import web.html.HTMLDivElement
import web.html.HTMLElement

external interface UstadRawHtmlProps: Props {

    var html: String

    var style: react.CSSProperties?

    var id: String?

}

/**
 * Displays raw HTML. HTML will be put through DOMPurify to remove any potential XSS attacks.
 */
val UstadRawHtml = FC<UstadRawHtmlProps> { props ->
    val cleanHtml = useMemo(props.html) {
        sanitize(props.html)
    }

    val divRef = useRef<HTMLDivElement>(null)
    val linkOpener = useContext(OnClickLinkContext)

    div {
        ref = divRef
        dangerouslySetInnerHTML = jso {
            __html = cleanHtml
        }

        style = props.style
        id = props.id
    }

    useEffect(divRef.current) {
        divRef.current?.querySelectorAll("a")?.iterator()?.forEach { anchorEl ->
            if(!anchorEl.hasAttribute("ustadlink")) {
                anchorEl.addEventListener(
                    type = web.uievents.MouseEvent.Companion.click<HTMLElement>(),
                    handler = { evt ->
                        evt.preventDefault()
                        evt.stopPropagation()
                        val href = anchorEl.getAttribute("href")
                        val target = anchorEl.getAttribute("target")?.let {
                            LinkTarget.of(it)
                        }

                        href?.also { url ->
                            linkOpener?.invoke(url, target ?: LinkTarget.DEFAULT)
                        }
                    }
                )
                anchorEl.setAttribute("ustadlink", "ustadlink")
            }
        }
    }
}

