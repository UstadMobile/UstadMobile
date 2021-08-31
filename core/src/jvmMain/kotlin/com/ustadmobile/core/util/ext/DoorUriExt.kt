package com.ustadmobile.core.util.ext

import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun DoorUri.emptyRecursively() {
    withContext(Dispatchers.IO) {
        toFile().listFiles()?.forEach {
            it.deleteRecursively()
        }
    }
}
