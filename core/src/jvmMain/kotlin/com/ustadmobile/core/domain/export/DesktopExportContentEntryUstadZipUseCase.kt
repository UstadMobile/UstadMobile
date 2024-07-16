package com.ustadmobile.core.domain.export

import com.ustadmobile.core.db.dao.ContentEntryDao
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipOutputStream

class DesktopExportContentEntryUstadZipUseCase(
    contentEntryDao: ContentEntryDao,
    json: Json
) : CommonJvmExportContentEntryUstadZipUseCase(contentEntryDao, json) {

    override fun openOutputStream(destZipFilePath: String): ZipOutputStream {
        return ZipOutputStream(FileOutputStream(destZipFilePath))
    }

    override fun getOutputDirectory(): String {
        return System.getProperty("user.home")
    }
}
