package com.ustadmobile.core.util.isimplerequest

import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.core.util.stringvalues.asIStringValues
import rawhttp.core.RawHttpRequest

class RawHttpSimpleRequest(private val rawRequest: RawHttpRequest): ISimpleTextRequest {

    override val headers: IStringValues
        get() = rawRequest.headers.asIStringValues()
    override val method: String
        get() = rawRequest.method
    override val path: String
        get() = rawRequest.uri.path
    override val body: String?
        get() = rawRequest.body?.get()?.decodeBodyToString(Charsets.UTF_8)

}

fun RawHttpRequest.asISimpleTextRequest(): ISimpleTextRequest = RawHttpSimpleRequest(this)

