package com.ustadmobile.sharedse.io

import kotlinx.io.InputStream
import kotlinx.io.core.Input
import kotlinx.io.core.Output

expect fun createFileOutputWritableChannel(filePath: String, append: Boolean = false): Output

expect fun inputStreamAsInput(inputStream: InputStream): Input
