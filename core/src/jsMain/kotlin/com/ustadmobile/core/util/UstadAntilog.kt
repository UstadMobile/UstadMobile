package com.ustadmobile.core.util

import com.ustadmobile.door.ext.DoorTag
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import kotlin.js.console

class UstadAntilog(private val defaultTag: String = "NoTag"): Antilog() {

    override fun isEnable(priority: LogLevel, tag: String?): Boolean {
        return when {
            tag == DoorTag.LOG_TAG && (priority == LogLevel.DEBUG || priority == LogLevel.VERBOSE) -> false
            else -> true
        }
    }

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        val logTag = tag ?: defaultTag

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.message}"
            } else {
                message
            }
        } else throwable?.message ?: return

        when (priority) {
            LogLevel.VERBOSE -> console.log("VERBOSE $logTag : $fullMessage")
            LogLevel.DEBUG -> console.log("DEBUG $logTag : $fullMessage")
            LogLevel.INFO -> console.info("INFO $logTag : $fullMessage")
            LogLevel.WARNING -> console.warn("WARNING $logTag : $fullMessage")
            LogLevel.ERROR -> console.error("ERROR $logTag : $fullMessage")
            LogLevel.ASSERT -> console.error("ASSERT $logTag : $fullMessage")
        }
    }
}