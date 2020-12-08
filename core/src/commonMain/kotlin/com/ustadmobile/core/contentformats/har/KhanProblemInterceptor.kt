package com.ustadmobile.core.contentformats.har



@OptIn(ExperimentalStdlibApi::class)
class KhanProblemInterceptor : HarInterceptor() {

    override fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

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
