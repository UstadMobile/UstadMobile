package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UmCallback
import com.ustadmobile.core.impl.UmCallbackWithDefaultValue
import com.ustadmobile.core.view.PersonPictureDialogView
import com.ustadmobile.lib.db.entities.Role



import com.ustadmobile.core.view.PersonDetailView.Companion.ARG_PERSON_UID

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
            personUid = arguments!!.get(ARG_PERSON_UID)
        }
        checkPermissions()
    }

    fun checkPermissions() {
        val personDao = repository.personDao
        personDao.personHasPermission(loggedInPersonUid, personUid,
                Role.PERMISSION_PERSON_PICTURE_UPDATE,
                UmCallbackWithDefaultValue(false,
                        object : UmCallback<Boolean> {
                            override fun onSuccess(result: Boolean?) {
                                view.showUpdateImageButton(result!!)
                            }

                            override fun onFailure(exception: Throwable?) {
                                print(exception!!.message)
                            }
                        }
                ))
    }

}
