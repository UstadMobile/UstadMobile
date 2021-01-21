package com.ustadmobile.door.attachments

import com.ustadmobile.door.DoorDatabaseRepository
import java.io.File

fun DoorDatabaseRepository.requireAttachmentDirFile(): File {
    return attachmentsDir?.let { File(it) }
            ?: throw IllegalStateException("requireAttachmentDirFile called on repository with null attachment dir")
}
