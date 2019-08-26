package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.SelectPeopleDialogView
import com.ustadmobile.core.view.SelectPeopleDialogView.Companion.ARG_SELECTED_PEOPLE
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonWithEnrollment


/**
 * The SelectClazzesDialog Presenter.
 */
class SelectPeopleDialogPresenter(context: Any, arguments: Map<String, String>?,
                                  view: SelectPeopleDialogView) :
        UstadBaseController<SelectPeopleDialogView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var personWithEnrollmentUmProvider: DataSource.Factory<Int, PersonWithEnrollment>?=null
    var selectedPeopleList: List<Long>? = null

    lateinit var people: HashMap<String, Long>

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
        if (!people.containsValue(person.personUid)) {
            people[personName] = person.personUid
        }
    }

    fun removePeople(person: Person) {
        val personName = person.firstNames + " " +
                person.lastName
        if (people!!.containsKey(personName)) {
            people!!.remove(personName)
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
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
