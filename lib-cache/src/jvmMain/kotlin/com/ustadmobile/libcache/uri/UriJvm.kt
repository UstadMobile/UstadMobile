package com.ustadmobile.libcache.uri

import java.net.URI

class UriJvm(val uri: URI): IUri {

    override fun toString() = uri.toString()

}

actual fun IUri.Companion.parse(uri: String): IUri {
    return UriJvm(URI(uri))
}
