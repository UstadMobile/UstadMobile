package com.ustadmobile.core.util

expect object UmPlatform {

    var isWeb: Boolean

    /**
     * Run certain block on a specific platform
     * Some CRUD operation cause Web app to miss-behave (Will add and remove content instantly),
     * until we find a better way to handle this on web, better not running them at all on Web
     *
      */
    fun run(block:() -> Unit)

    fun debug()

    fun console(content: Any)
}