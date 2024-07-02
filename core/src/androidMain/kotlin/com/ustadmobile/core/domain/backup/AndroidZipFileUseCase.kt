package com.ustadmobile.core.domain.backup

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.InputStream


class AndroidZipFileUseCase(private val context: Context) : CommonJvmZipFileUseCase() {

    override fun openInputStream(uri: String): InputStream {
        return context.contentResolver.openInputStream(Uri.parse(uri))
            ?: throw IllegalArgumentException("Unable to open input stream for URI: $uri")
    }

    override fun createOutputFile(path: String): File {
        val file = File(context.getExternalFilesDir(null), path)
        file.parentFile?.mkdirs() // Ensure parent directories exist
        return file
    }
}
