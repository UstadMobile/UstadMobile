package com.ustadmobile.core.domain.blob.openblob

import web.dom.document

class OpenBlobUseCaseJs: OpenBlobUseCase {

    override suspend fun invoke(
        item: OpenBlobItem,
        onProgress: (bytesTransferred: Long, totalBytes: Long) ->  Unit,
    ) {
        val element = document.createElement("a")
        element.setAttribute("href", item.uri)
        element.setAttribute("download", item.fileName)
        element.style.display = "none"
        document.body.appendChild(element)
        element.click()
        document.body.removeChild(element)
    }
}