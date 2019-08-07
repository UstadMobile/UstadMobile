package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.UmProvider
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectClazzesDialogView
import com.ustadmobile.core.view.SelectPeopleDialogView
import com.ustadmobile.lib.db.entities.Clazz
import com.ustadmobile.lib.db.entities.ClazzWithNumStudents
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithEnrollment


import com.ustadmobile.core.view.ReportEditView.Companion.ARG_CLASSES_SET
import com.ustadmobile.core.view.ReportEditView.Companion.ARG_LOCATIONS_SET
import com.ustadmobile.core.view.SelectPeopleDialogView.Companion.ARG_SELECTED_PEOPLE


/**
 * The SelectClazzesDialog Presenter.
 */
class SelectPeopleDialogPresenter(context: Any, arguments: Map<String, String>?,
                                  view: SelectPeopleDialogView) :
        UstadBaseController<SelectPeopleDialogView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var personWithEnrollmentUmProvider: UmProvider<PersonWithEnrollment>? = null
    var selectedPeopleList: List<Long>? = null

    var people: HashMap<String, Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {
        if (arguments!!.containsKey(ARG_SELECTED_PEOPLE)) {
            val clazzesSelected = arguments!!.get(ARG_SELECTED_PEOPLE) as LongArray
            selectedPeopleList = ReportOverallAttendancePresenter.convertLongArray(clazzesSelected)
        }

    }

    fun addToPeople(person: Person) {
        val personName = person.firstNames + " " +
                person.lastName
        if (!people!!.containsKey(person.personUid)) {
            people!![personName] = person.personUid
        }
    }

    fun removePeople(person: Person) {
        val personName = person.firstNames + " " +
                person.lastName
        if (people!!.containsKey(personName)) {
            people!!.remove(personName)
        }
    }

    fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        people = HashMap()

        //Find the provider
        personWithEnrollmentUmProvider = repository.personDao.findAllPeopleWithEnrollment()
        view.setPeopleProvider(personWithEnrollmentUmProvider!!)

    }

    fun handleSelection(personUid: Long, enrolled: Boolean) {
        //TODO
    }

    fun handleCommonPressed(arg: Any) {
        // The finish() should call the onResult method in parent activity, etc.
        // Make sure you send the list
        view.finish()
    }
}
