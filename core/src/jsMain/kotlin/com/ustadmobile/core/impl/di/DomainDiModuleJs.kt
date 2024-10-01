package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.LearningSpaceScope
import com.ustadmobile.core.db.UmAppDataLayer
import com.ustadmobile.core.domain.account.SetPasswordUseCase
import com.ustadmobile.core.domain.account.SetPasswordUseCaseJs
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUiUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCase
import com.ustadmobile.core.domain.blob.openblob.OpenBlobUseCaseJs
import com.ustadmobile.core.domain.blob.saveandupload.SaveAndUploadLocalUrisUseCase
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobUseCaseJs
import com.ustadmobile.core.domain.blob.savelocaluris.SaveLocalUrisAsBlobsUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCase
import com.ustadmobile.core.domain.blob.savepicture.EnqueueSavePictureUseCaseJs
import com.ustadmobile.core.domain.blob.savepicture.SavePictureUseCase
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCase
import com.ustadmobile.core.domain.clipboard.SetClipboardStringUseCaseJs
import com.ustadmobile.core.domain.compress.image.CompressImageUseCaseJs
import com.ustadmobile.core.domain.contententry.delete.DeleteContentEntryParentChildJoinUseCase
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCaseJs
import com.ustadmobile.core.domain.contententry.getmetadatafromuri.ContentEntryGetMetaDataFromUriUseCase
import com.ustadmobile.core.domain.contententry.importcontent.CancelRemoteContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.DismissRemoteContentEntryImportErrorUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueContentEntryImportUseCase
import com.ustadmobile.core.domain.contententry.importcontent.EnqueueImportContentEntryUseCaseRemote
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCase
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.LaunchXapiUseCaseJs
import com.ustadmobile.core.domain.language.SetLanguageUseCase
import com.ustadmobile.core.domain.language.SetLanguageUseCaseJs
import com.ustadmobile.core.domain.contententry.launchcontent.xapi.ResolveXapiLaunchHrefUseCase
import com.ustadmobile.core.domain.contententry.move.MoveContentEntriesUseCase
import com.ustadmobile.core.domain.deleteditem.DeletePermanentlyUseCase
import com.ustadmobile.core.domain.deleteditem.RestoreDeletedItemUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCase
import com.ustadmobile.core.domain.getapiurl.GetApiUrlUseCaseDirect
import com.ustadmobile.core.domain.openlink.OnClickLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJs
import com.ustadmobile.core.domain.passkey.PasskeyRequestJsonUseCase
import com.ustadmobile.core.domain.person.AddNewPersonUseCase
import com.ustadmobile.core.domain.phonenumber.IPhoneNumberUtil
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCase
import com.ustadmobile.core.domain.phonenumber.OnClickPhoneNumUseCaseJs
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCaseJs
import com.ustadmobile.core.domain.phonenumber.PhoneNumValidatorUseCase
import com.ustadmobile.core.domain.phonenumber.PhoneNumberUtilJs
import com.ustadmobile.core.domain.sendemail.OnClickEmailUseCase
import com.ustadmobile.core.domain.sendemail.OnClickSendEmailUseCaseJs
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCase
import com.ustadmobile.core.domain.tmpfiles.DeleteUrisUseCaseJs
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCase
import com.ustadmobile.core.domain.tmpfiles.IsTempFileCheckerUseCaseJs
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCase
import com.ustadmobile.core.domain.upload.ChunkedUploadClientLocalUriUseCaseJs
import com.ustadmobile.core.domain.xapi.StoreActivitiesUseCase
import com.ustadmobile.core.domain.xapi.XapiJson
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder.NonInteractiveContentXapiStatementRecorderFactory
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCaseJs
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCaseJs
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCase
import com.ustadmobile.core.domain.xapi.session.ResumeOrStartXapiSessionUseCaseJs
import com.ustadmobile.core.domain.xxhash.XXHasher64Factory
import com.ustadmobile.core.domain.xxhash.XXHasher64FactoryJs
import com.ustadmobile.core.domain.xxhash.XXStringHasher
import com.ustadmobile.core.domain.xxhash.XXStringHasherJs
import com.ustadmobile.core.util.DiTag
import com.ustadmobile.door.ext.DoorTag
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.provider
import org.kodein.di.scoped
import org.kodein.di.singleton

