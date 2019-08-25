package com.ustadmobile.core.controller


import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.PersonPictureDialogView
import com.ustadmobile.lib.db.entities.Role
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PersonPictureDialogPresenter(context: Any, arguments: Map<String, String>?, view:
PersonPictureDialogView) : UstadBaseController<PersonPictureDialogView>(context, arguments!!,
        view) {


    internal var repository: UmAppDatabase
    internal var loggedInPersonUid: Long = 0
    internal var personUid = 0L

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        loggedInPersonUid = UmAccountManager.getActiveAccount(context)!!.personUid
        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            personUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        }
        checkPermissions()
    }

    fun checkPermissions() {
        val personDao = repository.personDao
        GlobalScope.launch {
            val result = personDao.personHasPermission(loggedInPersonUid, personUid,
                    Role.PERMISSION_PERSON_PICTURE_UPDATE)
            view.showUpdateImageButton(result!!)
        }
    }

}
