package com.ustadmobile.libcache.logging

/**
 * Common logging interface
 */
interface UstadCacheLogger {

    fun v(tag: String?, message: String, throwable: Throwable? = null)

    fun v(tag: String?, throwable: Throwable? = null, message: () -> String)

    fun d(tag: String?, message: String, throwable: Throwable? = null)

    fun d(tag: String?, throwable: Throwable? = null, message: () -> String, )

    fun i(tag: String?, message: String, throwable: Throwable? = null)

    fun i(tag: String?, throwable: Throwable? = null, message: () -> String, )

    fun w(tag: String?, message: String, throwable: Throwable? = null)

    fun w(tag: String?, throwable: Throwable? = null, message: () -> String)

    fun e(tag: String?, message: String, throwable: Throwable? = null)

    fun e(tag: String?, throwable: Throwable? = null, message: () -> String, )

}