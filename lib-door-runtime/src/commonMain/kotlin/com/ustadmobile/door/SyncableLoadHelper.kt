package com.ustadmobile.door

class SyncableLoadHelper<T>(val mirrorProvider: MirrorProvider,
                            val loadFn: suspend(endpoint: String) -> T) {

    class RequestResult<T>(val result: T?, val numLoaded: Int)

    suspend fun doRequest() : RequestResult<T> {
        val endpointToUse = mirrorProvider.getMirrorEndpoints().firstOrNull()
        if(endpointToUse != null) {
            val t = loadFn(endpointToUse.endpointUrl)
            if(t is List<*>) {
                return RequestResult(t, t.size)
            }else {
                return RequestResult(t, if(t != null) 1 else 0)
            }
        }
;
        return RequestResult(null, -1)
    }
}