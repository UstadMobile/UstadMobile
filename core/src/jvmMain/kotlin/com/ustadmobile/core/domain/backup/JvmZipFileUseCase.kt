package com.ustadmobile.core.domain.backup

import com.ustadmobile.core.util.ZipProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class JvmZipFileUseCase : CommonJvmZipFileUseCase() {
    override fun openInputStream(uri: String): InputStream {
        return try {
            Files.newInputStream(Paths.get(URI(uri)))
        } catch (e: Exception) {
            FileInputStream(uri)
        }
    }

    override fun createOutputFile(path: String): File {
        return File(path)
    }
}