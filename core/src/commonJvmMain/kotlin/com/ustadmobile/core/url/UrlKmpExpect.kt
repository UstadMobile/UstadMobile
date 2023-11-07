package com.ustadmobile.core.url

import java.net.URL

actual fun UrlKmp(url: String): UrlKmp = UrlKmpJvm(URL(url))
