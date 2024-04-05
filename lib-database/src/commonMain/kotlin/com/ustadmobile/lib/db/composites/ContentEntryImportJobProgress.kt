package com.ustadmobile.lib.db.composites

data class ContentEntryImportJobProgress(
    var cjiUid: Long = 0,

    /**
     * Represents the progress on this item
     */
    var cjiItemProgress: Long = 0,

    /**
     * Represents the total to process on this item
     */
    var cjiItemTotal: Long = 0,

    var cjiStatus: Int = 4,

    var cjiError: String? = null,

)
