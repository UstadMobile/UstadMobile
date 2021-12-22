package com.ustadmobile.core.util

expect object UmPlatform {

    var isWeb: Boolean

    /**
     * Some CRUD operation cause Web app to miss-behave, until we find a better way
     * to handle them on web, better not running them at all on Web
      */
    fun run(block:() -> Unit)

    fun debug()

    fun console(content: Any)
}