package com.ustadmobile.core.domain.contententry.importcontent

import com.ustadmobile.lib.db.entities.ContentEntryImportJob
import kotlinx.serialization.Serializable

/**
 * Simple wrapper class that will be used to send ContentJob and ContentJobItem to server for
 * processing.
 */
@Serializable
data class ImportRequest(
    val contentJobItem: ContentEntryImportJob,
)
