package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoProfileView
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Presenter for UserProfile view
 */
class PersonWithSaleInfoProfilePresenter(context: Any,
                         arguments: Map<String, String>?,
                         view: PersonWithSaleInfoProfileView,
                         val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                         private val repository: UmAppDatabase =
                                 UmAccountManager.getRepositoryForActiveAccount(context) )
    : UstadBaseController<PersonWithSaleInfoProfileView>(context, arguments!!, view) {


    private val personDao: PersonDao
    private var personPictureDao: PersonPictureDao

    var thisPerson:Person ?= null


    init {

        //Get provider Dao
        personDao = repository.personDao
        personPictureDao = repository.personPictureDao

    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        var personUid = 0L
        if(arguments.containsKey(PersonWithSaleInfoDetailView.ARG_WE_UID)){
            personUid = arguments.get(PersonWithSaleInfoDetailView.ARG_WE_UID)!!.toLong()
        }

        if (personUid != 0L) {

            GlobalScope.launch {
                thisPerson = personDao.findByUidAsync(personUid)

                if (thisPerson != null) {
                    view.updatePersonOnView(thisPerson!!)

                    personPictureDao = repository.personPictureDao
                    val personPicture =
                            personPictureDao.findByPersonUidAsync(thisPerson!!.personUid)
                    if (personPicture != null) {
                        //TODO: Fix for KMP
                        //view.updateImageOnView(personPictureDao!!.getAttachmentPath
                        //(personPicture.personPictureUid))
                    }
                }
            }
        }

    }
}
