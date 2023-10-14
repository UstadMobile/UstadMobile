package com.ustadmobile.libcache.response

import java.io.InputStream

fun HttpResponse.bodyAsStream(): InputStream {
    return (this as HttpResponseJvm).bodyInputStream()
}
