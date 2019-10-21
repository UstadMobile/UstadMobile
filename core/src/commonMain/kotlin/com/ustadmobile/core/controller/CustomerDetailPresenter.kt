package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.RoleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.CustomerDetailView
import com.ustadmobile.core.view.CustomerDetailView.Companion.ARG_CD_LE_UID
import com.ustadmobile.core.view.CustomerDetailView.Companion.ARG_CUSTOMER_UID
import com.ustadmobile.core.view.SelectPersonDialogView
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
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
    private val roleDao : RoleDao
    lateinit var currentPerson : Person
    private var leUid : Long = 0L

    private var locationName : String = ""
    private var phoneNumber : String = ""


    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)

        personDao = repository.personDao
        roleDao = repository.roleDao
        if (arguments!!.containsKey(ARG_CUSTOMER_UID)) {
            customerUid = arguments.get(ARG_CUSTOMER_UID)!!.toLong()
        }else{
        }
        if (arguments!!.containsKey(ARG_CD_LE_UID)) {
            leUid = arguments.get(ARG_CD_LE_UID)!!.toLong()
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        view.updateLocationName("")
        view.updatePhoneNumber("")
        view.updateCustomerName("")

        if (customerUid != 0L) {
            selectedCustomer(customerUid)
        }else{
            createNewCustomer()
        }

    }

    fun selectCustomer(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(SelectPersonDialogView.ARG_SP_LE_UID, leUid.toString())
        impl.go(SelectPersonDialogView.VIEW_NAME, args, context)
        view.finish()
    }

    fun createNewCustomer(){
        GlobalScope.launch {
            currentPerson = Person()
            val customerRole = roleDao.findByName(Role.ROLE_NAME_CUSTOMER)
            currentPerson.personUid = personDao.createPersonAsync(currentPerson)
            //Create Customer Role for this person and persist.
            if(customerRole!=null){
                currentPerson.personRoleUid = customerRole.roleUid
            }

        }
    }

    fun updatePhoneNumber(pn: String){
        currentPerson.phoneNum = pn
    }


    fun selectedCustomer(selectedCustomerUid: Long){
        customerUid = selectedCustomerUid
        GlobalScope.launch {
            val result = personDao.findByUidAsync(customerUid)
            currentPerson = result!!
            var firstNames = ""
            var lastName = ""
            if(currentPerson.firstNames != null){
                firstNames = currentPerson.firstNames!!
            }
            if(currentPerson.lastName != null){
                lastName = currentPerson.lastName!!
            }
            view.updateCustomerName(firstNames + " " + lastName)

            if(currentPerson.personAddress != null){
                view.updateLocationName(currentPerson.personAddress!!)
            }else {
                view.updateLocationName("")
            }

            if(currentPerson.phoneNum != null){
                view.updatePhoneNumber(currentPerson.phoneNum.toString())
            }else{
                view.updatePhoneNumber("")
            }

        }
    }


    fun doneSelecting(location: String, phoneNumber : String, customerName : String){
        //Persist any changes
        GlobalScope.launch {
            currentPerson.phoneNum = phoneNumber

            if(customerName.split("\\w+").size >0){

                currentPerson.lastName = customerName.substring(customerName.lastIndexOf(" ")+1);
                currentPerson.firstNames = customerName.substring(0, customerName.lastIndexOf(' '));
            }
            else{
                currentPerson.firstNames = customerName;
                currentPerson.lastName = "";
            }

            currentPerson.personAddress = location
            currentPerson.active = true
            personDao.updateAsync(currentPerson)

            view.updateAndDismiss(customerUid, customerName)
        }

    }


}
