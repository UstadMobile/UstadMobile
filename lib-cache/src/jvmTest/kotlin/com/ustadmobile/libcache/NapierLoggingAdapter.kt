package com.ustadmobile.libcache

import com.ustadmobile.libcache.logging.UstadCacheLogger
import io.github.aakira.napier.Napier

class NapierLoggingAdapter: UstadCacheLogger {
    override fun v(tag: String?, message: String, throwable: Throwable?) {
        Napier.v(message = message, tag = tag, throwable = throwable)
    }

    override fun v(tag: String?, throwable: Throwable?, message: () -> String, ) {
        Napier.v(message = message, tag = tag, throwable = throwable)
    }

    override fun d(tag: String?, message: String, throwable: Throwable?) {
        Napier.d(message = message, tag = tag, throwable = throwable)
    }

    override fun d(tag: String?, throwable: Throwable?, message: () -> String) {
        Napier.d(message = message, tag = tag, throwable = throwable)
    }

    override fun i(tag: String?, message: String, throwable: Throwable?) {
        Napier.i(message = message, tag = tag, throwable = throwable)
    }

    override fun i(tag: String?, throwable: Throwable?, message: () -> String) {
        Napier.i(message = message, tag = tag, throwable = throwable)
    }

    override fun w(tag: String?, message: String, throwable: Throwable?) {
        Napier.w(message = message, tag = tag, throwable = throwable)
    }

    override fun w(tag: String?, throwable: Throwable?, message: () -> String) {
        Napier.w(message = message, tag = tag, throwable = throwable)
    }

    override fun e(tag: String?, message: String, throwable: Throwable?) {
        Napier.e(message = message, tag = tag, throwable = throwable)
    }

    override fun e(tag: String?, throwable: Throwable?, message: () -> String) {
        Napier.e(message = message, tag = tag, throwable = throwable)
    }
}