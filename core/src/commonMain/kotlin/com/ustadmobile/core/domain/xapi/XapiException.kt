package com.ustadmobile.core.domain.xapi

class XapiException(val responseCode: Int, message: String): Exception(message)
