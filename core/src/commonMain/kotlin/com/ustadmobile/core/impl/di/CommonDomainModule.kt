package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
import com.ustadmobile.core.domain.assignment.submitmark.SubmitMarkUseCase
import com.ustadmobile.core.domain.assignment.submittername.GetAssignmentSubmitterNameUseCase
import com.ustadmobile.core.domain.clazz.CreateNewClazzUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.ApproveOrDeclinePendingEnrolmentUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.EnrolIntoCourseUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.IApproveOrDeclinePendingEnrolmentRequestUseCase
import com.ustadmobile.core.domain.clazzenrolment.pendingenrolment.RequestEnrolmentUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.DefaultLaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.LaunchContentEntryVersionUseCase
import com.ustadmobile.core.domain.contententry.save.SaveContentEntryUseCase
import com.ustadmobile.core.domain.makelink.MakeLinkUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.siteterms.GetLocaleForSiteTermsUseCase
import com.ustadmobile.core.domain.xapi.coursegroup.CreateXapiGroupForCourseGroupUseCase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

/**
 * Domain (UseCases) that are part of commonMain source for client (Desktop, JS, Android)
 *
 * Includes items that require systemImpl, so cannot be used on Android for dependencies that are
 * at the Application level di.
 */
fun commonDomainDiModule(endpointScope: EndpointScope) = DI.Module("CommonDomain") {
    bind<EnrolIntoCourseUseCase>() with scoped(endpointScope).provider {
        EnrolIntoCourseUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<IApproveOrDeclinePendingEnrolmentRequestUseCase>() with scoped(endpointScope).provider {
        ApproveOrDeclinePendingEnrolmentUseCase(
            repo = instance(tag = DoorTag.TAG_REPO),
            db = instance(tag = DoorTag.TAG_DB),
            enrolIntoCourseUseCase = instance(),
        )
    }

    bind<SaveContentEntryUseCase>() with scoped(endpointScope).provider {
        SaveContentEntryUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instanceOrNull(tag = DoorTag.TAG_REPO),
            enqueueSavePictureUseCase = instance(),
        )
    }

    bind<GetAssignmentSubmitterNameUseCase>() with scoped(endpointScope).provider {
        GetAssignmentSubmitterNameUseCase(
            repo = on(context).instance(tag = DoorTag.TAG_REPO),
            systemImpl = instance()
        )
    }

    bind<GetLocaleForSiteTermsUseCase>() with scoped(endpointScope).provider {
        GetLocaleForSiteTermsUseCase(
            supportedLangConfig = instance(),
            repo = on(context).instance(tag = DoorTag.TAG_REPO)
        )
    }

    bind<LaunchContentEntryVersionUseCase>() with scoped(endpointScope).provider {
        DefaultLaunchContentEntryVersionUseCase()
    }

    bind<RequestEnrolmentUseCase>() with scoped(endpointScope).provider {
        RequestEnrolmentUseCase(activeRepo = instance(tag = DoorTag.TAG_REPO))
    }

    bind<MakeLinkUseCase>() with scoped(endpointScope).singleton {
        MakeLinkUseCase(context)
    }

    bind<CreateNewClazzUseCase>() with scoped(endpointScope).singleton {
        CreateNewClazzUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO)
        )
    }

    bind<AddNewPersonUseCase>() with scoped(endpointScope).singleton {
        AddNewPersonUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<SubmitMarkUseCase>() with scoped(endpointScope).provider {
        SubmitMarkUseCase(
            repo = instance(tag = DoorTag.TAG_REPO),
            endpoint = context,
            createXapiGroupUseCase = instance(),
            xapiStatementResource = instance(),
            xxStringHasher = instance(),
        )
    }

    bind<CreateXapiGroupForCourseGroupUseCase>() with scoped(endpointScope).provider {
        CreateXapiGroupForCourseGroupUseCase(
            repo = instance(tag = DoorTag.TAG_REPO),
            endpoint = context,
            stringHasher = instance(),
        )
    }

}
