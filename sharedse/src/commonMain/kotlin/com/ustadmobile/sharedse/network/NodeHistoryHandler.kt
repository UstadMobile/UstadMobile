package com.ustadmobile.sharedse.network

val NODE_EVT_TYPE_FAIL = 1
val NODE_EVT_TYPE_SUCCESS = 2

typealias NodeHistoryHandler = (nodeAddr: String, evtType: Int) -> Unit
