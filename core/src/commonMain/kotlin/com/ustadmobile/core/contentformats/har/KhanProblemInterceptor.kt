package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.io.ext.getStringFromContainerEntry
import com.ustadmobile.core.util.ext.isTextContent

class KhanProblemInterceptor : HarInterceptor() {

    override suspend fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        if (request.regexedUrl?.contains("getAssessmentItem") == false){
            return response
        }

        val harList = harContainer.requestMap[(Pair(request.method, "https://www.khanacademy.org/getAssessmentItem"))]


        val harEntry = if(request.regexedUrl?.contains("https://www.khanacademy.org/getAssessmentItem") == true){
            harList?.removeAt(0) ?: return response
        }else{
            harList?.get(0) ?: return response
        }

        if(harList.isEmpty()){
            harList.add(harEntry)
        }

        val harResponse = harEntry.response ?: return response
        val harText = harResponse.content?.text ?: ""

        val containerEntryFile = harContainer.db.containerEntryDao.findByPathInContainer(
                harContainer.containerUid, harText)?.containerEntryFile ?: harResponse.content?.entryFile

        if (containerEntryFile == null) {
            harResponse.status = 402
            harResponse.statusText = "Not Found"
            harResponse.content = null
            return harResponse
        }


        val mutMap = harContainer.getHeaderMap(harResponse.headers, containerEntryFile)
        harResponse.headers = mutMap.map { HarNameValuePair(it.key, it.value) }

        if(harResponse.content?.isTextContent() == true){
            harResponse.content?.text = containerEntryFile.getStringFromContainerEntry()
        }
        harResponse.content?.entryFile = containerEntryFile

        return harResponse
    }


}
