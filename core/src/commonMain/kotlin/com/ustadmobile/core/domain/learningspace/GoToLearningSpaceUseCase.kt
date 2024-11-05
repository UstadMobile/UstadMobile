package com.ustadmobile.core.domain.learningspace

import com.ustadmobile.core.impl.nav.UstadNavController

interface GoToLearningSpaceUseCase {
     operator fun invoke(
        url: String,
        navController: UstadNavController,
        args:Map<String, String>,
        viewName:String)
}