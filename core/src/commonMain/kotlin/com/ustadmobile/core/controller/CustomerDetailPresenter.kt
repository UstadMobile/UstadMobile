package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.LocationDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.CustomerDetailView
import com.ustadmobile.core.view.CustomerDetailView.Companion.ARG_CD_LE_UID
import com.ustadmobile.core.view.CustomerDetailView.Companion.ARG_CUSTOMER_UID
import com.ustadmobile.core.view.SelectCustomerView
import com.ustadmobile.lib.db.entities.Location
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * Presenter for CustomerDetailPresenter view
 */
class CustomerDetailPresenter(context: Any, arguments: Map<String, String>?, view:
CustomerDetailView) : UstadBaseController<CustomerDetailView>(context, arguments!!, view) {

    internal var repository: UmAppDatabase

    var customerUid : Long = 0L
    var customerName : String = ""
    private val personDao : PersonDao
    private val locationDao : LocationDao
    private val roleDao : RoleDao
    private var currentPerson : Person? = null
    private var updatedPerson: Person ?=null
    private var leUid : Long = 0L

    private var locationName : String = ""
    private var phoneNumber : String = ""

    var editMode: Boolean = false

    private var positionToLocationUid: MutableMap<Int, Long>? = null


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        personDao = repository.personDao
        locationDao = repository.locationDao
        roleDao = repository.roleDao
        if (arguments!!.containsKey(ARG_CUSTOMER_UID)) {
            customerUid = arguments.get(ARG_CUSTOMER_UID)!!.toLong()
        }else{
        }
        if (arguments!!.containsKey(ARG_CD_LE_UID)) {
            leUid = arguments.get(ARG_CD_LE_UID)!!.toLong()
        }

        positionToLocationUid = HashMap()
    }

    private fun startObservingLocations() {
        val locLive = locationDao.findAllActiveLocationsLive()
        val thisP = this
        GlobalScope.launch(Dispatchers.Main) {
            locLive.observeWithPresenter(thisP, thisP::handleLocationsChanged)
        }
    }

    private fun handleLocationsChanged(changedLocations: List<Location>?) {
        var selectedPosition = 0

        var locationUid: Long = 0


        GlobalScope.launch {
            if (currentPerson == null) {
                currentPerson = personDao.findByUid(customerUid)!!
            }
            if (currentPerson!!.personLocationUid != 0L) {
                locationUid = currentPerson!!.personLocationUid
            }


            val locationList = ArrayList<String>()
            var spinnerId = 0
            for (el in changedLocations!!) {
                positionToLocationUid?.set(spinnerId, el.locationUid)

                val title = el.title
                if (title != null) {
                    locationList.add(title)
                }
                if (locationUid == el.locationUid) {
                    selectedPosition = spinnerId
                }
                spinnerId++
            }

            var locationPreset = locationList.toTypedArray<String>()

            view.runOnUiThread(Runnable {
                view.setLocationPresets(locationPreset, selectedPosition)
            })
        }

    }

    fun handleLocationSelected(position: Int) {
        if (position >= 0 && !positionToLocationUid!!.isEmpty()
                && positionToLocationUid!!.containsKey(position)) {
            val locationUid = positionToLocationUid!!.get(position)


            currentPerson!!.personLocationUid = locationUid!!
        }
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        view.updateLocationName("")
        view.updatePhoneNumber("")
        view.updateCustomerName("")

        //New way:
        initFromCustomer()

    }

    private fun initFromCustomer(){
        val thisP = this
        GlobalScope.launch {

            if (customerUid == 0L) {
                editMode = true
                currentPerson = Person()
                val customerRole = roleDao.findByName(Role.ROLE_NAME_CUSTOMER)

                //Create Customer Role for this person and persist.
                if(customerRole!=null){
                    currentPerson!!.personRoleUid = customerRole.roleUid
                }

                val loggedInPersonUid = UmAccountManager.getActivePersonUid(context)
                currentPerson!!.personAddedByUid = loggedInPersonUid
                currentPerson!!.personUid = personDao.createPersonAsync(currentPerson!!,
                        loggedInPersonUid)
                customerUid = currentPerson!!.personUid

                view.runOnUiThread(Runnable {
                    view.updatePAB(true)
                })
            }else{
                editMode = false
                view.runOnUiThread(Runnable {
                    view.updatePAB(false)
                })
            }

            val resultLive = personDao.findByUidLive(customerUid)
            view.runOnUiThread(Runnable {
                resultLive.observeWithPresenter(thisP, thisP::updateCustomerOnView)
            })

            startObservingLocations()

        }
    }

    private fun updateCustomerOnView(person:Person?){
        editMode = false
        currentPerson = person!!
        var firstNames = ""
        var lastName = ""
        if(currentPerson!!.firstNames != null){
            firstNames = currentPerson!!.firstNames!!
        }
        if(currentPerson!!.lastName != null){
            lastName = currentPerson!!.lastName!!
        }
        if(firstNames != "" && lastName != "") {
            view.updateCustomerName(firstNames + " " + lastName)
        }else if(firstNames!= ""){
            view.updateCustomerName(firstNames + " " + lastName)
        }else{
            view.updateCustomerName("")
        }

        if(currentPerson!!.personAddress != null){
            view.updateLocationName(currentPerson!!.personAddress!!)
        }else {
            view.updateLocationName("")
        }

        if(currentPerson!!.phoneNum != null){
            view.updatePhoneNumber(currentPerson!!.phoneNum.toString())
        }else{
            view.updatePhoneNumber("")
        }

        editMode = true

    }

    fun selectCustomer(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(SelectCustomerView.ARG_SP_LE_UID, leUid.toString())
        impl.go(SelectCustomerView.VIEW_NAME, args, context)
        view.finish()
    }

    fun updatePhoneNumber(pn: String){
        currentPerson!!.phoneNum = pn
    }

    fun updateCustomerUid(uid: Long){
        customerUid = uid
        initFromCustomer()
    }

    fun doneSelecting(location: String, phoneNumber : String, customerName : String){

        val newCustomerName = customerName.trim()

        //Persist any changes
        GlobalScope.launch {
            currentPerson!!.phoneNum = phoneNumber

            if(newCustomerName != "" && newCustomerName.split(" " ).size > 1 &&
                    newCustomerName.split("\\w+").size >0){

                currentPerson!!.lastName =
                        newCustomerName.substring(newCustomerName.lastIndexOf(" ")+1);
                currentPerson!!.firstNames =
                        newCustomerName.substring(0, newCustomerName.lastIndexOf(' '));
            }
            else{
                currentPerson!!.firstNames = newCustomerName;
                currentPerson!!.lastName = "";
            }

            currentPerson!!.personAddress = location
            currentPerson!!.active = true
            personDao.updateAsync(currentPerson!!)

            view.updateAndDismiss(customerUid, newCustomerName)
        }

    }


}
