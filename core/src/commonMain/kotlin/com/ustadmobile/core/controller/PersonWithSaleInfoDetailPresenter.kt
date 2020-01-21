package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.SaleDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.util.ext.observeWithPresenter
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView.Companion.ARG_WE_UID
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 *  Presenter for PersonWithSaleInfoDetail view
 **/
class PersonWithSaleInfoDetailPresenter(context: Any,
                    arguments: Map<String, String>?,
                    view: PersonWithSaleInfoDetailView,
                    val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                    private val repository: UmAppDatabase =
                                    UmAccountManager.getRepositoryForActiveAccount(context),
                    private val saleDao: SaleDao = repository.saleDao)
    : UstadBaseController<PersonWithSaleInfoDetailView>(context, arguments!!, view) {

    private lateinit var personDao: PersonDao

    private var personUid :Long = 0

    init {
        personDao = repository.personDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Any arguments

		if(arguments.containsKey(ARG_WE_UID)){
			personUid = (arguments[ARG_WE_UID]!!.toLong())
		}

        val thisP = this
        //Populate view
        GlobalScope.launch {

            val personLive = personDao.findByUidLive(personUid)
            view.runOnUiThread(Runnable {
                personLive.observeWithPresenter(thisP, thisP::handlePersonLive)
            })

        }
    }

    private fun handlePersonLive(person:Person?){
        if(person!= null) {
            view.updatePersonOnView(person)
        }
    }
}
