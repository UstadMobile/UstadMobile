package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.ApproveOrDeclinePendingEnrolmentUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.IApproveOrDeclinePendingEnrolmentRequestUseCase
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

/**
 * Domain (UseCases) that are part of commonMain source.
 */
fun commonDomainDiModule(endpointScope: EndpointScope) = DI.Module("CommonDomain") {
    bind<IApproveOrDeclinePendingEnrolmentRequestUseCase>() with scoped(endpointScope).provider {
        ApproveOrDeclinePendingEnrolmentUseCase(db = instance())
    }
}
