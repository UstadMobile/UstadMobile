package com.ustadmobile.door.attachments

/**
 * Shorthand for tableName/attachmentMd5. The attachment should be stored in (repo attachment dir)/relativePath.
 */
internal val EntityWithAttachment.relativePath: String
    get() = "$tableName/$attachmentMd5"

/**
 *
 */
internal fun EntityWithAttachment.makeAttachmentUriFromTableNameAndMd5(): String = "door-attachment://$tableName/$attachmentMd5"
