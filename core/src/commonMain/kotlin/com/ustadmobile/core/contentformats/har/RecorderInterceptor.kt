package com.ustadmobile.core.contentformats.har


class RecorderInterceptor : HarInterceptor() {

    override suspend fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse {

        if (response.content?.mimeType?.contains("text/html") == false) {
            return response
        }

        val head = Regex("(<head([^>]*)>)")

        var data = response.content?.text ?: return response
        data = data.replace(head, "$1$jsInject")

        response.content?.text = data

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