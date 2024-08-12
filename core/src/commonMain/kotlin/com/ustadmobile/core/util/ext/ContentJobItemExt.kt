package com.ustadmobile.core.util.ext

import com.ustadmobile.door.DoorUri
import com.ustadmobile.lib.db.entities.ContentEntryImportJob



fun ContentEntryImportJob?.requireSourceAsDoorUri(): DoorUri {
    return this?.sourceUri?.let { DoorUri.parse(it) }
        ?: throw IllegalArgumentException("requireSourceAsDoorUri: SourceUri is null!")
}

