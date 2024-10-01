package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.LearningSpaceScope
import com.ustadmobile.core.db.UmAppDataLayer
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
import com.ustadmobile.core.domain.siteterms.GetLocaleForSiteTermsUseCase
import com.ustadmobile.core.domain.xapi.coursegroup.CreateXapiGroupForCourseGroupUseCase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

/**
 * Domain (UseCases) that are part of commonMain source for client (Desktop, JS, Android)
 *
 * Includes items that require systemImpl, so cannot be used on Android for dependencies that are
 * at the Application level di.
 */
fun commonDomainDiModule(endpointScope: LearningSpaceScope) = DI.Module("CommonDomain") {


    bind<EnrolIntoCourseUseCase>() with scoped(endpointScope).provider {
        EnrolIntoCourseUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
        )
    }

    bind<IApproveOrDeclinePendingEnrolmentRequestUseCase>() with scoped(endpointScope).provider {
        ApproveOrDeclinePendingEnrolmentUseCase(
            repo = instance<UmAppDataLayer>().repository,
            db = instance(tag = DoorTag.TAG_DB),
            enrolIntoCourseUseCase = instance(),
        )
    }

    bind<SaveContentEntryUseCase>() with scoped(endpointScope).provider {
        SaveContentEntryUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
            enqueueSavePictureUseCase = instance(),
        )
    }

    bind<GetAssignmentSubmitterNameUseCase>() with scoped(endpointScope).provider {
        GetAssignmentSubmitterNameUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            systemImpl = instance()
        )
    }

    bind<GetLocaleForSiteTermsUseCase>() with scoped(endpointScope).provider {
        GetLocaleForSiteTermsUseCase(
            supportedLangConfig = instance(),
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }

    bind<LaunchContentEntryVersionUseCase>() with scoped(endpointScope).provider {
        DefaultLaunchContentEntryVersionUseCase()
    }

    bind<RequestEnrolmentUseCase>() with scoped(endpointScope).provider {
        RequestEnrolmentUseCase(
            activeRepo = instance<UmAppDataLayer>().repositoryOrLocalDb
        )
    }

    bind<MakeLinkUseCase>() with scoped(endpointScope).singleton {
        MakeLinkUseCase(context)
    }

    bind<CreateNewClazzUseCase>() with scoped(endpointScope).singleton {
        CreateNewClazzUseCase(
            repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }


    bind<SubmitMarkUseCase>() with scoped(endpointScope).provider {
        SubmitMarkUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            learningSpace = context,
            createXapiGroupUseCase = instance(),
            xapiStatementResource = instance(),
            xxStringHasher = instance(),
            json = instance()
        )
    }

    bind<CreateXapiGroupForCourseGroupUseCase>() with scoped(endpointScope).provider {
        CreateXapiGroupForCourseGroupUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            learningSpace = context,
            stringHasher = instance(),
        )
    }

}
