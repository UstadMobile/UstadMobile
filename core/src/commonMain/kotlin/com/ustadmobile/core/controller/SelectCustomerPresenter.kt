package com.ustadmobile.core.controller


import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.CustomerDetailView
import com.ustadmobile.core.view.SelectCustomerView
import com.ustadmobile.core.view.SelectCustomerView.Companion.ARG_SP_LE_UID
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch


/**
 * The SelectCustomerPresenter Presenter.
 */
class SelectCustomerPresenter(context: Any, arguments: Map<String, String>?,
                              view: SelectCustomerView) :
        UstadBaseController<SelectCustomerView>(context, arguments!!, view) {

    //Any arguments stored as variables here
    private var personUmProvider: DataSource.Factory<Int, Person>?=null
    var leUid : Long = 0L

    internal var repository = UmAccountManager.getRepositoryForActiveAccount(context)
    internal var database = UmAppDatabase.getInstance(context)

    init {

        if (arguments!!.containsKey(ARG_SP_LE_UID)) {
            leUid = arguments.get(ARG_SP_LE_UID)!!.toLong()
        }

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Find the provider
        GlobalScope.launch {
            val customerRole = database.roleDao.findByName(Role.ROLE_NAME_CUSTOMER)
            if(customerRole!=null){
                personUmProvider = repository.personDao.findAllPeopleByLEAndRoleUid(leUid,
                        customerRole.roleUid)
                view.runOnUiThread(Runnable {
                    view.setListProvider(personUmProvider!!)
                })
            }
        }
    }

    fun handleClickCustomer(selectedPersonUid:Long){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(CustomerDetailView.ARG_CUSTOMER_UID, selectedPersonUid.toString())
        args.put(CustomerDetailView.ARG_CD_LE_UID,
                UmAccountManager.getActiveAccount(context)!!.personUid.toString())
        impl.go(CustomerDetailView.VIEW_NAME, args, context)
        view.finish()
    }

    fun handleClickNewCustomer(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(CustomerDetailView.ARG_CD_LE_UID,
                UmAccountManager.getActiveAccount(context)!!.personUid.toString())
        impl.go(CustomerDetailView.VIEW_NAME, args, context)
        view.finish()
    }

}
