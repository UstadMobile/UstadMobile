package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PersonWithSaleInfoDetailView
import com.ustadmobile.core.view.PersonWithSaleInfoProfileView
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch

/**
 * Presenter for UserProfile view
 */
class PersonWithSaleInfoProfilePresenter(context: Any,
                                         arguments: Map<String, String>?,
                                         view: PersonWithSaleInfoProfileView,
                                         repository: UmAppDatabase =
                                                 UmAccountManager.getRepositoryForActiveAccount(context))
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

        val thisP=this
        if (personUid != 0L) {

            GlobalScope.launch {
                val personLive = personDao.findByUidLive(personUid)
                GlobalScope.launch(Dispatchers.Main){
                    personLive.observe(thisP, thisP::handlePersonLive)
                }
            }
        }
    }
    private fun handlePersonLive(person:Person?){
        if (person != null) {
            view.runOnUiThread(Runnable {
                view.updatePersonOnView(person)
            })

            thisPerson = person


            GlobalScope.launch {
                val personPicture =
                        personPictureDao.findByPersonUidAsync(thisPerson!!.personUid)
                if (personPicture != null) {
                    view.updateImageOnView(personPictureDao!!.getAttachmentPath(personPicture))
                }
            }
        }
    }
}
