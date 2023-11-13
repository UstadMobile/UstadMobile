package com.ustadmobile.hooks

import com.ustadmobile.door.DoorUri

/**
 * Takes an attachment Uri (this can be attachemnt:/// or a file/other path when editing is in
 * progress. Returns a Uri that can be used as an src.
 */
@Deprecated("Attachments are gone")
fun useAttachmentUriSrc(
    attachmentUri: String?,
    revokeOnCleanup: Boolean = false,
): DoorUri? {
    return null
}

