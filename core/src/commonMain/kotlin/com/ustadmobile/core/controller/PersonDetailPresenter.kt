package com.ustadmobile.core.controller

import androidx.paging.DataSource
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.db.dao.PersonPictureDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.ChangePasswordView
import com.ustadmobile.core.view.PersonDetailView
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.lib.db.entities.Person
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *  Presenter for MyWomenEntrepreneurs view
 **/
class PersonDetailPresenter(context: Any,
                            arguments: Map<String, String>?,
                            view: PersonDetailView,
                            val systemImpl: UstadMobileSystemImpl = UstadMobileSystemImpl.instance,
                            private val repository: UmAppDatabase =
                                    UmAccountManager.getRepositoryForActiveAccount(context))
    : UstadBaseController<PersonDetailView>(context, arguments!!, view) {

    private var currentSortOrder = 0

    private var rvDao: PersonDao
    private lateinit var personPictureDao : PersonPictureDao
    private var personUid: Long = 0L
    private var person: Person?= null


    init {
        //Initialise Daos, etc here.
        rvDao = repository.personDao
        personPictureDao = repository.personPictureDao
    }

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)

        //Get person from arguments and update the view.
        if(arguments.containsKey(ARG_PERSON_UID)){
            personUid = arguments.get(ARG_PERSON_UID).toString().toLong()

            GlobalScope.launch {
                person = rvDao.findByUidAsync(personUid)
                if (person != null) {
                    view.updatePersonOnView(person!!)

                    val personPicture = personPictureDao.findByPersonUidAsync(person!!.personUid)
                    // TODO: KMP Picure stuff
                    view.updateImageOnView("")

                }
            }
        }
    }

    fun handleClickResetPassword(){
        systemImpl.go(ChangePasswordView.VIEW_NAME,
                mapOf(ARG_PERSON_UID to personUid.toString()), context)
    }

    //Call and Message are direct methods of the Activity in their respective platforms

    fun handleUpdateFirstNames(fn:String){
        if (person != null){
            person!!.firstNames = fn
        }
    }

    fun hanldeUpdateLastName(ln:String){
        if (person != null){
            person!!.lastName = ln
        }
    }

    fun handleUpatePhoneNum(pn:String){
        if (person != null){
            person!!.phoneNum = pn
        }
    }

}
