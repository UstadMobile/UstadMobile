package com.ustadmobile.sharedse.network.fetch

import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2core.Func

//class FetchMppAdaptor(val fetch: Fetch): FetchMpp {
//
//    override fun enqueue(requests: List<RequestMpp>, onEnqueue: (List<Pair<RequestMpp, Int>>) -> Unit): FetchMpp {
//        fetch.enqueue(requests, object: Func<List<Pair<RequestMpp, Error>>> {
//            override fun call(result: List<Pair<RequestMpp, Error>>) {
//                onEnqueue.invoke(result.map { Pair(it.first, it.second.value) })
//            }
//        })
//
//        return this
//    }
//}