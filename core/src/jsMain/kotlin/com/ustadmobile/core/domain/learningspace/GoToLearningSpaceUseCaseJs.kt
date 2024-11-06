package com.ustadmobile.core.domain.learningspace

import com.ustadmobile.core.account.LearningSpace
import com.ustadmobile.core.domain.makelink.MakeLinkUseCase
import com.ustadmobile.core.impl.nav.UstadNavController
import com.ustadmobile.core.util.ext.onActiveEndpoint
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import web.dom.document

class GoToLearningSpaceUseCaseJs(
    val di:DI
):GoToLearningSpaceUseCase{
    override fun invoke(
        url: String,
        navController: UstadNavController,
        args:Map<String, String>,
        viewName:String
    ) {
        val makeLinkUseCase: MakeLinkUseCase by di.on(LearningSpace(url)).instance()
    //   makeLinkUseCase.invoke(document)

    }
}