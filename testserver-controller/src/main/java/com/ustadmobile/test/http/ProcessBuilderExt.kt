package com.ustadmobile.test.http

import java.io.InputStreamReader

fun Process.getOutputAsString(): String {
    val commandOutput = InputStreamReader(inputStream).readText().trimEnd()
    return commandOutput
}