package com.ustadmobile.core.url

import java.net.URL

class UrlKmpJvm internal constructor(private val url: URL): UrlKmp {

    override fun toString(): String {
        return url.toString()
    }

    override fun resolve(path: String): UrlKmp {
        return UrlKmpJvm(URL(url, path))
    }
}