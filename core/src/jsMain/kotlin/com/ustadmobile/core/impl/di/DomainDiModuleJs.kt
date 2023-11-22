package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.import.ImportContentUseCaseJs
import com.ustadmobile.core.domain.openexternallink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openexternallink.OpenExternalLinkUseCaseJs
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped

fun DomainDiModuleJs(endpointScope: EndpointScope) = DI.Module("DomainDiModuleJs") {
    bind<ImportContentUseCase>() with scoped(endpointScope).provider {
        ImportContentUseCaseJs(
            endpoint = context,
            httpClient = instance(),
        )
    }

    bind<OpenExternalLinkUseCase>() with provider {
        OpenExternalLinkUseCaseJs()
    }
}
