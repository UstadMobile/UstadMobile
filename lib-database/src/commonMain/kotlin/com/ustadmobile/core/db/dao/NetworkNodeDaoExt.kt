package com.ustadmobile.core.db.dao

suspend fun NetworkNodeDao.updateNodeLastSeen(knownNodes: Map<String, Long>) {
    val nodeIterator = knownNodes.entries.iterator()
    while (nodeIterator.hasNext()) {
        val nodeUpdates = nodeIterator.next()
        updateLastSeenAsync(nodeUpdates.key, nodeUpdates.value)
    }
}