package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.ihttp.headers.IHttpHeaders

class CacheHttpHeadersIStringValues(private val headers: IHttpHeaders) : IStringValues{

    override fun get(key: String) = headers[key]

    override fun getAll(key: String) = headers.getAllByName(key)
    override fun names() = headers.names()
}

fun IHttpHeaders.asIStringValues(): IStringValues = CacheHttpHeadersIStringValues(this)