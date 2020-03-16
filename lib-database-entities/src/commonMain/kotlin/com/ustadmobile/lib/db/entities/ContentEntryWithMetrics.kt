package com.ustadmobile.lib.db.entities

import kotlinx.serialization.Serializable

@Serializable
class ContentEntryWithMetrics() : ContentEntry() {

    //Progress
    var contentEntryWithMetricsProgress: Double = 0.0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        if (!super.equals(other)) return false

        other as ContentEntryWithMetrics

        if (contentEntryWithMetricsProgress != other.contentEntryWithMetricsProgress) return false

        return true
    }

    constructor(ce: ContentEntry) : this() {
        contentEntryUid = ce.contentEntryUid
        title = ce.title
        description = ce.description
        entryId = ce.entryId
        author = ce.author
        publisher = ce.publisher
        licenseType = ce.licenseType
        licenseName = ce.licenseName
        licenseUrl = ce.licenseUrl
        sourceUrl = ce.sourceUrl
        thumbnailUrl = ce.thumbnailUrl
        lastModified = ce.lastModified
        primaryLanguageUid = ce.primaryLanguageUid
        languageVariantUid = ce.languageVariantUid
        contentFlags = ce.contentFlags
        leaf = ce.leaf
        publik = ce.publik
        ceInactive = ce.ceInactive
        contentTypeFlag = ce.contentTypeFlag
        contentEntryLocalChangeSeqNum = ce.contentEntryLocalChangeSeqNum
        contentEntryMasterChangeSeqNum = ce.contentEntryMasterChangeSeqNum
        contentEntryLastChangedBy = ce.contentEntryLastChangedBy

    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contentEntryWithMetricsProgress.hashCode()
        return result
    }


}
