package com.ustadmobile.core.impl.di

import com.ustadmobile.core.account.EndpointScope
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
import com.ustadmobile.core.domain.openlink.OnClickLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCase
import com.ustadmobile.core.domain.openlink.OpenExternalLinkUseCaseJs
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
import com.ustadmobile.core.domain.xapi.XapiStatementResource
import com.ustadmobile.core.domain.xapi.noninteractivecontentusagestatementrecorder.NonInteractiveContentXapiStatementRecorderFactory
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnClearUseCaseJs
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCase
import com.ustadmobile.core.domain.xapi.savestatementonclear.SaveStatementOnUnloadUseCaseJs
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCase
import com.ustadmobile.core.domain.xapi.starthttpsession.StartXapiSessionOverHttpUseCaseJs
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

fun DomainDiModuleJs(endpointScope: EndpointScope) = DI.Module("DomainDiModuleJs") {
    bind<EnqueueContentEntryImportUseCase>() with scoped(endpointScope).provider {
        EnqueueImportContentEntryUseCaseRemote(
            endpoint = context,
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
            repo = instance(tag = DoorTag.TAG_REPO),
            compressImageUseCase = CompressImageUseCaseJs(),
            deleteUrisUseCase = instance(),
        )
    }

    bind<SaveLocalUrisAsBlobsUseCase>() with scoped(endpointScope).singleton {
        SaveLocalUrisAsBlobUseCaseJs(
            chunkedUploadClientLocalUriUseCase = instance(),
            endpoint = context,
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

    bind<SetPasswordUseCase>() with scoped(endpointScope).provider {
        SetPasswordUseCaseJs(
            endpoint = context,
            repo = instance(tag = DoorTag.TAG_REPO),
            httpClient = instance()
        )
    }

    bind<ResolveXapiLaunchHrefUseCase>() with scoped(endpointScope).provider {
        ResolveXapiLaunchHrefUseCase(
            activeRepo = instance(tag = DoorTag.TAG_REPO),
            httpClient = instance(),
            json = instance(),
            xppFactory = instance(tag = DiTag.XPP_FACTORY_NSAWARE),
            startXapiSessionOverHttpUseCase = instance(),
            endpoint = context,
            stringHasher = instance(),
        )
    }

    bind<StartXapiSessionOverHttpUseCase>() with scoped(EndpointScope.Default).singleton {
        StartXapiSessionOverHttpUseCaseJs(
            endpoint = context,
            httpClient = instance(),
            repo = instance(tag = DoorTag.TAG_REPO),
            json = instance(),
        )
    }

    bind<LaunchXapiUseCase>() with scoped(endpointScope).provider {
        LaunchXapiUseCaseJs(
            resolveXapiLaunchHrefUseCase = instance()
        )
    }

    bind<MoveContentEntriesUseCase>() with scoped(EndpointScope.Default).provider {
        MoveContentEntriesUseCase(
            repo = instance(tag = DoorTag.TAG_REPO),
            systemImpl = instance()
        )
    }

    bind<DeleteContentEntryParentChildJoinUseCase>() with scoped(EndpointScope.Default).provider {
        DeleteContentEntryParentChildJoinUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<RestoreDeletedItemUseCase>() with scoped(EndpointScope.Default).provider {
        RestoreDeletedItemUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<DeletePermanentlyUseCase>() with scoped(EndpointScope.Default).provider {
        DeletePermanentlyUseCase(
            repoOrDb = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<SetClipboardStringUseCase>() with singleton {
        SetClipboardStringUseCaseJs()
    }

    /**
     * SaveAndUploadLocalUris - because saving the local uri as a blob does the upload itself  on JS,
     * there is no use of enqueueBlobUploadClientUseCase
     */
    bind<SaveAndUploadLocalUrisUseCase>() with scoped(EndpointScope.Default).singleton {
        SaveAndUploadLocalUrisUseCase(
            saveLocalUrisAsBlobsUseCase = instance(),
            enqueueBlobUploadClientUseCase = null,
            activeDb = instance(tag = DoorTag.TAG_DB),
            activeRepo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<OpenBlobUseCase>() with scoped(EndpointScope.Default).provider {
        OpenBlobUseCaseJs()
    }

    bind<OpenBlobUiUseCase>() with scoped(EndpointScope.Default).singleton {
        OpenBlobUiUseCase(
            openBlobUseCase = instance(),
            systemImpl = instance(),
        )
    }

    bind<CancelRemoteContentEntryImportUseCase>() with scoped(EndpointScope.Default).singleton {
        CancelRemoteContentEntryImportUseCase(
            endpoint = context,
            httpClient = instance(),
            repo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<DismissRemoteContentEntryImportErrorUseCase>() with scoped(EndpointScope.Default).singleton {
        DismissRemoteContentEntryImportErrorUseCase(
            endpoint = context,
            httpClient = instance(),
            repo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<XXStringHasher>() with singleton {
        XXStringHasherJs()
    }

    bind<XXHasher64Factory>() with singleton {
        XXHasher64FactoryJs()
    }

    bind<StoreActivitiesUseCase>() with scoped(EndpointScope.Default).singleton {
        StoreActivitiesUseCase(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
        )
    }

    bind<XapiStatementResource>() with scoped(EndpointScope.Default).singleton {
        XapiStatementResource(
            db = instance(tag = DoorTag.TAG_DB),
            repo = instance(tag = DoorTag.TAG_REPO),
            xxHasher = instance(),
            endpoint = context,
            xapiJson = instance(),
            hasherFactory = instance(),
            storeActivitiesUseCase = instance(),
        )
    }

    bind<SaveStatementOnClearUseCase>() with scoped(EndpointScope.Default).singleton {
        SaveStatementOnClearUseCaseJs(
            xapiStatementResource = instance(),
        )
    }

    bind<SaveStatementOnUnloadUseCase>() with scoped(EndpointScope.Default).singleton {
        SaveStatementOnUnloadUseCaseJs(
            endpoint = context,
            json = instance(),
        )
    }

    bind<NonInteractiveContentXapiStatementRecorderFactory>() with scoped(EndpointScope.Default).singleton {
        NonInteractiveContentXapiStatementRecorderFactory(
            saveStatementOnClearUseCase = instance(),
            saveStatementOnUnloadUseCase = instance(),
            xapiStatementResource = instance(),
            endpoint = context,
        )
    }

}
