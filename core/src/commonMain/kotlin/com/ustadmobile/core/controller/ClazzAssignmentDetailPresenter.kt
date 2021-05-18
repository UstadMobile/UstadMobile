package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzAssignment
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.*
import org.kodein.di.DI

class ClazzAssignmentDetailPresenter(context: Any,
                                  arguments: Map<String, String>, view: ClazzAssignmentDetailView, di: DI,
                                  lifecycleOwner: DoorLifecycleOwner)
    : UstadDetailPresenter<ClazzAssignmentDetailView, ClazzAssignment>(context, arguments, view, di, lifecycleOwner) {


    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return false
    }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onLoadFromJson(bundle: Map<String, String>): ClazzAssignment? {
        super.onLoadFromJson(bundle)

        val entityJsonStr = bundle[UstadEditView.ARG_ENTITY_JSON]
        var editEntity: ClazzAssignment? = null
        if (entityJsonStr != null) {
            editEntity = safeParse(di, ClazzAssignment.serializer(), entityJsonStr)
        } else {
            editEntity = ClazzAssignment()
        }

        setupTabs(editEntity)

        return editEntity
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ClazzAssignment? {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        val entry = withContext(Dispatchers.Default) {
            withTimeoutOrNull(2000) { db.clazzAssignmentDao.findByUidAsync(entityUid) }
        } ?: ClazzAssignment()

        setupTabs(entry)

        return entry
    }


    private fun setupTabs(assignment: ClazzAssignment) {
        val entityUid = arguments[UstadView.ARG_ENTITY_UID]?.toLong() ?: 0L
        GlobalScope.launch(doorMainDispatcher()) {
            val loggedInPersonUid = accountManager.activeAccount.personUid
            val hasPermission = db.clazzDao.personHasPermissionWithClazz(loggedInPersonUid,
                    assignment.caClazzUid, Role.PERMISSION_ASSIGNMENT_VIEWSTUDENTPROGRESS)
            if(hasPermission){
                view.tabs = listOf("${ClazzAssignmentDetailOverviewView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$entityUid",
                        "${ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$entityUid")
            }else{
                view.tabs = listOf("${ClazzAssignmentDetailOverviewView.VIEW_NAME}?${UstadView.ARG_ENTITY_UID}=$entityUid")
            }
        }
    }


}


