package com.ustadmobile.core.util

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel
import java.util.logging.ConsoleHandler
import java.util.logging.Level
import java.util.logging.Logger

class NapierAntilogJvm(val logLevel: Level): Antilog() {

    private val consoleHandler = ConsoleHandler().apply {
        level = logLevel
    }

    private val logger: Logger = Logger.getLogger("com.ustadmobile").apply {
        level = logLevel

        addHandler(consoleHandler)
    }

    private val levelMap = mapOf(
        LogLevel.VERBOSE to Level.FINEST,
        LogLevel.DEBUG to Level.FINER,
        LogLevel.INFO to Level.INFO,
        LogLevel.WARNING to Level.WARNING,
        LogLevel.ERROR to Level.SEVERE,
        LogLevel.ASSERT to Level.SEVERE,
    )

    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        logger.log(levelMap[priority]!!, message)
    }
}