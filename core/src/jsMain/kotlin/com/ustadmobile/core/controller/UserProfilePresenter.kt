package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.HomeView
import com.ustadmobile.core.view.UserProfileView
import kotlin.browser.window

actual class UserProfilePresenter actual constructor(context: Any, arguments: Map<String, String?>, view: UserProfileView,
                                                     personDao: PersonDao, impl: UstadMobileSystemImpl)
    : UserProfilePresenterCommon(context, arguments, view, personDao, impl){

    actual override fun handleNavigation() {
        window.open(window.location.origin,"_self")
    }

}