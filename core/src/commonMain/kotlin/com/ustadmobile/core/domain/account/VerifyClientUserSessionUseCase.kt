package com.ustadmobile.core.domain.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.NodeIdAuthCache

/**
 * Use to verify that the given client as per a nodeId and NodeAuth has a valid userSession
 * for a given accountPersonUid
 */
class VerifyClientUserSessionUseCase(
    private val nodeIdAndAuthCache: NodeIdAuthCache,
    private val db: UmAppDatabase,
) {

    suspend operator fun invoke(
        fromNodeId: Long,
        nodeAuth: String,
        accountPersonUid: Long,
    ) {
        if(!nodeIdAndAuthCache.verify(fromNodeId, nodeAuth)) {
            throw IllegalArgumentException("Invalid nodeId/nodeauth")
        }

        if(accountPersonUid == 0L)
            throw IllegalArgumentException("verifyclientsession: accountPersonUid = 0")

        //nodeActiveUserUid must have an active session
        val sessionsForUser = db.userSessionDao().countActiveSessionsForUserAndNode(
            personUid = accountPersonUid,
            nodeId = fromNodeId,
        )

        if(sessionsForUser < 1) {
            throw IllegalStateException("User $accountPersonUid does not have an active session on $fromNodeId")
        }
    }

}