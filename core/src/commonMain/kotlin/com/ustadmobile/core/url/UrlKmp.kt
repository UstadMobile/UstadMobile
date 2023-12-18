package com.ustadmobile.core.url

/**
 * Basic Multiplatform wrapper for URL. Mostly intended to be used for resolving relative links.
 */
interface UrlKmp {

    override fun toString(): String

    fun resolve(path: String): UrlKmp

}