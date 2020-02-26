package com.ustadmobile.core.controller

//TODO: this
//import com.ustadmobile.core.controller.ReportOptionsDetailPresenter.convertCSVStringToLongList
import androidx.paging.DataSource
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.SelectMultiplePeopleView
import com.ustadmobile.core.view.SelectMultiplePeopleView.Companion.ARG_SELECTED_PEOPLE
import com.ustadmobile.lib.db.entities.Person


/**
 * The SelectClazzesDialog Presenter.
 */
class SelectMultiplePeoplePresenter(context: Any, arguments: Map<String, String>?,
                                    view: SelectMultiplePeopleView)
    : UstadBaseController<SelectMultiplePeopleView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private lateinit var personWithEnrollmentUmProvider: DataSource.Factory<Int, Person>
    var selectedPeopleList: List<Long>? = null

    var people: HashMap<String, Long>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)

    init {
        if (arguments!!.containsKey(ARG_SELECTED_PEOPLE)) {
            val selectedPeopleCSString = arguments!!.get(ARG_SELECTED_PEOPLE).toString()

            //TODO: this
            //selectedPeopleList = convertCSVStringToLongList(selectedPeopleCSString)

        }

    }

    fun addToPeople(person: Person) {
        val personName = person.fullName(UstadMobileSystemImpl.instance.getLocale(context))
        if (!people!!.containsKey(personName)) {
            people!![personName] = person.personUid
        }
    }

    fun removePeople(person: Person) {
        val personName = person.fullName(UstadMobileSystemImpl.instance.getLocale(context))
        if (people!!.containsKey(personName)) {
            people!!.remove(personName)
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        people = HashMap()

        //Find the provider
        personWithEnrollmentUmProvider = repository.personDao.findAllPeopleProvider()
        view.setListProvider(personWithEnrollmentUmProvider)

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
