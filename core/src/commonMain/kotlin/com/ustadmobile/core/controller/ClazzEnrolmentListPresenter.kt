package com.ustadmobile.core.controller

import com.ustadmobile.core.view.*
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.lib.db.entities.ClazzEnrolment
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.UmAccount
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.kodein.di.DI

class ClazzEnrolmentListPresenter(context: Any, arguments: Map<String, String>, view: ClazzEnrolmentListView,
                                   di: DI, lifecycleOwner: DoorLifecycleOwner)
    : UstadListPresenter<ClazzEnrolmentListView, ClazzEnrolment>(context, arguments, view, di, lifecycleOwner) {

    var selectedPerson = 0L
    var selectedClazz = 0L

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        selectedPerson = arguments[UstadView.ARG_PERSON_UID]?.toLong() ?: 0L
        selectedClazz = arguments[UstadView.ARG_FILTER_BY_CLAZZUID]?.toLong() ?: 0L
        updateListOnView()
    }

    override suspend fun onCheckAddPermission(account: UmAccount?): Boolean {
        return true
    }

    private fun updateListOnView() {
        GlobalScope.launch(doorMainDispatcher()){
            view.person = db.personDao.findByUid(selectedPerson)

            view.list = db.clazzEnrolmentDao.findAllEnrolmentsByPersonAndClazzUid(
                    selectedPerson, selectedClazz)
        }
    }

    override fun handleClickCreateNewFab() {
          systemImpl.go(ClazzEnrolmentEditView.VIEW_NAME, arguments, context)
    }

    fun handleClickClazzEnrolment(enrolment: ClazzEnrolment){
        systemImpl.go(ClazzEnrolmentEditView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to enrolment.clazzEnrolmentUid.toString())
                        .plus(arguments), context)
    }

    fun handleClickProfile(personUid: Long){
        systemImpl.go(PersonDetailView.VIEW_NAME,
                mapOf(UstadView.ARG_ENTITY_UID to personUid.toString()), context)
    }
}