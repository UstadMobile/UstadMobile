package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.ustadmobile.core.view.PersonWithSaleInfoListView;
import com.ustadmobile.lib.db.entities.Person
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.lib.db.entities.PersonWithSaleInfo

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class PersonWithSaleInfoListPresenter(context: Any,
                                      arguments: Map<String, String>?,
                                      view: PersonWithSaleInfoListView)
    : UstadBaseController<PersonWithSaleInfoListView>(context, arguments!!, view) {

    internal lateinit var repository: UmAppDatabase

    private var entityDao: SaleDao? = null
    private var entity: Person? = null

    private var personDao: PersonDao? = null
    private var currentPerson: Person? = null
    private var loggedInPersonUid = 0L

    private var personUid: Long =0
    private lateinit var factory : DataSource.Factory<Int, PersonWithSaleInfo>

    init {
        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        entityDao = repository.saleDao
        personDao = repository.personDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        if (loggedInPersonUid != 0L) {
            GlobalScope.launch {
                var result = personDao!!.findByUidAsync(loggedInPersonUid)
                currentPerson = result
            }
        }

        //Any arguments
        if(arguments.containsKey(PersonWithSaleInfoListView.ARG_LE_UID)){
            personUid = (arguments[PersonWithSaleInfoListView.ARG_LE_UID]!!.toLong())
        }

        //Get assigned people
        factory = entityDao!!.getMyWomenEntrepreneurs(personUid)
        view.setWEListFactory(factory)
    }

    fun handleClickWE(weUid:Long){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        args.put(ARG_WE_UID, weUid.toString())
        impl.go(PersonWithSaleInfoDetailView.VIEW_NAME, args, context)
    }

    fun handleClickSearch(){
        val impl = UstadMobileSystemImpl.instance
        val args = HashMap<String, String>()
        //TODO:
        //impl.go(PersonWithSaleInfoSearchView.VIEW_NAME, args, context)
    }

}
