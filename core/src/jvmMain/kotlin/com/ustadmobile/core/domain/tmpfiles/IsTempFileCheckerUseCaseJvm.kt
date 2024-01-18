package com.ustadmobile.core.domain.tmpfiles

import com.ustadmobile.core.ext.isChildOf
import java.io.File
import java.net.URI
import java.nio.file.Paths

class IsTempFileCheckerUseCaseJvm(
    private val tmpRootDir: File,
) : IsTempFileCheckerUseCase {
    override fun invoke(uri: String): Boolean {
        val uriObj = URI(uri)
        return uriObj.scheme == "file" && Paths.get(uriObj).toFile().isChildOf(tmpRootDir)
    }
}