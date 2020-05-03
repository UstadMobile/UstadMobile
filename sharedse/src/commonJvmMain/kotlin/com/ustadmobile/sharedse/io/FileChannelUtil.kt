package com.ustadmobile.sharedse.io

import io.ktor.utils.io.nio.asOutput
import java.io.FileOutputStream
import kotlinx.io.InputStream
import io.ktor.utils.io.streams.asInput

actual fun createFileOutputWritableChannel(filePath: String, append: Boolean) =  FileOutputStream(filePath, append).channel.asOutput()

actual fun inputStreamAsInput(inputStream: InputStream) = inputStream.asInput()
