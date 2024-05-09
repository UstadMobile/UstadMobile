package com.ustadmobile.core.util.isimplerequest

import com.ustadmobile.core.util.stringvalues.IStringValues

/**
 * Generic interface used to wrap various different types of http requests e.g. RawHttp, NanoHttp,
 * Ktor, etc.
 */
interface ISimpleTextRequest {

    val headers: IStringValues

    val method: String

    val path: String

    val body: String?

}