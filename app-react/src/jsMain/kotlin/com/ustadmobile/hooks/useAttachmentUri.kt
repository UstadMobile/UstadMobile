package com.ustadmobile.hooks

import com.ustadmobile.core.hooks.useCoroutineScope
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.attachments.retrieveAttachment
import com.ustadmobile.door.ext.DoorTag
import kotlinx.coroutines.launch
import react.useEffect
import react.useState
import web.url.URL

/**
 * Takes an attachment Uri (this can be attachemnt:/// or a file/other path when editing is in
 * progress. Returns a Uri that can be used as an src.
 */
fun useAttachmentUriSrc(
    attachmentUri: String?,
    revokeOnCleanup: Boolean = false,
): DoorUri? {
    var attachmentSrc: DoorUri? by useState { null }
    val db = useActiveDatabase(DoorTag.TAG_DB)
    val scope = useCoroutineScope(attachmentUri)

    useEffect(attachmentUri) {
        if(attachmentUri == null) {
            attachmentSrc = null
            return@useEffect
        }

        var resolvedUrl: DoorUri? = null

        scope.launch {
            resolvedUrl = if(attachmentUri.startsWith(DoorDatabaseRepository.DOOR_ATTACHMENT_URI_PREFIX)) {
                db.retrieveAttachment(attachmentUri)
            }else {
                DoorUri.parse(attachmentUri)
            }
            attachmentSrc = resolvedUrl
        }

        cleanup {
            val revokeUri = resolvedUrl
            if(revokeOnCleanup && revokeUri != null && revokeUri.uri.protocol == "blob:"){
                URL.revokeObjectURL(revokeUri.toString())
            }
        }

    }

    return attachmentSrc
}

