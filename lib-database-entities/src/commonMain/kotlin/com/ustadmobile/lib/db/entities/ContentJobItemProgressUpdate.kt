package com.ustadmobile.lib.db.entities

/**
 * This is a utility class that holds only the information needed to update progress on a
 * ContentJobItem
 */
class ContentJobItemProgressUpdate(
    val cjiUid: Long,
    val cjiProgress: Long = 0,
    val cjiTotal: Long = 0
) {
    override fun equals(other: Any?) = (other as? ContentJobItemProgressUpdate)?.cjiUid == cjiUid

    override fun hashCode() = cjiUid.hashCode()
}


fun ContentJobItem.toProgressUpdate() = ContentJobItemProgressUpdate(cjiUid,
    cjiProgress, cjiTotal)