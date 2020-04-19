package com.ustadmobile.core.controller

import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.db.dao.ClazzMemberDao
import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.staging.core.view.PersonDetailView.Companion.ARG_PERSON_UID
import com.ustadmobile.core.view.RecordDropoutDialogView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * Presenter for RecordDropoutDialog view
 */
class RecordDropoutDialogPresenter(context: Any, arguments: Map<String, String>?,
                               view: RecordDropoutDialogView) :
        UstadBaseController<RecordDropoutDialogView>(context, arguments!!,
        view) {

    var repository: UmAppDatabase

    internal var personDao: PersonDao
    internal var clazzMemberDao: ClazzMemberDao

    internal var personUid: Long = 0

    var isOtherNGO: Boolean = false
    var isMove: Boolean = false
    var isCry: Boolean = false
    var isSickness: Boolean = false
    var isPermission: Boolean = false
    var isSchool: Boolean = false
    var isTransportation: Boolean = false
    var isPersonal: Boolean = false
    var isOther: Boolean = false

    init {

        repository = UmAccountManager.getRepositoryForActiveAccount(context)
        personDao = repository.personDao
        clazzMemberDao = repository.clazzMemberDao

        if (arguments!!.containsKey(ARG_PERSON_UID)) {
            personUid = arguments!!.get(ARG_PERSON_UID)!!.toLong()
        }
    }

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)


    }

    fun handleClickOk() {
        GlobalScope.launch {
            clazzMemberDao.inactivateClazzMemberForPerson(personUid)
            view.finish()
        }
    }
}
