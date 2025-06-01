package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.LearningSpaceScope
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.domain.account.CheckRegistrationAllowedUseCase
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
import com.ustadmobile.core.domain.credentials.username.CreateCredentialUsernameUseCase
import com.ustadmobile.core.domain.credentials.username.ParseCredentialUsernameUseCase
import com.ustadmobile.core.domain.invite.ParseInviteUseCase
import com.ustadmobile.core.domain.makelink.MakeLinkUseCase
import com.ustadmobile.core.domain.navigation.GetDefaultDestinationUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.siteterms.GetLocaleForSiteTermsUseCase
import com.ustadmobile.core.domain.xapi.coursegroup.CreateXapiGroupForCourseGroupUseCase
import com.ustadmobile.core.domain.xapi.formatresponse.FormatStatementResponseUseCase
import com.ustadmobile.core.viewmodel.clazz.detailoverview.CopyCourseUseCase
import com.ustadmobile.core.username.UsernameSuggestionUseCase
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

/**
 * Domain (UseCases) that are part of commonMain source for client (Desktop, JS, Android)
 *
 * Includes items that require systemImpl, so cannot be used on Android for dependencies that are
 * at the Application level di.
 */
fun commonDomainDiModule(learningSpaceScope: LearningSpaceScope) = DI.Module("CommonDomain") {

    bind<EnrolIntoCourseUseCase>() with scoped(learningSpaceScope).provider {
        EnrolIntoCourseUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
        )
    }

    bind<IApproveOrDeclinePendingEnrolmentRequestUseCase>() with scoped(learningSpaceScope).provider {
        ApproveOrDeclinePendingEnrolmentUseCase(
            repo = instance<UmAppDataLayer>().repository,
            db = instance(tag = DoorTag.TAG_DB),
            enrolIntoCourseUseCase = instance(),
        )
    }

    bind<SaveContentEntryUseCase>() with scoped(learningSpaceScope).provider {
        SaveContentEntryUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
            enqueueSavePictureUseCase = instance(),
        )
    }

    bind<GetAssignmentSubmitterNameUseCase>() with scoped(learningSpaceScope).provider {
        GetAssignmentSubmitterNameUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            systemImpl = instance()
        )
    }

    bind<GetLocaleForSiteTermsUseCase>() with scoped(learningSpaceScope).provider {
        GetLocaleForSiteTermsUseCase(
            supportedLangConfig = instance(),
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }

    bind<LaunchContentEntryVersionUseCase>() with scoped(learningSpaceScope).provider {
        DefaultLaunchContentEntryVersionUseCase()
    }

    bind<RequestEnrolmentUseCase>() with scoped(learningSpaceScope).provider {
        RequestEnrolmentUseCase(
            activeRepo = instance<UmAppDataLayer>().repositoryOrLocalDb
        )
    }

    bind<MakeLinkUseCase>() with scoped(learningSpaceScope).singleton {
        MakeLinkUseCase(context)
    }

    bind<CreateNewClazzUseCase>() with scoped(learningSpaceScope).singleton {
        CreateNewClazzUseCase(
            repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }
    bind<UsernameSuggestionUseCase>() with scoped(LearningSpaceScope.Default).provider {
        UsernameSuggestionUseCase(
            filterUsernameUseCase = instance(),
            db = instance(tag = DoorTag.TAG_DB)
        )
    }

    bind<ParseInviteUseCase>() with singleton {
        ParseInviteUseCase(
            phoneNumValidatorUseCase = instance(),
            validateEmailUseCase = instance()
        )
    }

    bind<CopyCourseUseCase>() with scoped(learningSpaceScope).singleton {
        CopyCourseUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
            accountManager = instance()
        )
    }

    bind<AddNewPersonUseCase>() with scoped(learningSpaceScope).singleton {
        AddNewPersonUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<SubmitMarkUseCase>() with scoped(learningSpaceScope).provider {
        SubmitMarkUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            learningSpace = context,
            createXapiGroupUseCase = instance(),
            xapiStatementResource = instance(),
            xxStringHasher = instance(),
            json = instance()
        )
    }

    bind<CreateXapiGroupForCourseGroupUseCase>() with scoped(learningSpaceScope).provider {
        CreateXapiGroupForCourseGroupUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            learningSpace = context,
            stringHasher = instance(),
        )
    }

    bind<FormatStatementResponseUseCase>() with scoped(learningSpaceScope).singleton {
        FormatStatementResponseUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instanceOrNull(tag = DoorTag.TAG_REPO),
        )
    }

    bind<GetDefaultDestinationUseCase>() with scoped(learningSpaceScope).singleton {
        GetDefaultDestinationUseCase(
            systemUrlConfig = instance(),
            learningSpace = context,
        )
    }

    bind<CheckRegistrationAllowedUseCase>() with scoped(learningSpaceScope).singleton {
        CheckRegistrationAllowedUseCase(dataLayer = instance())
    }

    bind<CreateCredentialUsernameUseCase>() with scoped(learningSpaceScope).singleton {
        CreateCredentialUsernameUseCase(learningSpace = context)
    }

    bind<ParseCredentialUsernameUseCase>() with singleton {
        ParseCredentialUsernameUseCase()
    }

}
