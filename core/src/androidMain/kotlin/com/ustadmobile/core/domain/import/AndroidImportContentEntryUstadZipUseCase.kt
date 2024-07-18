package com.ustadmobile.core.domain.import

import android.content.Context
import android.net.Uri
import kotlinx.serialization.json.Json
import java.util.zip.ZipInputStream
import com.ustadmobile.core.db.dao.ContentEntryDao

class AndroidImportContentEntryUstadZipUseCase(
    private val context: Context,
    contentEntryDao: ContentEntryDao,
    json: Json
) : CommonJvmImportContentEntryUstadZipUseCase(contentEntryDao, json) {

    override fun openInputStream(sourceZipFilePath: String): ZipInputStream {
        val uri = Uri.parse(sourceZipFilePath)
        val inputStream = context.contentResolver.openInputStream(uri)
        return ZipInputStream(inputStream)
    }
}
