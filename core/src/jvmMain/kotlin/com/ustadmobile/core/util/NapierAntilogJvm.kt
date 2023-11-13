package com.ustadmobile.core.util

import com.ustadmobile.door.ext.DoorTag
import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import java.io.PrintWriter
import java.io.StringWriter
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern

class NapierAntilogJvm(val logLevel: Level): Antilog() {

    private val anonymousClass = Pattern.compile("(\\$\\d+)+$")

    private val consoleHandler = ConsoleHandler().apply {
        level = logLevel
    }

    private val logger: Logger = Logger.getLogger("com.ustadmobile").apply {
        level = logLevel

        addHandler(consoleHandler)
    }

    private val tagMap = mapOf(
        LogLevel.VERBOSE to Level.FINEST,
        LogLevel.DEBUG to Level.FINER,
        LogLevel.INFO to Level.INFO,
        LogLevel.WARNING to Level.WARNING,
        LogLevel.ERROR to Level.SEVERE,
        LogLevel.ASSERT to Level.SEVERE,
    )

    override fun isEnable(priority: LogLevel, tag: String?): Boolean {
        return priority.ordinal >= LogLevel.DEBUG.ordinal
                || tag != DoorTag.LOG_TAG
    }

    internal fun createStackElementTag(className: String): String {
        var tag = className
        val m = anonymousClass.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        return tag.substring(tag.lastIndexOf('.') + 1)
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

    internal fun buildLog(priority: LogLevel, tag: String?, message: String?): String {
        return "${tagMap[priority]} ${tag ?: performTag("Ustad")} - $message"
    }

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {

        val debugTag = tag ?: performTag("Ustad")

        val fullMessage = if (message != null) {
            if (throwable != null) {
                "$message\n${throwable.stackTraceString}"
            } else {
                message
            }
        } else throwable?.stackTraceString ?: return

        when (priority) {
            LogLevel.VERBOSE -> logger.finest(buildLog(priority, debugTag, fullMessage))
            LogLevel.DEBUG -> logger.fine(buildLog(priority, debugTag, fullMessage))
            LogLevel.INFO -> logger.info(buildLog(priority, debugTag, fullMessage))
            LogLevel.WARNING -> logger.warning(buildLog(priority, debugTag, fullMessage))
            LogLevel.ERROR -> logger.severe(buildLog(priority, debugTag, fullMessage))
            LogLevel.ASSERT -> logger.severe(buildLog(priority, debugTag, fullMessage))
        }
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