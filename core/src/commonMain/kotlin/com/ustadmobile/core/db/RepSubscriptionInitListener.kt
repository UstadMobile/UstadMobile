package com.ustadmobile.core.db

import com.ustadmobile.core.util.ext.grantScopedPermission
import com.ustadmobile.core.util.ext.insertPersonAndGroup
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.replication.ReplicationSubscriptionManager
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.ScopedGrant
import com.ustadmobile.lib.db.entities.UserSession

/**
 * When the client connects to a remote node (e.g. the primary server or an intermediary device) then
 * we need to setup grants etc so that data will be replicated to it.
 *
 * After this runs, the ReplicationSubscriptionListener is going to call onNewDoorNode that should
 * find anything that needs to be replicated.
 *
 */
class RepSubscriptionInitListener : ReplicationSubscriptionManager.SubscriptionInitializedListener {

    override suspend fun onSubscriptionInitialized(
        repo: DoorDatabaseRepository,
        remoteNodeId: Long
    ) {
        (repo as UmAppDatabase).withDoorTransactionAsync { transactDb ->
            //create a virtual person and persongroup
            if(transactDb.personDao.findSystemAccount(remoteNodeId) == null) {
                val systemPerson = transactDb.insertPersonAndGroup(Person().apply {
                    this.username = repo.config.endpoint
                    this.personType = Person.TYPE_SYSTEM
                    this.dateOfBirth = remoteNodeId
                })

                transactDb.grantScopedPermission(systemPerson, Role.ALL_PERMISSIONS,
                    ScopedGrant.ALL_TABLES, ScopedGrant.ALL_ENTITIES)

                transactDb.userSessionDao.insertSession(UserSession().apply {
                    this.usClientNodeId = remoteNodeId
                    this.usPersonUid = systemPerson.personUid
                    this.usEndTime = Long.MAX_VALUE
                    this.usStartTime = systemTimeInMillis()
                    this.usStatus = UserSession.STATUS_ACTIVE
                    usSessionType = UserSession.TYPE_UPSTREAM
                })
            }
        }
    }
}