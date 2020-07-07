package com.ustadmobile.core.contentformats.har

import com.ustadmobile.core.util.UMIOUtils
import kotlinx.io.ByteArrayInputStream
import kotlinx.serialization.toUtf8Bytes

@ExperimentalStdlibApi
class RecorderInterceptor : HarInterceptor() {

    override fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        if (response.content?.mimeType?.contains("text/html") == false) {
            return response
        }

        val head = Regex("(<head([^>]*)>)")

        val input = response.content?.data ?: return response
        var data = UMIOUtils.readStreamToString(input)
        data = data.replace(head, "$1$jsInject")
        response.content?.data = ByteArrayInputStream(data.encodeToByteArray())

        return response
    }

    private val worker = """
    
     const constantMock = window.fetch;
        window.fetch = function() {
            // Get the parameter in arguments
            // Intercept the parameter here
            
             if(arguments.length > 0){
                 var request = arguments[0].clone()
                console.log(request.bodyUsed)
                if (request.method !== 'GET' && request.method !== 'HEAD') { 
                    request.text().then(function(body) {
                            console.log(body)
                            recorder.recordPayload(request.method, request.url, body);
                    });
                }
            }

            return constantMock.apply(this, arguments);
        };
"""

    private val jsInject = "<script type=\"text/javascript\">$worker</script>"

}