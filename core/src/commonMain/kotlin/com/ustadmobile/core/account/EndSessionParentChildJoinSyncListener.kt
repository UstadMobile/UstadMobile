package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.SyncEntitiesReceivedEvent
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.PersonParentJoin
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Listen for changes to parental consent. If consent is revoked, then end any sessions that are
 * active for the child.
 *
 * TODO: convert to taking place as part of the DoorMessage callback
 */
class EndSessionParentChildJoinSyncListener(
    private val repo: UmAppDatabase
) {

    fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<PersonParentJoin>) {
        val consentRevokedChildPersonUidList = evt.entitiesReceived.filter {
            it.ppjParentPersonUid != 0L && it.ppjStatus != PersonParentJoin.STATUS_APPROVED
        }.map {
            it.ppjMinorPersonUid
        }

        GlobalScope.takeIf { consentRevokedChildPersonUidList.isNotEmpty() }?.launch {
            consentRevokedChildPersonUidList.forEach { childPersonUid ->
                repo.userSessionDao().endOtherSessions(childPersonUid, 0,
                    UserSession.STATUS_LOGGED_OUT, UserSession.REASON_CONSENT_REVOKED,
                    systemTimeInMillis())
            }
        }

    }
}
