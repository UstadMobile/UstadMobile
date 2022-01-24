package com.ustadmobile.core.assignment

import com.ustadmobile.core.account.Endpoint
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.lib.db.entities.ClazzAssignment
import org.kodein.di.DI

fun UmAppDatabase.setupAssignmentSyncListener(site: Endpoint, di: DI) : UmAppDatabase{
    val listener = ClazzAssignmentSyncListener(site, di)
    //(this as DoorDatabaseRepository).addSyncListener(ClazzAssignment::class, listener.assignmentListener)
    return this
}