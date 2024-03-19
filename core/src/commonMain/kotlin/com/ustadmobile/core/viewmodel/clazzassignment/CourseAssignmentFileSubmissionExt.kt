package com.ustadmobile.core.viewmodel.clazzassignment

import com.ustadmobile.core.domain.blob.openblob.OpenBlobItem
import com.ustadmobile.lib.db.entities.CourseAssignmentSubmissionFile

fun CourseAssignmentSubmissionFile.asBlobOpenItem() = OpenBlobItem(
    uri = casaUri ?: "",
    mimeType = casaMimeType ?: "application/octet-stream",
    fileName = casaFileName ?: "",
    fileSize = casaSize.toLong()
)
