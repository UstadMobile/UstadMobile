package com.ustadmobile.ihttp.headers

data class IHttpHeaderImpl(
    override val name: String,
    override val value: String,
): IHttpHeader
