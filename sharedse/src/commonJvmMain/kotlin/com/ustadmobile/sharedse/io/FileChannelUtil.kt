package com.ustadmobile.sharedse.io

import kotlinx.io.core.Input
import kotlinx.io.nio.asOutput
import java.io.FileOutputStream
import kotlinx.io.InputStream
import kotlinx.io.streams.asInput

actual fun createFileOutputWritableChannel(filePath: String, append: Boolean) =  FileOutputStream(filePath, append).channel.asOutput()

actual fun inputStreamAsInput(inputStream: InputStream) = inputStream.asInput()
