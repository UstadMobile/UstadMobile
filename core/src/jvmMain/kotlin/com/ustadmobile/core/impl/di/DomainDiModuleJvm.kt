package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCase
import com.ustadmobile.core.domain.contententry.importcontent.ImportContentUseCaseJvm
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped

fun DomainDiModuleJvm(endpointScope: EndpointScope) = DI.Module("JvmDomain") {
    bind<ImportContentUseCase>() with scoped(endpointScope).provider {
        ImportContentUseCaseJvm(
            db = instance(tag = DoorTag.TAG_DB),
            scheduler = instance(),
            endpoint = context,
        )
    }
}
