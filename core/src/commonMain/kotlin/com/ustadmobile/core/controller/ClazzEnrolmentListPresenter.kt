package com.ustadmobile.core.controller

import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.view.ClazzEnrolmentEditView
import com.ustadmobile.core.view.ClazzEnrolmentListView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.UstadView
import com.ustadmobile.door.lifecycle.LifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.ClazzEnrolmentWithLeavingReason
import com.ustadmobile.lib.db.entities.Role
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzEnrolmentListPresenter(
    context: Any,
    arguments: Map<String, String>,
    view: ClazzEnrolmentListView,
    di: DI,
    lifecycleOwner: LifecycleOwner
): UstadListPresenter<ClazzEnrolmentListView, ClazzEnrolment>(context, arguments, view, di, lifecycleOwner) {

    var selectedPerson = 0L
    var selectedClazz = 0L

    val loggedInPersonUid = accountManager.activeAccount.personUid

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedPerson = arguments[UstadView.ARG_PERSON_UID]?.toLong() ?: 0L
        selectedClazz = arguments[UstadView.ARG_CLAZZUID]?.toLong() ?: 0L
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        val studentPermission = db.clazzDao.personHasPermissionWithClazz(loggedInPersonUid,
                selectedClazz,  Role.PERMISSION_CLAZZ_ADD_STUDENT)

        val teacherPermission = db.clazzDao.personHasPermissionWithClazz(loggedInPersonUid,
                selectedClazz,  Role.PERMISSION_CLAZZ_ADD_TEACHER)

        view.isStudentEnrolmentEditVisible = studentPermission
        view.isTeacherEnrolmentEditVisible = teacherPermission

        val hasPermission = studentPermission || teacherPermission

        val maxDateOfEnrolment = db.clazzEnrolmentDao.findMaxEndDateForEnrolment(
                selectedClazz, selectedPerson, 0)
        return if(maxDateOfEnrolment == Long.MAX_VALUE){
            false
        }else{
            hasPermission
        }
    }

    private fun updateListOnView() {
        GlobalScope.launch(doorMainDispatcher()){
            view.person = repo.personDao.findByUidAsync(selectedPerson)
            view.clazz = repo.clazzDao.findByUidAsync(selectedClazz)

            view.enrolmentList = repo.clazzEnrolmentDao.findAllEnrolmentsByPersonAndClazzUid(
                    selectedPerson, selectedClazz)
        }
    }

    override fun handleClickCreateNewFab() {
        navigateForResult(
            NavigateForResultOptions(this,
                null, ClazzEnrolmentEditView.VIEW_NAME,
                ClazzEnrolmentWithLeavingReason::class,
                ClazzEnrolmentWithLeavingReason.serializer(),
                arguments = arguments
                    .plus(UstadView.ARG_SAVE_TO_DB to true.toString()).toMutableMap())
        )
    }

    override fun handleClickAddNewItem(args: Map<String, String>?, destinationResultKey: String?) {
        handleClickCreateNewFab()
    }

    fun handleClickClazzEnrolment(enrolment: ClazzEnrolmentWithLeavingReason){
        navigateForResult(
            NavigateForResultOptions(this,
                enrolment, ClazzEnrolmentEditView.VIEW_NAME,
                ClazzEnrolmentWithLeavingReason::class,
                ClazzEnrolmentWithLeavingReason.serializer(),
                arguments = mapOf(
                    UstadView.ARG_ENTITY_UID to enrolment.clazzEnrolmentUid.toString(),
                    UstadView.ARG_SAVE_TO_DB to true.toString())
                    .plus(arguments).toMutableMap())
        )
    }

    fun handleClickProfile(personUid: Long){
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to personUid.toString()), context)
    }
}