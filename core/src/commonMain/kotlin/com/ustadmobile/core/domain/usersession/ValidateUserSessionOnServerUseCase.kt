package com.ustadmobile.core.domain.usersession

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.NodeIdAuthCache

/**
 * Use case for a server to validate client credentials
 *  1) Check that the door node id and node auth are valid
 *  2) Check that the given node has an active session for the given accountPersonUid
 */
class ValidateUserSessionOnServerUseCase(
    private val db: UmAppDatabase,
    private val nodeIdAuthCache: NodeIdAuthCache,
) {

    suspend operator fun invoke(
        nodeId: Long,
        nodeAuth: String,
        accountPersonUid: Long,
    ) {
        if(nodeId == 0L || accountPersonUid == 0L)
            throw IllegalArgumentException("Cannot validate session for nodeid = 0 or personuid = 0")

        if(!nodeIdAuthCache.verify(nodeId, nodeAuth)) {
            throw IllegalArgumentException("Invalid nodeId/nodeauth")
        }

        //nodeActiveUserUid must have an active session
        val sessionsForUser = db.userSessionDao().countActiveSessionsForUserAndNode(
            personUid = accountPersonUid,
            nodeId = nodeId,
        )

        if(sessionsForUser < 1) {
            throw IllegalArgumentException("User $nodeId does not have an active session on $nodeId")
        }
    }

}