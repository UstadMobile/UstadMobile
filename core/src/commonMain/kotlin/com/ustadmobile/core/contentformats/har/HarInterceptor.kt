package com.ustadmobile.core.contentformats.har

@ExperimentalStdlibApi
abstract class HarInterceptor {

    abstract fun intercept(request: HarRequest, response: HarResponse, harContainer: HarContainer, jsonArgs: String?): HarResponse

    companion object {

        const val KHAN_PROBLEM = "KhanProblemInterceptor"

        val interceptorMap = mapOf<String, HarInterceptor>(
                "RecorderInterceptor" to RecorderInterceptor(),
                KHAN_PROBLEM to KhanProblemInterceptor()
        )
    }

}