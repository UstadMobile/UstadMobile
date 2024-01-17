package com.ustadmobile.core.io.ext

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toDoorUri
import kotlinx.io.files.Path
import java.io.File

fun Path.toDoorUri(): DoorUri {
    return File(this.toString()).toDoorUri()
}
