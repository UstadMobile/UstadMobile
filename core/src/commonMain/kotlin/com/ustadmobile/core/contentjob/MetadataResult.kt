package com.ustadmobile.core.contentjob

import com.ustadmobile.lib.db.entities.ContentEntryWithLanguage
import kotlinx.serialization.Serializable

@Serializable
data class MetadataResult(
    val entry: ContentEntryWithLanguage,

    /**
     * The pluginId that provided this metadata
     */
    val pluginId: Int
)