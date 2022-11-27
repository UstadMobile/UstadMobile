package com.ustadmobile.lib.rest.logging

import com.ustadmobile.door.ext.DoorTag
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import org.slf4j.LoggerFactory
import org.slf4j.Logger
import java.io.PrintWriter
import java.io.StringWriter
import java.util.regex.Pattern

class LogbackAntiLog: Antilog() {

    private val defaultTag = "app"

    private val anonymousClass = Pattern.compile("(\\$\\d+)+$")


    private val tagMap: HashMap<LogLevel, String> = hashMapOf(
        LogLevel.VERBOSE to "[VERBOSE]",
        LogLevel.DEBUG to "[DEBUG]",
        LogLevel.INFO to "[INFO]",
        LogLevel.WARNING to "[WARN]",
        LogLevel.ERROR to "[ERROR]",
        LogLevel.ASSERT to "[ASSERT]"
    )

    override fun isEnable(priority: LogLevel, tag: String?): Boolean {
        return when {
            tag == DoorTag.LOG_TAG && (priority == LogLevel.DEBUG || priority == LogLevel.VERBOSE) -> false
            else -> true
        }
    }

    private val logger: Logger = LoggerFactory.getLogger(LogbackAntiLog::class.java.name)

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {

        val debugTag = tag ?: performTag(defaultTag)

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.stackTraceString}"
            } else {
                message
            }
        } else throwable?.stackTraceString ?: return

        when (priority) {
            LogLevel.VERBOSE -> logger.trace(buildLog(priority, debugTag, fullMessage))
            LogLevel.DEBUG -> logger.debug(buildLog(priority, debugTag, fullMessage))
            LogLevel.INFO -> logger.info(buildLog(priority, debugTag, fullMessage))
            LogLevel.WARNING -> logger.warn(buildLog(priority, debugTag, fullMessage))
            LogLevel.ERROR -> logger.error(buildLog(priority, debugTag, fullMessage))
            LogLevel.ASSERT -> logger.error(buildLog(priority, debugTag, fullMessage))
        }
    }

    internal fun buildLog(priority: LogLevel, tag: String?, message: String?): String {
        return "${tagMap[priority]} ${tag ?: performTag(defaultTag)} - $message"
    }

    private fun performTag(defaultTag: String): String {
        val thread = Thread.currentThread().stackTrace

        return if (thread.size >= CALL_STACK_INDEX) {
            thread[CALL_STACK_INDEX].run {
                "${createStackElementTag(className)}\$$methodName"
            }
        } else {
            defaultTag
        }
    }

    internal fun createStackElementTag(className: String): String {
        var tag = className
        val m = anonymousClass.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
    }

    private val Throwable.stackTraceString
        get(): String {
            // DO NOT replace this with Log.getStackTraceString() - it hides UnknownHostException, which is
            // not what we want.
            val sw = StringWriter(256)
            val pw = PrintWriter(sw, false)
            printStackTrace(pw)
            pw.flush()
            return sw.toString()
        }

    companion object {
        private const val CALL_STACK_INDEX = 8
    }
}