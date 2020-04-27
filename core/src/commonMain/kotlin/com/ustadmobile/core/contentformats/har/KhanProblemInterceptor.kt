package com.ustadmobile.core.contentformats.har

import kotlinx.serialization.Serializable

@Serializable
class ProblemData(val num_problem_size: Int)

class KhanProblemInterceptor : HarInterceptor() {

    var counter = 0

    override fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        if (request.url?.contains("getAssessmentItem") == false){
            return response
        }

     /*   if(request.url?.contains("attemptProblem") == true){

            val requestBody = request.body
            if(requestBody.isNullOrEmpty()) return response

            val assessmentUrl = "https://www.khanacademy.org/getAssessmentItem"
            val harList = harContainer.requestMap[(Pair("POST", assessmentUrl))]

            val body: KhanProblemBody = harContainer.json.parse(KhanProblemBody.serializer(), requestBody)

            val itemId = body.variables?.input?.assessmentItemId.toString()
            val entry = harList?.find { it.request?.headers?.contains(HarNameValuePair("itemId", itemId)) == true  }
            harList?.remove(entry)

            return response
        }*/


        val harList = harContainer.requestMap[(Pair(request.method, "https://www.khanacademy.org/getAssessmentItem"))]

        val harEntry = if(request.url?.contains("https://www.khanacademy.org/getAssessmentItem") == true){
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
