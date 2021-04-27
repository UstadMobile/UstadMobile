package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzAssignmentDetailOverviewView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.CommentsWithPerson
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class ClazzAssignmentDetailOverviewPresenter(context: Any,
                                             arguments: Map<String, String>, view: ClazzAssignmentDetailOverviewView,
                                             lifecycleOwner: DoorLifecycleOwner,
                                             di: DI)
    : UstadDetailPresenter<ClazzAssignmentDetailOverviewView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        val clazzUid = withTimeoutOrNull(2000) {
            repo.clazzAssignmentDao.findByUidAsync(arguments[ARG_ENTITY_UID]?.toLong()
                    ?: 0)?.caClazzUid
        } ?: 0L

        return db.clazzDao.personHasPermissionWithClazz(accountManager.activeAccount.personUid,
                clazzUid, Role.PERMISSION_ASSIGNMENT_UPDATE)
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        /**
         * TODO
         *  1. clazzAssignment Detail
         *  2. clazzTimeZone
         *  3. clazzAssignmentContentEntryJoin
         *  4. Class Comments
         *  5. Private Comments
         */

        return null
    }

    fun handleSubmitComment(commentsWithPerson: CommentsWithPerson){

    }

    companion object {

        //TODO: Add constants for keys that would be used for any One To Many Join helpers

    }

}