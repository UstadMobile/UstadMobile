package com.ustadmobile.core.domain.backup

import java.io.InputStream
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths


class JvmUnzipFileUseCase : CommonJvmUnzipFileUseCase() {
    override fun openInputStream(path: String): InputStream {
        val filePath = try {
            Paths.get(URI(path))
        } catch (e: Exception) {
            Paths.get(path)
        }
        return Files.newInputStream(filePath)
    }

    override fun getOutputDirectory(): String {
        return System.getProperty("user.home")
    }
}