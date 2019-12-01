package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error

actual interface FetchMpp {
    actual fun enqueue(request: RequestMpp, func: FuncMpp<RequestMpp>?, func2: FuncMpp<Error>?): FetchMpp

    actual fun enqueue(requests: List<RequestMpp>, func: FuncMpp<List<Pair<RequestMpp, Error>>>?): FetchMpp




    actual fun addListener(listener: FetchListenerMpp): FetchMpp

    actual fun removeListener(listener: FetchListenerMpp): FetchMpp



}