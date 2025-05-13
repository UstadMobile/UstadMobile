package com.ustadmobile.core.domain.learningspace

import com.ustadmobile.core.impl.nav.UstadNavController

/**
 * On mobile and desktop we can use the normal navigation controller to the login screen (or any
 * other screen) for any learning space.
 *
 * On the web, because a different learning space is a different url origin, we need to set
 * location.href
 */
interface GoToLearningSpaceUseCase {

    operator fun invoke(
        url: String,
        navController: UstadNavController,
        args:Map<String, String>,
        viewName:String
    )
}