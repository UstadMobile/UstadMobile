package com.ustadmobile.core.domain.tmpfiles

import android.net.Uri
import androidx.core.net.toFile
import com.ustadmobile.core.ext.isChildOf
import java.io.File

/**
 * Temp files on Android are stored within a particular root directory
 */
class IsTempFileCheckerUseCaseAndroid(
    private val tmpDir: File
): IsTempFileCheckerUseCase {

    override fun invoke(uri: String): Boolean {
        val uriObj = Uri.parse(uri)
        if(uriObj.scheme != "file")
            return false

        return uriObj.toFile().isChildOf(tmpDir)
    }
}