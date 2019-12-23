package com.ustadmobile.lib.db.entities

class NetworkNodeWithStatusResponsesAndHistory : NetworkNode() {

    val statusResponses: MutableMap<Long, EntryStatusResponse> = mutableMapOf()

    val nodeFailures: MutableList<Long> = mutableListOf()

}