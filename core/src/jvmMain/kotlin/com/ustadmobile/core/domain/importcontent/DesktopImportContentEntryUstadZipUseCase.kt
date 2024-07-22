package com.ustadmobile.core.domain.import

import com.ustadmobile.core.db.dao.ContentEntryDao
import kotlinx.serialization.json.Json
import java.io.FileInputStream
import java.util.zip.ZipInputStream

class DesktopImportContentEntryUstadZipUseCase(
    contentEntryDao: ContentEntryDao,
    json: Json
) : CommonJvmImportContentEntryUstadZipUseCase(contentEntryDao, json) {

    override fun openInputStream(sourceZipFilePath: String): ZipInputStream {
        return ZipInputStream(FileInputStream(sourceZipFilePath))
    }
}
