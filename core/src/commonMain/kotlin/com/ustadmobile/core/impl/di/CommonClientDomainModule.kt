package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.ConsentRequestToParentUseCaseSendToServerImpl
import com.ustadmobile.core.account.LearningSpaceScope
import com.ustadmobile.core.account.SendConsentRequestToParentUseCase
import com.ustadmobile.core.domain.invite.SendClazzInvitesUseCase
import com.ustadmobile.core.domain.invite.SendClazzInvitesUseCaseSendToServerImpl
import com.ustadmobile.core.domain.username.GetUsernameSuggestionUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

/**
 * Common domain layer DI for all clients (web, mobile, desktop)
 */
fun commonClientDomainDiModule(
    learningSpaceScope: LearningSpaceScope
) = DI.Module("CommonDomainClient") {
    bind<SendClazzInvitesUseCase>() with scoped(learningSpaceScope).provider {
        SendClazzInvitesUseCaseSendToServerImpl(
            httpClient = instance(),
            learningSpace = context,
            json = instance()
        )
    }
    bind<SendConsentRequestToParentUseCase>() with scoped(learningSpaceScope).provider {
        ConsentRequestToParentUseCaseSendToServerImpl(
            httpClient = instance(),
            learningSpace = context,
            json = instance()
        )
    }

    bind<GetUsernameSuggestionUseCase>() with scoped(learningSpaceScope).singleton {
        GetUsernameSuggestionUseCase(
            httpClient = instance(),
            learningSpace = context,
        )
    }
}
