package com.ustadmobile.core.util.isimpleresponse

import com.ustadmobile.core.util.stringvalues.IStringValues

/**
 * GEneric interface used to wrap various different types of http responses e.g. RawHttp, NanoHttp,
 * Ktor, etc.
 */
interface ISimpleTextResponse {

    val headers: IStringValues

    val responseCode: Int

    val responseMessage: String?

    val responseBody: String?

}