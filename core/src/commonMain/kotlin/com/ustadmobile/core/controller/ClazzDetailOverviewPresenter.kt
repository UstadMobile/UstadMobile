package com.ustadmobile.core.controller

import com.ustadmobile.core.account.UstadAccountManager
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.ext.toDeepLink
import com.ustadmobile.core.view.ClazzAssignmentDetailView
import com.ustadmobile.core.view.ClazzDetailOverviewView
import com.ustadmobile.core.view.ClazzDetailView
import com.ustadmobile.core.view.ClazzEdit2View
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.direct
import org.kodein.di.instance


class ClazzDetailOverviewPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ClazzDetailOverviewView,
    di: DI,
    lifecycleOwner: DoorLifecycleOwner,
    val contentEntryListItemListener: DefaultContentEntryListItemListener =
        DefaultContentEntryListItemListener(
            view = view,
            context = context,
            di = di,
            clazzUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        )
) : UstadDetailPresenter<ClazzDetailOverviewView, ClazzWithDisplayDetails>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner
), ContentEntryListItemListener by contentEntryListItemListener {


    var collapsedList: MutableSet<Long> = mutableSetOf()

    val deepLink: String
        get() {
            val activeEndpoint = di.direct.instance<UstadAccountManager>().activeAccount.endpointUrl
            return arguments.toDeepLink(activeEndpoint, ClazzDetailView.VIEW_NAME)
        }

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.LIVEDATA

    override suspend fun onCheckEditPermission(account: UmAccount?): Boolean {
        return db.clazzDao.personHasPermissionWithClazz(account?.personUid ?: 0L,
                arguments[ARG_ENTITY_UID]?.toLong() ?: 0L, Role.PERMISSION_CLAZZ_UPDATE)
    }

    override fun onLoadLiveData(repo: UmAppDatabase): DoorLiveData<ClazzWithDisplayDetails?>? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        view.scheduleList = repo.scheduleDao.findAllSchedulesByClazzUid(entityUid)
        presenterScope.launch {
            view.clazzCodeVisible = repo.clazzDao.personHasPermissionWithClazz(
                    accountManager.activeAccount.personUid, entityUid,
                    Role.PERMISSION_CLAZZ_ADD_STUDENT)
            view.courseBlockList = repo.courseBlockDao.findAllCourseBlockByClazzUidLive(
                entityUid, accountManager.activeAccount.personUid,
                collapsedList.toList(), systemTimeInMillis())
        }

        return repo.clazzDao.getClazzWithDisplayDetails(entityUid, systemTimeInMillis())
    }

    override fun handleClickEdit() {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        navigateForResult(
            NavigateForResultOptions(this,
                null, ClazzEdit2View.VIEW_NAME, ClazzWithHolidayCalendarAndSchool::class,
                ClazzWithHolidayCalendarAndSchool.serializer(), SAVEDSTATE_KEY_CLAZZ,
                arguments = mutableMapOf(ARG_ENTITY_UID to entityUid.toString())
            )
        )
    }

    fun handleModuleExpandCollapseClicked(courseBlock: CourseBlock){
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0L
        val foundBlock: Long? = collapsedList.find { it == courseBlock.cbUid }
        if(foundBlock != null){
            collapsedList.remove(foundBlock)
        }else{
            collapsedList.add(courseBlock.cbUid)
        }
        view.courseBlockList = repo.courseBlockDao.findAllCourseBlockByClazzUidLive(
            entityUid, accountManager.activeAccount.personUid,
            collapsedList.toList(), systemTimeInMillis())
    }

    fun handleClickAssignment(assignment: ClazzAssignment){
        ustadNavController.navigate(
            ClazzAssignmentDetailView.VIEW_NAME,
            mapOf(ARG_ENTITY_UID to assignment.caUid.toString()))
    }

    fun handleDownloadAllClicked() {

    }


    companion object {
        const val SAVEDSTATE_KEY_CLAZZ = "Clazz"
    }

}