package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.IncomingReplicationEvent
import com.ustadmobile.door.IncomingReplicationListener
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PersonAuth2
import com.ustadmobile.lib.db.entities.UserSession
import io.github.aakira.napier.Napier
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

/**
 * SyncListener that will end any other session on other devices when a user changes their password.
 */
@Suppress("Unused") //This is actually used in UmRestApplication
class EndSessionPersonAuth2IncomingReplicationListener(
    private val db: UmAppDatabase
): IncomingReplicationListener{

    override suspend fun onIncomingReplicationProcessed(incomingReplicationEvent: IncomingReplicationEvent) {
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
    }
}