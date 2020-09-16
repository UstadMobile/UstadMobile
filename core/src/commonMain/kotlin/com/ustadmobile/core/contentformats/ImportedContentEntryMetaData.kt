package com.ustadmobile.core.contentformats

import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.serialization.Serializable

@Serializable
data class ImportedContentEntryMetaData(var contentEntry: ContentEntryWithLanguage, var mimeType: String, var uri: String, var importMode: Int, var scraperType: String? = null)