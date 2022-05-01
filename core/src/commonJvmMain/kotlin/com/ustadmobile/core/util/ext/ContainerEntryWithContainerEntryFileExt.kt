package com.ustadmobile.core.util.ext

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.lib.db.entities.ContainerEntryWithContainerEntryFile
import com.ustadmobile.retriever.RetrieverRequest
import java.io.File
import java.net.URLEncoder

/**
 * Make a RetrieverRequest for this ContainerEntry .
 */
fun ContainerEntryWithContainerEntryFile.toRetrieverRequest(
    endpoint: Endpoint,
    destinationDir: File
): RetrieverRequest {
    val md5Base64 = containerEntryFile?.cefMd5
        ?: throw IllegalArgumentException("toRetrieverRequest: null md5 on $cePath !")
    val cefIntegrity = containerEntryFile?.cefIntegrity
        ?: throw IllegalArgumentException("toRetrieverRequest: null integrity on $cePath")
    val destFile = File(destinationDir, md5Base64.base64EncodedToHexString())
    return RetrieverRequest(
        endpoint.url("/Container/FileByMd5/${URLEncoder.encode(md5Base64, "UTF-8")}"),
            destFile.absolutePath, cefIntegrity)
}
