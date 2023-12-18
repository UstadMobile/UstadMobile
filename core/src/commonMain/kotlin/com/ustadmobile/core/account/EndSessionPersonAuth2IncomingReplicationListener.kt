package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase

/**
 * SyncListener that will end any other session on other devices when a user changes their password.
 *
 * TODO: convert this to using door message
 */
@Suppress("Unused") //This is actually used in UmRestApplication
class EndSessionPersonAuth2IncomingReplicationListener(
    private val db: UmAppDatabase
) {

    suspend fun onIncomingReplicationProcessed() {
        /*
        if(incomingReplicationEvent.tableId != PersonAuth2.TABLE_ID)
            return

        db.withDoorTransactionAsync { txDb ->
            incomingReplicationEvent.incomingReplicationData.forEach {
                val jsonObj = it.jsonObject
                val authUid = jsonObj["pauthUid"]?.jsonPrimitive?.long ?: return@forEach
                val pauthLcb = jsonObj["pauthLcb"]?.jsonPrimitive?.long ?: return@forEach

                Napier.i("PersonAuth changed: end other sessions for Person uid $authUid" +
                    " except from node id $pauthLcb (where password was changed)")
                val numSessionsEnded = txDb.userSessionDao.endOtherSessions(authUid, pauthLcb,
                    UserSession.STATUS_NEEDS_REAUTH, UserSession.REASON_PASSWORD_CHANGED,
                    systemTimeInMillis())
            }
        }
        */
    }
}