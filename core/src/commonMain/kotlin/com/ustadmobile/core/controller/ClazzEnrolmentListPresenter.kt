package com.ustadmobile.core.controller

import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzEnrolmentListPresenter(context: Any, arguments: Map<String, String>, view: ClazzEnrolmentListView,
                                   di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzEnrolmentListView, ClazzEnrolment>(context, arguments, view, di, lifecycleOwner) {

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
            view.person = repo.personDao.findByUid(selectedPerson)
            view.clazz = repo.clazzDao.findByUidAsync(selectedClazz)

            view.enrolmentList = repo.clazzEnrolmentDao.findAllEnrolmentsByPersonAndClazzUid(
                    selectedPerson, selectedClazz)
        }
    }

    override fun handleClickCreateNewFab() {
          systemImpl.go(ClazzEnrolmentEditView.VIEW_NAME, arguments
                  .plus(UstadView.ARG_SAVE_TO_DB to true.toString()), context)
    }

    fun handleClickClazzEnrolment(enrolment: ClazzEnrolmentWithLeavingReason){
        systemImpl.go(ClazzEnrolmentEditView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to enrolment.clazzEnrolmentUid.toString(),
                        UstadView.ARG_SAVE_TO_DB to true.toString())
                        .plus(arguments), context)
    }

    fun handleClickProfile(personUid: Long){
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to personUid.toString()), context)
    }
}