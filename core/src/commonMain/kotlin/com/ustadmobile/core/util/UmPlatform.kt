package com.ustadmobile.core.util

expect object UmPlatform {

    var isWeb: Boolean

    fun debug()

    fun console(content: Any)
}