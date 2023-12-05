package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.assignment.submittername.GetAssignmentSubmitterNameUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.ApproveOrDeclinePendingEnrolmentUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.IApproveOrDeclinePendingEnrolmentRequestUseCase
import com.ustadmobile.core.domain.contententry.save.SaveContentEntryUseCase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on
import org.kodein.di.provider
import org.kodein.di.scoped

/**
 * Domain (UseCases) that are part of commonMain source.
 */
fun commonDomainDiModule(endpointScope: EndpointScope) = DI.Module("CommonDomain") {
    bind<IApproveOrDeclinePendingEnrolmentRequestUseCase>() with scoped(endpointScope).provider {
        ApproveOrDeclinePendingEnrolmentUseCase(db = instance())
    }

    bind<SaveContentEntryUseCase>() with scoped(endpointScope).provider {
        SaveContentEntryUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instanceOrNull(tag = DoorTag.TAG_REPO),
        )
    }

    bind<GetAssignmentSubmitterNameUseCase>() with scoped(endpointScope).provider {
        GetAssignmentSubmitterNameUseCase(
            repo = on(context).instance(tag = DoorTag.TAG_REPO),
            systemImpl = instance()
        )
    }
}
