package com.ustadmobile.core.controller

import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.view.SelectExtractFileView
import io.github.aakira.napier.Napier
import kotlinx.browser.window
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.await
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import org.w3c.files.Blob
import org.w3c.xhr.FormData
import org.w3c.xhr.XMLHttpRequest


actual class SelectExtractFilePresenter actual constructor(
    context: Any,
    arguments: Map<String, String>,
    view: SelectExtractFileView,
    di: DI,
) : SelectExtractFilePresenterCommon(context, arguments, view, di){

    private val json: Json by instance()

    override suspend fun extractMetadata(uri: String, fileName: String): MetadataResult {
        //turn the object url back into a blob
        val completeableMetadataResult = CompletableDeferred<MetadataResult>()
        Napier.d { "SelectExtractFilePresenter: Reading uri : $uri" }
        val blob: Blob = window.fetch(uri).await().blob().await()

        Napier.d { "SelectExtractFilePresenter: starting upload : $uri" }
        val formData = FormData()
        formData.append("file", blob, filename = fileName)
        val request = XMLHttpRequest()
        val endPoint = "${accountManager.activeEndpoint.url}contentupload/upload"
        request.open("POST", endPoint)
        request.send(formData)
        request.onreadystatechange = {
            if(request.readyState.toInt() == 4) {
                Napier.d { "SelectExtractFilePresenter: response ready "}
                val response = json.decodeFromString(MetadataResult.serializer(),
                    request.responseText)
                completeableMetadataResult.complete(response)
            }
        }

        return completeableMetadataResult.await()
    }
}
