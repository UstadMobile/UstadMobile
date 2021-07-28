package com.ustadmobile.core.io.ext

import com.ustadmobile.core.contentjob.ProcessContext
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.ext.toFile
import com.ustadmobile.door.ext.writeToFile
import com.ustadmobile.lib.contentscrapers.ScraperConstants
import io.ktor.client.call.*
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.URLDecoder
import java.nio.file.Files
import java.nio.file.Paths

actual suspend fun DoorUri.guessMimeType(): String? {
    return Files.probeContentType(Paths.get(this.uri))
}

actual suspend fun DoorUri.getSize(context: Any): Long {
    return this.toFile().length()
}

actual suspend fun DoorUri.downloadUrl(processContext: ProcessContext): DoorUri {
    if(this.uri.toString().startsWith("http")){

        if(processContext.params.containsKey("fileLocation")){
            return DoorUri.parse(processContext.params.getValue("fileLocation"))
        }

        val tempDir = Files.createTempDirectory("folder").toFile()
        tempDir.mkdir()

        val urlName = uri.toString().substringAfterLast("/")
        val contentFile = File(tempDir, urlName)

        val okRequest = Request.Builder().url(uri.toString()).build()
        val response = okHttpClient.newCall(okRequest).execute()
        response.body?.byteStream()?.writeToFile(contentFile)

        processContext.params["fileLocation"] = contentFile.toURI().toString()

        return DoorUri.parse(contentFile.toURI().toString())

    }
    return this
}
