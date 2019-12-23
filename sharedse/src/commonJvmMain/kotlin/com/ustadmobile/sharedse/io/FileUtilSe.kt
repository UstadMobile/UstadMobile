package com.ustadmobile.sharedse.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

actual suspend fun extractResourceToFile(resourcePath: String, filePath: String) = withContext(Dispatchers.IO){
    Any::class.java.getResourceAsStream(resourcePath).use { input ->
        FileOutputStream(filePath).use { output ->
            input!!.copyTo(output)
        }
    }
    Unit
}
