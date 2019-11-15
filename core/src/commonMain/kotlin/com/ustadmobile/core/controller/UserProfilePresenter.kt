package com.ustadmobile.core.controller

import com.ustadmobile.core.db.dao.PersonDao
import com.ustadmobile.core.impl.UstadMobileSystemImpl
import com.ustadmobile.core.view.UserProfileView

expect class UserProfilePresenter (context: Any, arguments: Map<String, String?>, view: UserProfileView,
                            personDao: PersonDao, impl: UstadMobileSystemImpl)
    : UserProfilePresenterCommon{

    override fun handleNavigation()
}