fun DomainDiModuleJs(endpointScope: LearningSpaceScope) = DI.Module("DomainDiModuleJs") {
    bind<EnqueueContentEntryImportUseCase>() with scoped(endpointScope).provider {
        EnqueueImportContentEntryUseCaseRemote(
            learningSpace = context,
            httpClient = instance(),
            json = instance(),
        )
    }

    bind<OpenExternalLinkUseCase>() with provider {
        OpenExternalLinkUseCaseJs()
    }

    bind<OnClickLinkUseCase>() with singleton {
        OnClickLinkUseCase(
            navController = instance(),
            accountManager = instance(),
            openExternalLinkUseCase = instance(),
            apiUrlConfig = instance(),
        )
    }

    bind<IPhoneNumberUtil>() with singleton {
        PhoneNumberUtilJs()
    }

    bind<PhoneNumValidatorUseCase>() with provider {
        PhoneNumValidatorUseCaseJs()
    }

    bind<OnClickPhoneNumUseCase>() with provider {
        OnClickPhoneNumUseCaseJs()
    }

    bind<OnClickEmailUseCase>() with provider {
        OnClickSendEmailUseCaseJs()
    }

    bind<SetLanguageUseCase>() with provider {
        SetLanguageUseCaseJs(
            languagesConfig = instance()
        )
    }

    bind<ChunkedUploadClientLocalUriUseCase>() with singleton {
        ChunkedUploadClientLocalUriUseCaseJs()
    }

    bind<EnqueueSavePictureUseCase>() with scoped(endpointScope).singleton {
        EnqueueSavePictureUseCaseJs(
            savePictureUseCase = instance()
        )
    }


    bind<IsTempFileCheckerUseCase>() with singleton {
        IsTempFileCheckerUseCaseJs()
    }

    bind<DeleteUrisUseCase>() with singleton {
        DeleteUrisUseCaseJs(
            isTempFileCheckerUseCase = instance()
        )
    }

    bind<SavePictureUseCase>() with scoped(endpointScope).singleton {
        SavePictureUseCase(
            saveLocalUrisAsBlobUseCase = instance(),
            enqueueBlobUploadClientUseCase = null,
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
            compressImageUseCase = CompressImageUseCaseJs(),
            deleteUrisUseCase = instance(),
        )
    }

    bind<SaveLocalUrisAsBlobsUseCase>() with scoped(endpointScope).singleton {
        SaveLocalUrisAsBlobUseCaseJs(
            chunkedUploadClientLocalUriUseCase = instance(),
            learningSpace = context,
            json = instance(),
            db = instance(tag = DoorTag.TAG_DB),
        )
    }


    bind<ContentEntryGetMetaDataFromUriUseCase>() with provider {
        ContentEntryGetMetaDataFromUriUseCaseJs(
            json = instance(),
            chunkedUploadClientLocalUriUseCase = instance()
        )
    }

    bind<PasskeyRequestJsonUseCase>() with provider {
        PasskeyRequestJsonUseCase(
            systemImpl = instance(),
            json = instance()
        )
    }

    bind<SetPasswordUseCase>() with scoped(endpointScope).provider {
        SetPasswordUseCaseJs(
            learningSpace = context,
            repo = instance<UmAppDataLayer>().requireRepository(),
            httpClient = instance()
        )
    }

    bind<ResolveXapiLaunchHrefUseCase>() with scoped(endpointScope).provider {
        ResolveXapiLaunchHrefUseCase(
            activeRepoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
            httpClient = instance(),
            json = instance(),
            xppFactory = instance(tag = DiTag.XPP_FACTORY_NSAWARE),
            learningSpace = context,
            resumeOrStartXapiSessionUseCase = instance(),
            accountManager = instance(),
            getApiUrlUseCase = instance(),
        )
    }

    bind<XapiJson>() with singleton {
        XapiJson()
    }

    bind<GetApiUrlUseCase>() with scoped(endpointScope).singleton {
        GetApiUrlUseCaseDirect(context)
    }

    bind<ResumeOrStartXapiSessionUseCase>() with scoped(endpointScope).singleton {
        ResumeOrStartXapiSessionUseCaseJs(
            learningSpace = context,
            httpClient = instance(),
            repo = instance<UmAppDataLayer>().requireRepository(),
            xapiJson = instance()
        )
    }

    bind<LaunchXapiUseCase>() with scoped(endpointScope).provider {
        LaunchXapiUseCaseJs(
            resolveXapiLaunchHrefUseCase = instance()
        )
    }

    bind<MoveContentEntriesUseCase>() with scoped(LearningSpaceScope.Default).provider {
        MoveContentEntriesUseCase(
            repo = instance<UmAppDataLayer>().repositoryOrLocalDb,
            systemImpl = instance()
        )
    }

    bind<DeleteContentEntryParentChildJoinUseCase>() with scoped(LearningSpaceScope.Default).provider {
        DeleteContentEntryParentChildJoinUseCase(
            repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }

    bind<RestoreDeletedItemUseCase>() with scoped(LearningSpaceScope.Default).provider {
        RestoreDeletedItemUseCase(
            repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }

    bind<DeletePermanentlyUseCase>() with scoped(LearningSpaceScope.Default).provider {
        DeletePermanentlyUseCase(
            repoOrDb = instance<UmAppDataLayer>().repositoryOrLocalDb,
        )
    }

    bind<SetClipboardStringUseCase>() with singleton {
        SetClipboardStringUseCaseJs()
    }

    /**
     * SaveAndUploadLocalUris - because saving the local uri as a blob does the upload itself  on JS,
     * there is no use of enqueueBlobUploadClientUseCase
     */
    bind<SaveAndUploadLocalUrisUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        SaveAndUploadLocalUrisUseCase(
            saveLocalUrisAsBlobsUseCase = instance(),
            enqueueBlobUploadClientUseCase = null,
            activeDb = instance(tag = DoorTag.TAG_DB),
            activeRepo = instance<UmAppDataLayer>().repository,
        )
    }

    bind<OpenBlobUseCase>() with scoped(LearningSpaceScope.Default).provider {
        OpenBlobUseCaseJs()
    }

    bind<OpenBlobUiUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        OpenBlobUiUseCase(
            openBlobUseCase = instance(),
            systemImpl = instance(),
        )
    }

    bind<CancelRemoteContentEntryImportUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        CancelRemoteContentEntryImportUseCase(
            learningSpace = context,
            httpClient = instance(),
            repo = instance<UmAppDataLayer>().requireRepository(),
        )
    }

    bind<DismissRemoteContentEntryImportErrorUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        DismissRemoteContentEntryImportErrorUseCase(
            learningSpace = context,
            httpClient = instance(),
            repo = instance<UmAppDataLayer>().requireRepository(),
        )
    }

    bind<XXStringHasher>() with singleton {
        XXStringHasherJs()
    }

    bind<XXHasher64Factory>() with singleton {
        XXHasher64FactoryJs()
    }

    bind<StoreActivitiesUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        StoreActivitiesUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
        )
    }

    bind<XapiStatementResource>() with scoped(LearningSpaceScope.Default).singleton {
        XapiStatementResource(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
            xxHasher = instance(),
            learningSpace = context,
            xapiJson = instance(),
            hasherFactory = instance(),
            storeActivitiesUseCase = instance(),
        )
    }

    bind<SaveStatementOnClearUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        SaveStatementOnClearUseCaseJs(
            xapiStatementResource = instance(),
        )
    }

    bind<SaveStatementOnUnloadUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        SaveStatementOnUnloadUseCaseJs(
            learningSpace = context,
            json = instance(),
        )
    }

    bind<NonInteractiveContentXapiStatementRecorderFactory>() with scoped(LearningSpaceScope.Default).singleton {
        NonInteractiveContentXapiStatementRecorderFactory(
            saveStatementOnClearUseCase = instance(),
            saveStatementOnUnloadUseCase = instance(),
            xapiStatementResource = instance(),
            learningSpace = context,
        )
    }

    bind<AddNewPersonUseCase>() with scoped(LearningSpaceScope.Default).singleton {
        AddNewPersonUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance<UmAppDataLayer>().repository,
        )
    }

}
