package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.util.ext.appendQueryArgs
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
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
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val entry = withContext(Dispatchers.Default) {
            db.onRepoWithFallbackToDb(2000) { it.clazzAssignmentDao.findByUidAsync(entityUid) }
        } ?: ClazzAssignment().apply {
            caUid = entityUid
        }

        setupTabs(entry)

        return entry
    }


    private fun setupTabs(assignment: ClazzAssignment) {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        presenterScope.launch {
            val loggedInPersonUid = accountManager.activeAccount.personUid
            val hasStudentProgressPermission = db.clazzDao.personHasPermissionWithClazz(
                loggedInPersonUid, assignment.caClazzUid, Role.PERMISSION_PERSON_LEARNINGRECORD_SELECT)

            val commonArgs = mapOf(UstadView.ARG_NAV_CHILD to true.toString(),
                    ARG_ENTITY_UID to entityUid.toString(),
                    ARG_CLAZZUID to assignment.caClazzUid.toString())

            val coreTabs = mutableListOf(
                    ClazzAssignmentDetailOverviewView.VIEW_NAME.appendQueryArgs(commonArgs))
            if(hasStudentProgressPermission){
                coreTabs += ClazzAssignmentDetailStudentProgressOverviewListView.VIEW_NAME.appendQueryArgs(commonArgs)
            }

            view.tabs = coreTabs
        }
    }


}


