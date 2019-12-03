package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error

expect interface FetchMpp {

    fun enqueue(request: RequestMpp, func: FuncMpp<RequestMpp>?, func2: FuncMpp<Error>?): FetchMpp

    fun pause(id: Int, func: FuncMpp<DownloadMpp>?, func2: FuncMpp<Error>?): FetchMpp

    fun resume(id: Int, func: FuncMpp<DownloadMpp>?, func2: FuncMpp<Error>?): FetchMpp

    fun enqueue(requests: List<RequestMpp>, func: FuncMpp<List<Pair<RequestMpp, Error>>>? = null): FetchMpp

    fun addListener(listener: FetchListenerMpp): FetchMpp

    fun removeListener(listener: FetchListenerMpp): FetchMpp


}