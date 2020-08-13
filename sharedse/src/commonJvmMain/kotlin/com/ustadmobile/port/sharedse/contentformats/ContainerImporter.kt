package com.ustadmobile.port.sharedse.contentformats

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.lib.db.entities.Container
import com.ustadmobile.lib.db.entities.ContentEntry
import com.ustadmobile.port.sharedse.contentformats.CONTENT_PLUGINS
import com.ustadmobile.port.sharedse.contentformats.ContentTypeFilePlugin
import java.io.File

interface ContainerImporter {

    suspend fun importContentEntryFromFile(contentEntryUid: Long, mimeType: String?, containerBaseDir: String,
                                           file: File, db: UmAppDatabase, dbRepo: UmAppDatabase, importMode: Int, context: Any): Container
}