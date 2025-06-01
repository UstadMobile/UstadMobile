package com.ustadmobile.core.domain.learningspace

import com.ustadmobile.core.impl.nav.UstadNavController

class GoToLearningSpaceUseCaseAndroid() : GoToLearningSpaceUseCase {
    override fun invoke(
        url: String,
        navController: UstadNavController,
        args: Map<String, String>,
        viewName: String
    ) {

        navController.navigate(
            viewName,
            args

        )
    }
}