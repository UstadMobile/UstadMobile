package com.ustadmobile.core.url

import web.url.URL

class UrlKmpJs(private val url: URL) : UrlKmp{

    override fun toString(): String {
        return url.toString()
    }

    override fun resolve(path: String): UrlKmp {
        return UrlKmpJs(URL(path, url.toString()))
    }

}