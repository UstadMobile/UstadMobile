package com.ustadmobile.core.util.ext

import com.ustadmobile.core.util.stringvalues.IStringValues
import com.ustadmobile.libcache.headers.HttpHeaders

class CacheHttpHeadersIStringValues(private val headers: HttpHeaders) : IStringValues{

    override fun get(key: String) = headers[key]

    override fun getAll(key: String) = headers.getAllByName(key)
    override fun names() = headers.names()
}

fun HttpHeaders.asIStringValues(): IStringValues = CacheHttpHeadersIStringValues(this)