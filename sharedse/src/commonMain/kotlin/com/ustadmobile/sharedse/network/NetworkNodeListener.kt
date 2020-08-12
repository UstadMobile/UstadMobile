package com.ustadmobile.sharedse.network

import com.ustadmobile.lib.db.entities.NetworkNode

/**
 *
 */
interface NetworkNodeListener {

    suspend fun onNewNodeDiscovered(node: NetworkNode)

    suspend fun onNodeLost(node: NetworkNode)

    suspend fun onNodeReputationChanged(node: NetworkNode, reputation: Int)

}