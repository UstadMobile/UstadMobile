package com.ustadmobile.mui.components

import js.core.jso
import react.FC
import react.Props
import react.dom.html.ReactHTML.div

external interface UstadRawHtmlProps: Props {

    var html: String

    var style: react.CSSProperties?

    var id: String?

}

val UstadRawHtml = FC<UstadRawHtmlProps> { props ->
    //Add https://github.com/cure53/DOMPurify
    div {
        dangerouslySetInnerHTML = jso {
            __html = props.html
        }

        style = props.style
        id = props.id
    }
}

