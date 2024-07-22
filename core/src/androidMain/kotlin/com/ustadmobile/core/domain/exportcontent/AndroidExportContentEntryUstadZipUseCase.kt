package com.ustadmobile.core.domain.export

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.ustadmobile.core.db.dao.ContentEntryDao
import kotlinx.serialization.json.Json
import java.io.File
import java.util.zip.ZipOutputStream

class AndroidExportContentEntryUstadZipUseCase(
    private val context: Context,
    contentEntryDao: ContentEntryDao,
    json: Json
) : CommonJvmExportContentEntryUstadZipUseCase(contentEntryDao, json) {
    override fun openOutputStream(destZipFilePath: String): ZipOutputStream {
        val uri = Uri.parse(destZipFilePath)
        return ZipOutputStream(context.contentResolver.openOutputStream(uri))
    }

    override fun getOutputDirectory(): String {
        return context.getExternalFilesDir(null)?.absolutePath
            ?: context.filesDir.absolutePath
    }
}
