package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.view.ClazzWorkDetailView
import com.ustadmobile.core.view.ClazzWorkEditView
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzMember
import com.ustadmobile.lib.db.entities.ClazzWork
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.kodein.di.DI


class ClazzWorkDetailPresenter(context: Any,
                          arguments: Map<String, String>, view: ClazzWorkDetailView,
                           di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ClazzWorkDetailView, ClazzWork>(context, arguments, view, di, lifecycleOwner) {

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzWork? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L

        val clazzWork = withTimeoutOrNull(2000) {
            db.clazzWorkDao.findByUidAsync(entityUid)
        } ?: ClazzWork()

        view.ustadFragmentTitle = clazzWork.clazzWorkTitle

        val loggedInPersonUid = accountManager.activeAccount.personUid

        val clazzMember: ClazzMember? =
                db.clazzMemberDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                        entity?.clazzWorkClazzUid?: 0L)

        val loggedInPerson: Person? = withTimeoutOrNull(2000){
            db.personDao.findByUid(loggedInPersonUid)
        }
        if(loggedInPerson?.admin == true){
            view.isStudent = false
        }else {
            if(clazzMember == null){
                view.isStudent = true
            }else {
                view.isStudent = (clazzMember.clazzMemberRole != ClazzMember.ROLE_TEACHER)
            }
        }

        return clazzWork
    }

    override fun onCreate(savedState: Map<String, String>?) {
        val loggedInPersonUid = accountManager.activeAccount.personUid
        GlobalScope.launch(doorMainDispatcher()) {
            val clazzMember: ClazzMember? =
                    db.clazzMemberDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                            entity?.clazzWorkClazzUid?: 0L)
            if(clazzMember == null){
                view.isStudent = true
            }else{
                view.isStudent = clazzMember.clazzMemberRole != ClazzMember.ROLE_TEACHER
            }

        }

        super.onCreate(savedState)

    }

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        val loggedInPersonUid = accountManager.activeAccount.personUid

        val loggedInPerson: Person? = withTimeoutOrNull(2000){
            db.personDao.findByUid(loggedInPersonUid)
        }
        if(loggedInPerson?.admin == true){
            return true
        }

        val clazzMember: ClazzMember? = withTimeoutOrNull(2000) {
            db.clazzMemberDao.findByPersonUidAndClazzUidAsync(loggedInPersonUid,
                    entity?.clazzWorkClazzUid?: 0L)
        }
        if(clazzMember == null){
            return true
        }else{
            return clazzMember.clazzMemberRole != ClazzMember.ROLE_TEACHER
        }


    }

    override fun handleClickEdit() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        systemImpl.go(ClazzWorkEditView.VIEW_NAME, mapOf(ARG_ENTITY_UID to entityUid.toString()),
                context)
    }
}