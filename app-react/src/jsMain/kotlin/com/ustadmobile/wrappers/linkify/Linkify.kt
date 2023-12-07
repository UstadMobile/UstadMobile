@file:JsModule("linkify-react")
@file:JsNonModule

package com.ustadmobile.wrappers.linkify

import react.FC
import react.PropsWithChildren

/**
 * As per https://linkify.js.org/docs/linkify-react.html
 */
external interface LinkifyProps : PropsWithChildren{

    @JsName("as")
    var `as`: String?

    var options: LinkifyOptions?

}

/*
 * as per https://linkify.js.org/docs/options.html
 */
@Suppress("unused")
external interface LinkifyOptions {

    var attributes: Any?

    var className: Any?

    var defaultProtocol: String?

    var events: Any?

    var tagName: Any?

    var target: Any?


}

@JsName("default")
external val Linkify: FC<LinkifyProps>

