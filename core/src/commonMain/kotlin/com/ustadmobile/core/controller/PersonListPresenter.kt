package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.PersonListView
import com.ustadmobile.lib.db.entities.Person

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class PersonListPresenter(context: Any,
                          arguments: Map<String, String>?,
                          view: PersonListView,
                          val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                          private val repository: UmAppDatabase =
                                  UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<PersonListView>(context, arguments!!, view) {


    private var currentSortOrder = 0

    private lateinit var rvDao: PersonDao

    private lateinit var factory: DataSource.Factory<Int, Person>

    init {
        //Initialise Daos, etc here.
        rvDao = repository.personDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        getAndSetProvider(currentSortOrder)
    }


    private fun getAndSetProvider(sortCode: Int) {
        //TODO:
        //factory = rvDao.filter(sortCode)
        //view.setListFactory(factory)
    }

    fun handleClickAddUser(){
        systemImpl.go(PersonDetailView.VIEW_NAME, mapOf(), context)
    }

    fun handleClickUser(userUid: Long){
        systemImpl.go(PersonDetailView.VIEW_NAME, mapOf(ARG_PERSON_UID to userUid.toString()),
                context)
    }


}
