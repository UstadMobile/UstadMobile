package com.ustadmobile.libcache.response

import java.io.InputStream

interface HttpResponseJvm: HttpResponse {

    fun bodyInputStream(): InputStream

}
