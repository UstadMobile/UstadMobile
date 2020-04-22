package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithLifecycleOwner
import com.ustadmobile.core.view.ClazzDetailEnrollStudentView.Companion.ARG_NEW_PERSON
import com.ustadmobile.core.view.PeopleListView
import com.ustadmobile.core.view.PeopleListView.Companion.SORT_ORDER_ATTENDANCE_ASC
import com.ustadmobile.core.view.PeopleListView.Companion.SORT_ORDER_ATTENDANCE_DESC
import com.ustadmobile.core.view.PeopleListView.Companion.SORT_ORDER_NAME_ASC
import com.ustadmobile.core.view.PeopleListView.Companion.SORT_ORDER_NAME_DESC
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.staging.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.PersonEditView
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.PersonCustomFieldValue
import com.ustadmobile.lib.db.entities.PersonWithEnrollment
import com.ustadmobile.lib.db.entities.Role.Companion.PERMISSION_PERSON_INSERT
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The PeopleList's Presenter - responsible for the logic behind slowing all the people regardless
 * of what class they are in. This presenter is also responsible in Adding a person handler and in
 * going to PersonDetail View to see more information about that Person.
 */
class PeopleListPresenter(context: Any, arguments: Map<String, String>?, view: PeopleListView,
                          private val lifecycleOwner: DoorLifecycleOwner,
                          val impl : UstadMobileSystemImpl = UstadMobileSystemImpl.instance) :
        CommonHandlerPresenter<PeopleListView>(context, arguments!!, view) {

    //Provider
    private var personWithEnrollmentUmProvider: DataSource.Factory<Int, PersonWithEnrollment>? = null

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var database = UmAccountManager.getActiveDatabase(context)

    private var loggedInPersonUid: Long? = 0L

    private var queryParam = "%"
    private var idToOrderInteger: HashMap<Int, Int>? = null

    /**
     * In order:
     * 1. Gets all people via the database as UmProvider and sets it to the view.
     *
     * @param savedState The saved state.
     */
    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)

        personWithEnrollmentUmProvider = repository.personDao.findAllPeopleWithEnrollment()
        updateProviderToView()

        idToOrderInteger = HashMap<Int, Int>()

        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid

        updateSortSpinnerPreset()

        getLoggedInPerson()

        checkPermissions()
    }

    private fun updateProviderToView() {
        setPeopleProviderToView()
    }

    /**
     * Updates people list provider with search parameter and updates the view to show it.
     * @param searchValue   The search value. eg: "Mo"
     */
    fun updateProviderWithSearch(searchValue: String) {
        queryParam = "%$searchValue%"
        personWithEnrollmentUmProvider = repository.personDao.findAllPeopleWithEnrollmentBySearch(queryParam)
        updateProviderToView()
    }


    /**
     * Common method to convert Array List to String Array
     *
     * @param presetAL The array list of string type
     * @return  String array
     */
    private fun arrayListToStringArray(presetAL: ArrayList<String>): Array<String?> {
        val objectArr = presetAL.toTypedArray()
        val strArr = arrayOfNulls<String>(objectArr.size)
        for (j in objectArr.indices) {
            strArr[j] = objectArr[j]
        }
        return strArr
    }

    /**
     * Updates the sort by drop down (spinner) on the Class list. For now the sort options are
     * defined within this method and will automatically update the sort options without any
     * database call.
     */
    private fun updateSortSpinnerPreset() {
        val presetAL = ArrayList<String>()

        idToOrderInteger = HashMap<Int, Int>()

        presetAL.add(impl.getString(MessageID.sort_by_name_asc, context))
        idToOrderInteger!!.put(presetAL.size, SORT_ORDER_NAME_ASC)
        presetAL.add(impl.getString(MessageID.sort_by_name_desc, context))
        idToOrderInteger!!.put(presetAL.size, SORT_ORDER_NAME_DESC)

        val sortPresets = arrayListToStringArray(presetAL)

        view.updateSortSpinner(sortPresets)
    }

    /**
     * Gets logged in person and observes it.
     */
    private fun getLoggedInPerson() {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        val loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
        val personLive = repository.personDao.findByUidLive(loggedInPersonUid)
        personLive.observeWithLifecycleOwner(lifecycleOwner, this::handlePersonValueChanged)
    }

    /**
     * Called on logged in person changed.
     *
     * @param loggedInPerson    The person changed.
     */
    private fun handlePersonValueChanged(loggedInPerson: Person?) {
        view.runOnUiThread(Runnable {
            view.showFAB(loggedInPerson?.admin ?: false)
        })

    }

    /**
     * Checks permission and updates view accordingly (ie: enables/disables components on view)
     */
    fun checkPermissions() {
        val clazzDao = repository.clazzDao
        GlobalScope.launch {
            val result = clazzDao.personHasPermission(loggedInPersonUid!!, PERMISSION_PERSON_INSERT)
            view.runOnUiThread(Runnable { view.showFAB(result) })
        }
    }

    /**
     * Sets the people list provider set in the Presenter to the View.
     */
    private fun setPeopleProviderToView() {
        view.setPeopleListProvider(personWithEnrollmentUmProvider!!)
    }

    /**
     * Handles what happens when you click Add Person button (Floating Action Button in Android).
     * This will create a new Person and persist it to the database. It will then pass this new
     * Person's Uid to PersonEdit screen to edit that. It will also add blank Custom Field Values
     * for that new Person so that those can be edited as well.
     */
    fun handleClickPrimaryActionButton() {
        //Goes to PersonEditActivity with currentClazzUid passed as argument

        val newPerson = Person()
        val personDao = repository.personDao
        val personFieldDao = repository.personCustomFieldDao
        val personFieldDaoDB = database.personCustomFieldDao
        val customFieldValueDao = repository.personCustomFieldValueDao
        val customFieldValueDaoDB = database.personCustomFieldValueDao

        GlobalScope.launch {
            val result = personDao.createPersonAsync(newPerson, loggedInPersonUid!!)

            //val allCustomFields = personFieldDao.findAllCustomFields()
            val allCustomFieldsLocal = personFieldDaoDB.findAllCustomFields()
            for (everyCustomField in allCustomFieldsLocal!!) {
                val cfv = PersonCustomFieldValue()
                cfv.personCustomFieldValuePersonCustomFieldUid = (everyCustomField.personCustomFieldUid)
                cfv.personCustomFieldValuePersonUid = (result)
                cfv.personCustomFieldValueUid = customFieldValueDao.insert(cfv)
            }

            val args = HashMap<String, String>()
            args.put(ARG_PERSON_UID, result.toString())
            args.put(ARG_NEW_PERSON, "true")
            impl.go(PersonEditView.VIEW_NAME, args, view.viewContext)

        }
    }

    /**
     * The primary action handler on the people list (for every item) is to open that Person's
     * Detail (ie: PersonDetailView)
     *
     * @param arg   The argument to be passed to the presenter for primary action pressed.
     */
    override fun handleCommonPressed(arg: Any) {
        val args = HashMap<String, String>()
        args.put(ARG_PERSON_UID, arg.toString())
        impl.go(PersonDetailView.VIEW_NAME, args, view.viewContext)
    }

    /**
     * The secondary action handler on the people list (for every item) is nothing here.
     * @param arg   The argument to be passed to the presenter for secondary action pressed.
     */
    override fun handleSecondaryPressed(arg: Any) {
        //No secondary action for every item here.
    }


    /**
     * Queries provider to be ordered and updates the view.
     * @param posiiton  Position of the order selected looked up in idToOrderInteger Map.
     */
    fun handleChangeSortOrder(posiiton: Int) {
        var posiiton = posiiton
        posiiton = posiiton + 1
        if (idToOrderInteger!!.containsKey(posiiton)) {
            val sortCode = idToOrderInteger!!.get(posiiton)!!
            getAndSetProvider(sortCode)
        }
    }

    /**
     * This method updates the Class List provider based on the sort order flag selected.
     * Every order has a corresponding order by change in the database query where this method
     * reloads the class list provider.
     *
     * @param order The order selected (defined in this class as static final)
     */
    private fun getAndSetProvider(order: Int) {
        when (order) {

            SORT_ORDER_NAME_DESC -> personWithEnrollmentUmProvider = repository.personDao
                    .findAllPeopleWithEnrollmentSortNameDesc()
            SORT_ORDER_ATTENDANCE_ASC -> personWithEnrollmentUmProvider = repository.personDao
                    .findAllPeopleWithEnrollmentBySearch(queryParam)
            SORT_ORDER_ATTENDANCE_DESC -> personWithEnrollmentUmProvider = repository.personDao
                    .findAllPeopleWithEnrollmentBySearch(queryParam)
            SORT_ORDER_NAME_ASC -> personWithEnrollmentUmProvider = repository.personDao
                    .findAllPeopleWithEnrollmentSortNameAsc()
        }

        updateProviderToView()

    }

}
