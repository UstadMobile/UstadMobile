package com.ustadmobile.core.url

import web.url.URL

actual fun UrlKmp(url: String): UrlKmp = UrlKmpJs(URL(url))

