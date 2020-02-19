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

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + contentEntryWithMetricsProgress.hashCode()
        return result
    }


}
