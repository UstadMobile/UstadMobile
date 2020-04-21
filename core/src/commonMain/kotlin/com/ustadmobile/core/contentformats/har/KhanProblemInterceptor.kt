package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.util.UMIOUtils
import kotlinx.io.ByteArrayInputStream
import kotlinx.serialization.toUtf8Bytes

class KhanProblemInterceptor : HarInterceptor() {

    override fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        if (request.url?.contains("getAssessmentItem") == false) {
            return response
        }

        val harList = harContainer.requestMap[(Pair(request.method, request.url))]

        val harEntry = harList?.removeAt(0) ?: return response

        // khan academy ignores the 2nd request that comes in when loading the page so i need to put it back on the list
        if (harList.size == 5) {
            harList.add(0, harEntry)
        }

        val harResponse = harEntry.response ?: return response
        val harText = harResponse.content?.text

        val containerEntry = harContainer.containerManager.getEntry(harText
                ?: "") ?: return response

        val entryFile = containerEntry.containerEntryFile
        if (entryFile == null) {
            harResponse.status = 402
            harResponse.statusText = "Not Found"

            harResponse.content = null
            return harResponse
        }

        val data = harContainer.containerManager.getInputStream(containerEntry)

        harResponse.content?.data = data

        val mutMap = harContainer.getHeaderMap(harResponse.headers, entryFile)
        harResponse.headers = mutMap.map { HarNameValuePair(it.key, it.value) }

        return harResponse
    }


}