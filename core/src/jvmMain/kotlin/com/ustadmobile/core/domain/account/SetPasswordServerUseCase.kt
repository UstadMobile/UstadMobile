package com.ustadmobile.core.domain.account

import com.ustadmobile.core.db.PermissionFlags
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.util.NodeIdAuthCache
import io.github.aakira.napier.Napier

/**
 * This use case handles requests from Javascript clients to change passwords (Javascript client
 * does not do local encryption). This endpoint validates the request to check:
 *
 * 1) The client has a valid node id and auth
 * 2) The active user has a valid session on the given node id
 * 3) The active user has permission to reset the password (if it is being reset without
 *    providing the existing password).
 *
 */
class SetPasswordServerUseCase(
    private val db: UmAppDatabase,
    private val setPasswordUseCase: SetPasswordUseCase,
    private val nodeIdAndAuthCache: NodeIdAuthCache,
) {

    suspend operator fun invoke(
        fromNodeId: Long,
        nodeAuth: String,
        nodeActiveUserUid: Long,
        personUid: Long,
        username: String,
        currentPassword: String?,
        newPassword: String
    ) {
        try {
            //Validate from node credentials and run permission check
            if(!nodeIdAndAuthCache.verify(fromNodeId, nodeAuth)) {
                throw IllegalArgumentException("Invalid nodeId/nodeauth")
            }
            //nodeActiveUserUid must have an active session
            val sessionsForUser = db.userSessionDao.countActiveSessionsForUserAndNode(
                personUid = nodeActiveUserUid,
                nodeId = fromNodeId,
            )

            if(sessionsForUser < 1) {
                throw IllegalArgumentException("User $nodeActiveUserUid does not have an active session on $fromNodeId")
            }

            if(currentPassword == null) {
                if(!db.systemPermissionDao.personHasSystemPermission(
                        accountPersonUid = nodeActiveUserUid,
                        permission = PermissionFlags.RESET_PASSWORDS,
                    )
                ) {
                    throw IllegalArgumentException("Password reset, but user does not have reset permission")
                }
            }

            setPasswordUseCase(
                activeUserPersonUid = nodeActiveUserUid,
                personUid = personUid,
                username = username,
                newPassword = newPassword,
                currentPassword = currentPassword,
            )
        }catch(e: Throwable) {
            Napier.w { "SetPasswordServerUseCase: Fail: ${e.message}" }
            throw e
        }
    }

}