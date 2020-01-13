package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UmAccountManager
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UserProfileView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class UserProfilePresenterCommon (context: Any, arguments: Map<String, String?>, view: UserProfileView,
                                  private val personDao: PersonDao, val impl: UstadMobileSystemImpl)
    : LanguageOptionPresenter(context, arguments, view, impl){

    private val profileView = view

    override fun onCreate(savedState: Map<String, String?>?) {
        super.onCreate(savedState)
        val account = UmAccountManager.getActiveAccount(context)
        if(account != null){
            GlobalScope.launch {
                val person = personDao.findByUid(account.personUid)
                if(person != null){
                    profileView.setLoggedPerson(person)
                    profileView.loadProfileIcon(if(account == null) "" else "")
                }
            }
        }
    }
}