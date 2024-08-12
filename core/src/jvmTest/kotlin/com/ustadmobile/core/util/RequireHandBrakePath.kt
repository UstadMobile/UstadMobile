package com.ustadmobile.core.util

import com.ustadmobile.core.domain.compress.video.FindHandBrakeUseCase
import kotlinx.coroutines.runBlocking

private val handbrakeCommand: List<String>? by lazy {
    runBlocking {
        FindHandBrakeUseCase().invoke()!!.command
    }
}

fun requireHandBrakeCommand(): List<String> {
    return handbrakeCommand ?: throw IllegalStateException("Could not find HandBrakeCLI")
}