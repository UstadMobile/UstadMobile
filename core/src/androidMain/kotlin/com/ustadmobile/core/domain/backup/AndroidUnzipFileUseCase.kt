package com.ustadmobile.core.domain.backup

import android.content.Context
import android.net.Uri
import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream


class AndroidUnzipFileUseCase(private val context: Context) : CommonJvmUnzipFileUseCase() {
    override fun openInputStream(path: String): InputStream {
        val uri = Uri.parse(path)
        return context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream for URI: $path")
    }

    override fun getOutputDirectory(): String {
        return context.getExternalFilesDir(null)?.absolutePath
            ?: context.filesDir.absolutePath
    }
}