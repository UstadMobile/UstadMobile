package com.ustadmobile.sharedse.io

import io.ktor.utils.io.core.Input
import io.ktor.utils.io.core.Output
import kotlinx.io.InputStream

expect fun createFileOutputWritableChannel(filePath: String, append: Boolean = false): Output

expect fun inputStreamAsInput(inputStream: InputStream): Input
