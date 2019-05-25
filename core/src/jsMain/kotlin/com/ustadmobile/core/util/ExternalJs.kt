package com.ustadmobile.core.util

import org.w3c.dom.Storage

external class UmContextWrapper {
    fun getRouter(): Router

    /**
     * Setting active route
     * @param activeRoute current active route
     */
    fun setActiveRoute(activeRoute: Any)

    /**
     * Get current active route
     */
    fun getActiveRoute(): Any
}

external class Router{

    fun navigate(commands: Any, extras: Any)
}

open class localStorage : Storage()