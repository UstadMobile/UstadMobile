package com.ustadmobile.core.controller

import com.ustadmobile.core.contentformats.ContentImportManager
import com.ustadmobile.core.contentformats.metadata.ImportedContentEntryMetaData
import com.ustadmobile.core.controller.ContentEntryList2Presenter.Companion.KEY_SELECTED_ITEMS
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.util.MessageIdOption
import com.ustadmobile.core.util.UMFileUtil
import com.ustadmobile.core.util.ext.logErrorReport
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.util.safeParse
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_IMPORTED_METADATA
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_URI
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.instanceOrNull
import org.kodein.di.on


class ContentEntryEdit2Presenter(context: Any,
                                 arguments: Map<String, String>, view: ContentEntryEdit2View,
                                 lifecycleOwner: DoorLifecycleOwner,
                                 di: DI)
    : UstadEditPresenter<ContentEntryEdit2View, ContentEntryWithLanguage>(context, arguments, view, di, lifecycleOwner),
        ContentEntryAddOptionsListener {

    private val contentImportManager: ContentImportManager?
            by on(accountManager.activeAccount).instanceOrNull()

    private lateinit var destinationOnFinish: String

    private val httpClient: HttpClient by di.instance()

    enum class LicenceOptions(val optionVal: Int, val messageId: Int) {
        LICENSE_TYPE_CC_BY(ContentEntry.LICENSE_TYPE_CC_BY, MessageID.licence_type_cc_by),
        LICENSE_TYPE_CC_BY_SA(ContentEntry.LICENSE_TYPE_CC_BY_SA, MessageID.licence_type_cc_by_sa),
        LICENSE_TYPE_CC_BY_SA_NC(ContentEntry.LICENSE_TYPE_CC_BY_SA_NC, MessageID.licence_type_cc_by_sa_nc),
        LICENSE_TYPE_CC_BY_NC(ContentEntry.LICENSE_TYPE_CC_BY_NC, MessageID.licence_type_cc_by_nc),
        ALL_RIGHTS_RESERVED(ContentEntry.ALL_RIGHTS_RESERVED, MessageID.licence_type_all_rights),
        LICENSE_TYPE_CC_BY_NC_SA(ContentEntry.LICENSE_TYPE_CC_BY_NC_SA, MessageID.licence_type_cc_by_nc_sa),
        LICENSE_TYPE_PUBLIC_DOMAIN(ContentEntry.LICENSE_TYPE_PUBLIC_DOMAIN, MessageID.licence_type_public_domain),
        LICENSE_TYPE_OTHER(ContentEntry.LICENSE_TYPE_OTHER, MessageID.other),
        LICENSE_TYPE_CC0(ContentEntry.LICENSE_TYPE_CC_0, MessageID.license_type_cc_0)
    }

    enum class CompletionCriteriaOptions(val optionVal: Int, val messageId: Int) {
        AUTOMATIC(ContentEntry.COMPLETION_CRITERIA_AUTOMATIC,
                MessageID.automatic),
        MIN_SCORE(ContentEntry.COMPLETION_CRITERIA_MIN_SCORE,
                MessageID.minimum_score),
        STUDENTS_MARKS_COMPLETE(ContentEntry.COMPLETION_CRITERIA_MARKED_BY_STUDENT,
                MessageID.student_marks_content)
    }

    class CompletionCriteriaMessageIdOption(day: CompletionCriteriaOptions, context: Any)
        : MessageIdOption(day.messageId, context, day.optionVal)


    data class UmStorageOptions(var messageId: Int, var label: String)

    private var parentEntryUid: Long = 0


    open class StorageOptions(context: Any, val storage: UmStorageOptions) : MessageIdOption(storage.messageId, context) {
        override fun toString(): String {
            return storage.label
        }
    }

    class LicenceMessageIdOptions(licence: LicenceOptions, context: Any)
        : MessageIdOption(licence.messageId, context, licence.optionVal)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.licenceOptions = LicenceOptions.values().map { LicenceMessageIdOptions(it, context) }
        view.completionCriteriaOptions = CompletionCriteriaOptions.values().map { CompletionCriteriaMessageIdOption(it, context) }
        parentEntryUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0
        destinationOnFinish = if ((arguments[ARG_ENTITY_UID]?.toLong() ?: 0L) == 0L)
            ContentEntryList2View.VIEW_NAME else ContentEntryDetailView.VIEW_NAME
        GlobalScope.launch(doorMainDispatcher()) {
            view.storageOptions = systemImpl.getStorageDirsAsync(context)
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithLanguage? {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0
        val isLeaf = arguments[ARG_LEAF]?.toBoolean()
        view.showCompletionCriteria = isLeaf ?: false
        val metaData = arguments[ARG_IMPORTED_METADATA]
        val uri = arguments[ARG_URI]
        if (db is DoorDatabaseRepository) {
            if (uri != null) {
                return handleFileSelection(uri)
            }
            if (metaData != null) {
                val importedMetadata = safeParse(di, ImportedContentEntryMetaData.serializer(), metaData)
                view.entryMetaData = importedMetadata
                return importedMetadata.contentEntry
            }
        }
        return withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.contentEntryDao?.findEntryWithLanguageByEntryIdAsync(entityUid)
        } ?: ContentEntryWithLanguage().apply {
            leaf = isLeaf ?: (contentFlags != ContentEntry.FLAG_IMPORTED)
        }
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntryWithLanguage? {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val metaDataStr = bundle[ARG_IMPORTED_METADATA]
        if (metaDataStr != null) {
            view.entryMetaData = safeParse(di, ImportedContentEntryMetaData.serializer(), metaDataStr)
        }
        var editEntity: ContentEntryWithLanguage? = null
        editEntity = if (entityJsonStr != null) {
            safeParse(di, ContentEntryWithLanguage.serializer(), entityJsonStr)
        } else {
            ContentEntryWithLanguage()
        }
        return editEntity
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(SAVED_STATE_KEY_URI, ListSerializer(String.serializer()),
                String::class) {
            val uri = it.firstOrNull() ?: return@observeSavedStateResult
            view.loading = true
            view.fieldsEnabled = false

            GlobalScope.launch(doorMainDispatcher()){
                view.entity = handleFileSelection(uri)
            }


            requireSavedStateHandle()[SAVED_STATE_KEY_URI] = null
        }

        observeSavedStateResult(SAVED_STATE_KEY_METADATA, ListSerializer(ImportedContentEntryMetaData.serializer()),
                ImportedContentEntryMetaData::class) {
            val metadata = it.firstOrNull() ?: return@observeSavedStateResult
            view.loading = true
            // back from navigate import
            view.entryMetaData = metadata
            val entry = view.entryMetaData?.contentEntry
            val entryUid = arguments[ARG_ENTITY_UID]
            if (entry != null) {
                if (entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                view.fileImportErrorVisible = false
                entity = entry
            }
            view.loading = false

            requireSavedStateHandle()[SAVED_STATE_KEY_METADATA] = null
        }


    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        val entityVal = view.entity
        savedState.putEntityAsJson(ARG_ENTITY_JSON, null, entityVal)
        savedState.putEntityAsJson(ARG_IMPORTED_METADATA, ImportedContentEntryMetaData.serializer(), view.entryMetaData)
    }


    override fun handleClickSave(entity: ContentEntryWithLanguage) {
        view.titleErrorEnabled = false
        view.fileImportErrorVisible = false
        GlobalScope.launch(doorMainDispatcher()) {
            val canCreate = isImportValid(entity)

            if (canCreate) {
                entity.licenseName = view.licenceOptions?.firstOrNull { it.code == entity.licenseType }.toString()
                if (entity.contentEntryUid == 0L) {
                    entity.contentEntryUid = repo.contentEntryDao.insertAsync(entity)

                    if (entity.entryId == null) {
                        entity.entryId = accountManager.activeAccount.endpointUrl +
                                "${entity.contentEntryUid}/${randomUuid()}"
                        repo.contentEntryDao.updateAsync(entity)
                    }
                    val contentEntryJoin = ContentEntryParentChildJoin().apply {
                        cepcjChildContentEntryUid = entity.contentEntryUid
                        cepcjParentContentEntryUid = parentEntryUid
                    }
                    repo.contentEntryParentChildJoinDao.insertAsync(contentEntryJoin)
                } else {
                    repo.contentEntryDao.updateAsync(entity)
                }

                val language = entity.language
                if (language != null && language.langUid == 0L) {
                    repo.languageDao.insertAsync(language)
                }

                val metaData = view.entryMetaData
                val uri = metaData?.uri
                val videoDimensions = view.videoDimensions
                val conversionParams = mapOf("compress" to view.compressionEnabled.toString(),
                        "dimensions" to "${videoDimensions.first}x${videoDimensions.second}")
                if (metaData != null && uri != null) {

                    if (uri.startsWith("content://")) {

                        metaData.contentEntry = entity
                        contentImportManager?.queueImportContentFromFile(uri, metaData,
                                view.storageOptions?.get(view.selectedStorageIndex)?.dirURI.toString(),
                                ContainerImportJob.CLIENT_IMPORT_MODE, conversionParams)

                        systemImpl.popBack(destinationOnFinish, popUpInclusive = false, context)
                        return@launch


                    } else {

                        var client: HttpResponse? = null
                        try {

                            client = httpClient.post<HttpStatement>() {
                                url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                                        "/import/downloadLink"))
                                parameter("parentUid", parentEntryUid)
                                parameter("scraperType", view.entryMetaData?.scraperType)
                                parameter("url", view.entryMetaData?.uri)
                                parameter("conversionParams",
                                        Json.encodeToString(MapSerializer(String.serializer(),
                                                String.serializer()),
                                                conversionParams))
                                header("content-type", "application/json")
                                body = entity
                            }.execute()

                        } catch (e: Exception) {
                            view.showSnackBar("${
                                systemImpl.getString(MessageID.error,
                                        context)
                            }: ${e.message ?: ""}", {})
                            return@launch
                        }

                        if (client.status.value != 200) {
                            view.showSnackBar(systemImpl.getString(MessageID.error,
                                    context), {})
                            return@launch
                        }

                        systemImpl.popBack(destinationOnFinish, popUpInclusive = false, context)
                        return@launch

                    }
                } else {
                    // its a folder, check if there is any selected items from previous screen
                    if (arguments.containsKey(KEY_SELECTED_ITEMS)) {
                        val selectedItems = arguments[KEY_SELECTED_ITEMS]?.split(",")?.map { it.trim().toLong() }
                                ?: listOf()
                        repo.contentEntryParentChildJoinDao.moveListOfEntriesToNewParent(entity.contentEntryUid, selectedItems)
                    }
                }

                systemImpl.popBack(destinationOnFinish, popUpInclusive = false, context)

            } else {
                view.titleErrorEnabled = entity.title == null
                view.fileImportErrorVisible = entity.title != null && entity.leaf
                        && view.entryMetaData?.uri == null
            }
        }
    }

    fun isImportValid(entity: ContentEntryWithLanguage): Boolean {
        return entity.title != null && (!entity.leaf || entity.contentEntryUid != 0L ||
                (entity.contentEntryUid == 0L && view.entryMetaData?.uri != null))
    }

    suspend fun handleFileSelection(uri: String): ContentEntryWithLanguage? {
        view.loading = true
        view.fieldsEnabled = true

        var entry: ContentEntryWithLanguage? = null
        try {
            val metadata = contentImportManager?.extractMetadata(uri)
            view.entryMetaData = metadata
            when (metadata) {
                null -> {
                    view.showSnackBar(systemImpl.getString(MessageID.import_link_content_not_supported, context))
                }
            }

            entry = metadata?.contentEntry
            val entryUid = arguments[ARG_ENTITY_UID]
            if (entry != null) {
                if (entryUid != null) entry.contentEntryUid = entryUid.toString().toLong()
                view.fileImportErrorVisible = false
                if (metadata?.mimeType?.startsWith("video/") == true &&
                        !metadata.uri.lowercase().startsWith("https://drive.google.com")) {
                    view.videoUri = uri
                }
            }
            view.loading = false
            view.fieldsEnabled = true
        }catch (e: Exception){
            view.showSnackBar(systemImpl.getString(MessageID.import_link_content_not_supported, context))
            repo.errorReportDao.logErrorReport(ErrorReport.SEVERITY_ERROR, e, this)
        }

        return entry
    }

    override fun onClickNewFolder() {
        // wont happen in edit screen
    }

    override fun onClickImportFile() {
        val args = mutableMapOf(
                SelectFileView.ARG_SELECTION_MODE to SelectFileView.SELECTION_MODE_FILE,
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, SelectFileView.VIEW_NAME, String::class,
                        String.serializer(), SAVED_STATE_KEY_URI,
                        arguments = args)
        )
    }

    override fun onClickImportLink() {
        val args = mutableMapOf(ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, ContentEntryImportLinkView.VIEW_NAME,
                        ImportedContentEntryMetaData::class,
                        ImportedContentEntryMetaData.serializer(), SAVED_STATE_KEY_METADATA,
                        arguments = args)
        )
    }

    override fun onClickImportGallery() {
        val args = mutableMapOf(
                SelectFileView.ARG_SELECTION_MODE to SelectFileView.SELECTION_MODE_GALLERY,
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, SelectFileView.VIEW_NAME, String::class,
                        String.serializer(), SAVED_STATE_KEY_URI,
                        arguments = args)
        )
    }

    companion object {

        const val SAVED_STATE_KEY_URI = "URI"

        const val SAVED_STATE_KEY_METADATA = "importedMetadata"


    }

}