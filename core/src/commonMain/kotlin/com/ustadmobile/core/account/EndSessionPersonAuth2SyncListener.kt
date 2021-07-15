package com.ustadmobile.core.account

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.SyncEntitiesReceivedEvent
import com.ustadmobile.door.SyncListener
import com.ustadmobile.lib.db.entities.PersonAuth2
import com.ustadmobile.lib.db.entities.UserSession
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * SyncListener that will end any other session on other devices when a user changes their password.
 */
class EndSessionPersonAuth2SyncListener(
    private val repo: UmAppDatabase
): SyncListener<PersonAuth2> {

    override fun onEntitiesReceived(evt: SyncEntitiesReceivedEvent<PersonAuth2>) {
        GlobalScope.launch {
            evt.entitiesReceived.forEach {
                repo.userSessionDao.endOtherSessions(it.pauthUid, it.pauthLcb.toLong(),
                    UserSession.STATUS_NEEDS_REAUTH, UserSession.REASON_PASSWORD_CHANGED)
            }
        }
    }
}