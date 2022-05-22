package com.ustadmobile.core.controller

import com.soywiz.klock.DateTime
import com.ustadmobile.core.contentjob.ContentJobManager
import com.ustadmobile.core.contentjob.ContentPluginManager
import com.ustadmobile.core.contentjob.MetadataResult
import com.ustadmobile.core.controller.ContentEntryList2Presenter.Companion.KEY_SELECTED_ITEMS
import com.ustadmobile.core.db.JobStatus
import com.ustadmobile.core.db.UmAppDatabase
import com.ustadmobile.core.generated.locale.MessageID
import com.ustadmobile.core.impl.ContainerStorageManager
import com.ustadmobile.core.impl.NavigateForResultOptions
import com.ustadmobile.core.io.ext.getSize
import com.ustadmobile.core.io.ext.isRemote
import com.ustadmobile.core.schedule.localMidnight
import com.ustadmobile.core.schedule.toLocalMidnight
import com.ustadmobile.core.schedule.toOffsetByTimezone
import com.ustadmobile.core.util.*
import com.ustadmobile.core.util.ext.effectiveTimeZone
import com.ustadmobile.core.util.ext.encodeStringMapToString
import com.ustadmobile.core.util.ext.putEntityAsJson
import com.ustadmobile.core.util.ext.putFromOtherMapIfPresent
import com.ustadmobile.core.view.*
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.ARG_IMPORTED_METADATA
import com.ustadmobile.core.view.ContentEntryEdit2View.Companion.BLOCK_REQUIRED
import com.ustadmobile.core.view.UstadEditView.Companion.ARG_ENTITY_JSON
import com.ustadmobile.core.view.UstadView.Companion.ARG_CLAZZUID
import com.ustadmobile.core.view.UstadView.Companion.ARG_ENTITY_UID
import com.ustadmobile.core.view.UstadView.Companion.ARG_LEAF
import com.ustadmobile.core.view.UstadView.Companion.ARG_PARENT_ENTRY_UID
import com.ustadmobile.door.DoorDatabaseRepository
import com.ustadmobile.door.DoorLifecycleOwner
import com.ustadmobile.door.DoorUri
import com.ustadmobile.door.doorMainDispatcher
import com.ustadmobile.door.ext.doorPrimaryKeyManager
import com.ustadmobile.door.ext.onDbThenRepoWithTimeout
import com.ustadmobile.door.ext.onRepoWithFallbackToDb
import com.ustadmobile.door.ext.withDoorTransactionAsync
import com.ustadmobile.door.util.randomUuid
import com.ustadmobile.door.util.systemTimeInMillis
import com.ustadmobile.lib.db.entities.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on


class ContentEntryEdit2Presenter(
    context: Any,
    arguments: Map<String, String>,
    view: ContentEntryEdit2View,
    lifecycleOwner: DoorLifecycleOwner,
    di: DI
) : UstadEditPresenter<ContentEntryEdit2View, ContentEntryWithBlockAndLanguage>(
    context,
    arguments,
    view,
    di,
    lifecycleOwner), ContentEntryAddOptionsListener {

    private val pluginManager: ContentPluginManager by on(accountManager.activeAccount).instance()

    private val contentJobManager: ContentJobManager by di.instance()

    private val httpClient: HttpClient by di.instance()

    private val containerStorageManager: ContainerStorageManager by on(accountManager.activeAccount).instance()

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

    class CompletionCriteriaMessageIdOption(day: CompletionCriteriaOptions, context: Any, di: DI)
        : MessageIdOption(day.messageId, context, day.optionVal, di = di)


    private var parentEntryUid: Long = 0

    class LicenceMessageIdOptions(licence: LicenceOptions, context: Any, di: DI)
        : MessageIdOption(licence.messageId, context, licence.optionVal, di = di)

    override val persistenceMode: PersistenceMode
        get() = PersistenceMode.DB

    private val json: Json by instance()

    override fun onCreate(savedState: Map<String, String>?) {
        super.onCreate(savedState)
        view.licenceOptions = LicenceOptions.values().map { LicenceMessageIdOptions(it, context, di) }
        view.completionCriteriaOptions = CompletionCriteriaOptions.values().map {
            CompletionCriteriaMessageIdOption(it, context, di)
        }
        parentEntryUid = arguments[ARG_PARENT_ENTRY_UID]?.toLong() ?: 0
        presenterScope.launch(doorMainDispatcher()) {
            view.storageOptions = containerStorageManager.storageList
        }
    }

    override suspend fun onLoadEntityFromDb(db: UmAppDatabase): ContentEntryWithBlockAndLanguage {
        val entityUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0
        val isLeaf = arguments[ARG_LEAF]?.toBoolean()
        val metaData = arguments[ARG_IMPORTED_METADATA]
        val isBlockRequired = arguments[BLOCK_REQUIRED].toBoolean()
        val clazzUid = arguments[ARG_CLAZZUID]?.toLongOrNull() ?: 0L

        //Show the update button only when an existing leaf entity is being edited
        view.showUpdateContentButton = entityUid != 0L && isLeaf == true

        if (db is DoorDatabaseRepository) {
            if (metaData != null) {
                val metadataResult = safeParse(di, MetadataResult.serializer(), metaData)
                return handleMetadataResult(metadataResult, isBlockRequired)
            }
        }
        val entry = withTimeoutOrNull(2000) {
            db.takeIf { entityUid != 0L }?.contentEntryDao?.findEntryWithBlockAndLanguageByUidAsync(entityUid)
        } ?: ContentEntryWithBlockAndLanguage().apply {
            leaf = isLeaf ?: (contentFlags != ContentEntry.FLAG_IMPORTED)
        }

        view.contentEntryPicture = db.onDbThenRepoWithTimeout(2000) { dbToUse, _ ->
            dbToUse.takeIf { entityUid != 0L }?.contentEntryPictureDao?.findByContentEntryUidAsync(entityUid)
        } ?: ContentEntryPicture()

        entry.block = if(isBlockRequired){
                entry.block ?: CourseBlock().apply {
                    cbUid = db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                    cbClazzUid = clazzUid
                    cbType = CourseBlock.BLOCK_CONTENT_TYPE
                }
        }else {
            null
        }


        handleBlock(entry,isBlockRequired)


        return entry
    }

    override fun onLoadFromJson(bundle: Map<String, String>): ContentEntryWithBlockAndLanguage {
        super.onLoadFromJson(bundle)
        val entityJsonStr = bundle[ARG_ENTITY_JSON]
        val metaDataStr = bundle[ARG_IMPORTED_METADATA]
        val isBlockRequired = arguments[BLOCK_REQUIRED].toBoolean()
        val clazzUid = arguments[ARG_CLAZZUID]?.toLongOrNull() ?: 0L

        if (metaDataStr != null) {
            view.metadataResult = safeParse(di, MetadataResult.serializer(), metaDataStr)
        }
        val editEntity: ContentEntryWithBlockAndLanguage = if (entityJsonStr != null) {
            safeParse(di, ContentEntryWithBlockAndLanguage.serializer(), entityJsonStr)
        } else {
            ContentEntryWithBlockAndLanguage().apply {
                block = if(isBlockRequired){
                    CourseBlock().apply {
                        cbUid = db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                        cbClazzUid = clazzUid
                        cbType = CourseBlock.BLOCK_CONTENT_TYPE
                    }
                }else{
                    null
                }
            }
        }

        presenterScope.launch(doorMainDispatcher()) {
            handleBlock(editEntity,isBlockRequired)
        }

        view.showUpdateContentButton = editEntity.contentEntryUid != 0L && editEntity.leaf
        view.contentEntryPicture = bundle[SAVED_STATE_CONTENTENTRY_PICTURE]?.let {
            json.decodeFromString(ContentEntryPicture.serializer(), it)
        }

        return editEntity
    }

    override fun onLoadDataComplete() {
        super.onLoadDataComplete()

        observeSavedStateResult(SAVED_STATE_KEY_METADATA, ListSerializer(MetadataResult.serializer()),
                MetadataResult::class) {
            val metadata = it.firstOrNull() ?: return@observeSavedStateResult
            view.loading = true
            presenterScope.launch(doorMainDispatcher()) {
                val entry = handleMetadataResult(metadata)
                // back from navigate import
                entry.contentEntryUid = arguments[ARG_ENTITY_UID]?.toLong() ?: 0
                view.entity = entry
            }
            requireSavedStateHandle()[SAVED_STATE_KEY_METADATA] = null


            view.fileImportErrorVisible = false
            view.loading = false
        }

        observeSavedStateResult(
            SAVEDSTATE_KEY_LANGUAGE, ListSerializer(Language.serializer()),
            Language::class) {
            val language = it.firstOrNull() ?: return@observeSavedStateResult
            entity?.language = language
            entity?.primaryLanguageUid = language.langUid
            view.entity = entity
            requireSavedStateHandle()[SAVEDSTATE_KEY_LANGUAGE] = null
        }


    }

    override fun onSaveInstanceState(savedState: MutableMap<String, String>) {
        super.onSaveInstanceState(savedState)
        savedState.putEntityAsJson(ARG_IMPORTED_METADATA, MetadataResult.serializer(),
            view.metadataResult)
        savedState.putEntityAsJson(SAVED_STATE_CONTENTENTRY_PICTURE,
            ContentEntryPicture.serializer(), view.contentEntryPicture)
    }

    fun loadEntityIntoDateTime(entry: ContentEntryWithBlockAndLanguage){
        val timeZone = view.timeZone ?: "UTC"

        val entity = entry.block ?: return

        if(entity.cbHideUntilDate != 0L){
            val startDateTimeMidnight = DateTime(entity.cbHideUntilDate)
                .toLocalMidnight(timeZone).unixMillisLong
            view.startDate = startDateTimeMidnight
            view.startTime = entity.cbHideUntilDate - startDateTimeMidnight
        }else{
            view.startDate = 0
        }


        if(entity.cbDeadlineDate != Long.MAX_VALUE){
            val deadlineDateTimeMidnight = DateTime(entity.cbDeadlineDate)
                .toLocalMidnight(timeZone).unixMillisLong
            view.deadlineDate = deadlineDateTimeMidnight
            view.deadlineTime = entity.cbDeadlineDate - deadlineDateTimeMidnight
        }else{
            view.deadlineDate = Long.MAX_VALUE
        }

        if(entity.cbGracePeriodDate != Long.MAX_VALUE){
            val gracePeriodDateTimeMidnight = DateTime(entity.cbGracePeriodDate)
                .toLocalMidnight(timeZone).unixMillisLong
            view.gracePeriodDate = gracePeriodDateTimeMidnight
            view.gracePeriodTime = entity.cbGracePeriodDate - gracePeriodDateTimeMidnight
        }else{
            view.gracePeriodDate = Long.MAX_VALUE
        }
    }

    fun saveDateTimeIntoEntity(entry: ContentEntryWithBlockAndLanguage){
        val timeZone = view.timeZone ?: "UTC"

        val entity = entry.block ?: return

        entity.cbHideUntilDate = DateTime(view.startDate).toOffsetByTimezone(timeZone)
            .localMidnight.utc.unixMillisLong + view.startTime

        if(view.deadlineDate != Long.MAX_VALUE){
            entity.cbDeadlineDate = DateTime(view.deadlineDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.deadlineTime
        }

        if(view.gracePeriodDate != Long.MAX_VALUE){
            entity.cbGracePeriodDate = DateTime(view.gracePeriodDate).toOffsetByTimezone(timeZone)
                .localMidnight.utc.unixMillisLong + view.gracePeriodTime
        }
    }

    private suspend fun handleBlock(entry: ContentEntryWithBlockAndLanguage, isBlockRequired: Boolean = false){
        if(isBlockRequired){
            val clazzUid = entry.block?.cbClazzUid ?: return
            val clazzWithSchool = db.onRepoWithFallbackToDb(2000) {
                it.clazzDao.getClazzWithSchool(clazzUid)
            } ?: ClazzWithSchool()

            val timeZone = clazzWithSchool.effectiveTimeZone()
            view.timeZone = timeZone
            loadEntityIntoDateTime(entry)
        }
    }



    private suspend fun handleMetadataResult(metadataResult: MetadataResult, blockRequired: Boolean = false): ContentEntryWithBlockAndLanguage {
        val entityVal = view.entity ?: ContentEntryWithBlockAndLanguage().apply {
            block = if(blockRequired){
                CourseBlock().apply {
                    cbClazzUid = arguments[ARG_CLAZZUID]?.toLongOrNull() ?: 0L
                    cbType = CourseBlock.BLOCK_CONTENT_TYPE
                    cbUid = db.doorPrimaryKeyManager.nextId(CourseBlock.TABLE_ID)
                }
            }else{
                null
            }
        }
        view.metadataResult = metadataResult

        val plugin = pluginManager.getPluginById(metadataResult.pluginId)
        val uri = metadataResult.entry.sourceUrl ?: ""
        val isRemote = DoorUri.parse(uri).isRemote()

        // show video preview
        if (!isRemote && plugin.supportedMimeTypes.firstOrNull()?.startsWith("video/") == true
            && !uri.lowercase().startsWith("https://drive.google.com")) {
            view.videoUri = uri
        }

        entityVal.title = metadataResult.entry.title
        entityVal.description = metadataResult.entry.description
        entityVal.sourceUrl = metadataResult.entry.sourceUrl
        entityVal.entryId = metadataResult.entry.entryId
        entityVal.author = metadataResult.entry.author
        entityVal.contentTypeFlag = metadataResult.entry.contentTypeFlag
        entityVal.publisher = metadataResult.entry.publisher
        entityVal.languageVariantUid = metadataResult.entry.languageVariantUid
        entityVal.primaryLanguageUid = metadataResult.entry.primaryLanguageUid
        entityVal.contentFlags = metadataResult.entry.contentFlags
        entityVal.leaf = metadataResult.entry.leaf

        return entityVal
    }

    override fun handleClickSave(entity: ContentEntryWithBlockAndLanguage) {
        view.loading = true
        view.fieldsEnabled = false
        view.titleErrorEnabled = false
        view.fileImportErrorVisible = false
        presenterScope.launch(doorMainDispatcher()) {
            if (isImportValid(entity)) {

                saveDateTimeIntoEntity(entity)

                val block = entity.block
                if(block != null){
                    var foundError = false
                    if(block.cbMaxPoints == 0){
                        view.caMaxPointsError = systemImpl.getString(MessageID.field_required_prompt, context)
                        foundError = true
                    }else{
                        view.caMaxPointsError = null
                    }

                    if (block.cbDeadlineDate <= block.cbHideUntilDate) {
                        view.caDeadlineError = systemImpl.getString(MessageID.end_is_before_start_error, context)
                        foundError = true
                    }else{
                        view.caDeadlineError = null
                    }

                    if (block.cbGracePeriodDate < block.cbDeadlineDate) {
                        view.caGracePeriodError = systemImpl.getString(MessageID.after_deadline_date_error, context)
                        foundError = true
                    }else{
                        view.caGracePeriodError = null
                    }

                    if(foundError){
                        return@launch
                    }

                }

                entity.licenseName = view.licenceOptions?.firstOrNull {
                    it.code == entity.licenseType
                }.toString()

                val isNewEntry = entity.contentEntryUid == 0L


                repo.withDoorTransactionAsync(UmAppDatabase::class) { txDb ->

                    if (entity.contentEntryUid == 0L) {
                        entity.contentOwner = accountManager.activeAccount.personUid
                        entity.contentEntryUid = txDb.contentEntryDao.insertAsync(entity)

                        if (entity.entryId == null) {
                            entity.entryId = accountManager.activeAccount.endpointUrl +
                                    "${entity.contentEntryUid}/${randomUuid()}"
                            txDb.contentEntryDao.updateAsync(entity)
                        }

                        if(parentEntryUid != 0L) {
                            val contentEntryJoin = ContentEntryParentChildJoin().apply {
                                cepcjChildContentEntryUid = entity.contentEntryUid
                                cepcjParentContentEntryUid = parentEntryUid
                            }
                            txDb.contentEntryParentChildJoinDao.insertAsync(contentEntryJoin)
                        }
                    } else {
                        txDb.contentEntryDao.updateAsync(entity)
                    }

                    UmPlatformUtil.runIfNotJsAsync {
                        val contentEntryPictureVal = view.contentEntryPicture
                        if(contentEntryPictureVal != null) {
                            contentEntryPictureVal.cepContentEntryUid = entity.contentEntryUid

                            if(contentEntryPictureVal.cepUid == 0L) {
                                txDb.contentEntryPictureDao.insertAsync(contentEntryPictureVal)
                            }else {
                                txDb.contentEntryPictureDao.updateAsync(contentEntryPictureVal)
                            }
                        }
                    }


                    val language = entity.language
                    if (language != null && language.langUid == 0L) {
                        txDb.languageDao.insertAsync(language)
                    }

                }

                val metaData = view.metadataResult
                val videoDimensions = view.videoDimensions
                val conversionParams = mapOf("compress" to view.compressionEnabled.toString(),
                        "dimensions" to "${videoDimensions.first}x${videoDimensions.second}")

                if (metaData != null) {

                    if (entity.sourceUrl?.let { DoorUri.parse(it) }?.isRemote() == false) {

                        val job = ContentJob().apply {
                            toUri = view.storageOptions?.get(view.selectedStorageIndex)?.dirUri
                            params = json.encodeStringMapToString(conversionParams)
                            cjIsMeteredAllowed = false
                            cjNotificationTitle = systemImpl.getString(MessageID.importing, context)
                                    .replace("%1\$s",entity.title ?: "")
                            cjUid = db.contentJobDao.insertAsync(this)
                        }
                        ContentJobItem().apply {
                            cjiJobUid = job.cjUid
                            sourceUri = entity.sourceUrl
                            cjiItemTotal = sourceUri?.let { DoorUri.parse(it).getSize(context, di)  } ?: 0L
                            cjiPluginId = metaData.pluginId
                            cjiContentEntryUid = entity.contentEntryUid
                            cjiIsLeaf = entity.leaf
                            cjiParentContentEntryUid = parentEntryUid
                            cjiConnectivityNeeded = false
                            cjiStatus = JobStatus.QUEUED
                            cjiContentDeletedOnCancellation = isNewEntry
                            cjiUid = db.contentJobItemDao.insertJobItem(this)
                        }

                        contentJobManager.enqueueContentJob(accountManager.activeEndpoint, job.cjUid)

                        view.loading = false
                        view.fieldsEnabled = true


                        finishWithResult(safeStringify(di,
                            ListSerializer(ContentEntryWithBlockAndLanguage.serializer()),
                            listOf(entity))
                        )

                        return@launch

                    } else {
                        try {
                            httpClient.post<HttpStatement>() {
                                url(UMFileUtil.joinPaths(accountManager.activeAccount.endpointUrl,
                                        "/import/downloadLink"))
                                parameter("parentUid", parentEntryUid)
                                parameter("pluginId", view.metadataResult?.pluginId)
                                parameter("url", entity.sourceUrl)
                                parameter(HTTP_PARAM_CONVERSION_PARAMS,
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
                            view.loading = false
                            view.fieldsEnabled = true
                            return@launch
                        }

                        view.loading = false
                        view.fieldsEnabled = true


                        finishWithResult(safeStringify(di,
                            ListSerializer(ContentEntryWithBlockAndLanguage.serializer()),
                            listOf(entity))
                        )


                        return@launch

                    }
                } else {
                    // its a folder, check if there is any selected items from previous screen
                    if (arguments.containsKey(KEY_SELECTED_ITEMS)) {
                        val selectedItems = arguments[KEY_SELECTED_ITEMS]?.split(",")
                            ?.map { it.trim().toLong() } ?: listOf()
                        repo.contentEntryParentChildJoinDao.moveListOfEntriesToNewParent(
                            entity.contentEntryUid, selectedItems, systemTimeInMillis())
                    }
                }

                view.loading = false
                view.fieldsEnabled = true


                finishWithResult(safeStringify(di,
                    ListSerializer(ContentEntryWithBlockAndLanguage.serializer()),
                    listOf(entity))
                )

            } else {
                view.titleErrorEnabled = entity.title == null
                view.fileImportErrorVisible = entity.title != null && entity.leaf
                        && view.metadataResult?.entry?.sourceUrl == null
                view.loading = false
                view.fieldsEnabled = true
            }
        }
    }

    fun isImportValid(entity: ContentEntryWithBlockAndLanguage): Boolean {
        return entity.title != null && ((!entity.leaf || entity.contentEntryUid != 0L) ||
                (entity.contentEntryUid == 0L && view.metadataResult != null))
    }

    override fun onClickNewFolder() {
        // wont happen in edit screen
    }

    override fun onClickImportFile() {
        val args = mutableMapOf(
                SelectFileView.ARG_MIMETYPE_SELECTED to
                        pluginManager.supportedMimeTypeList.joinToString(";"),
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
        args.putFromOtherMapIfPresent(arguments, BLOCK_REQUIRED)
        args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

        navigateForResult(
                NavigateForResultOptions(this,
                        null, SelectExtractFileView.VIEW_NAME, MetadataResult::class,
                        MetadataResult.serializer(), SAVED_STATE_KEY_METADATA,
                        arguments = args)
        )
    }

    override fun onClickImportLink() {
        val args = mutableMapOf(ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
        args.putFromOtherMapIfPresent(arguments, BLOCK_REQUIRED)
        args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

        navigateForResult(
                NavigateForResultOptions(
                    this,
                    null,
                    ContentEntryImportLinkView.VIEW_NAME,
                    MetadataResult::class,
                    MetadataResult.serializer(), SAVED_STATE_KEY_METADATA,
                    arguments = args
                )
        )
    }

    override fun onClickImportGallery() {
        val args = mutableMapOf(
                SelectFileView.ARG_MIMETYPE_SELECTED to SelectFileView.SELECTION_MODE_GALLERY,
                ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)
        args.putFromOtherMapIfPresent(arguments, BLOCK_REQUIRED)
        args.putFromOtherMapIfPresent(arguments, ARG_CLAZZUID)

        navigateForResult(
                NavigateForResultOptions(this,
                        null,
                    SelectExtractFileView.VIEW_NAME,
                    MetadataResult::class,
                    MetadataResult.serializer(), SAVED_STATE_KEY_METADATA,
                        arguments = args)
        )
    }

    fun handleClickLanguage(){
        navigateForResult(
            NavigateForResultOptions(this,
                null,
                LanguageListView.VIEW_NAME, Language::class,
                Language.serializer(),
                SAVEDSTATE_KEY_LANGUAGE))
    }

    override fun onClickAddFolder() {
        val args = mutableMapOf(ARG_LEAF to true.toString())
        args.putFromOtherMapIfPresent(arguments, ARG_PARENT_ENTRY_UID)


        navigateForResult(
                NavigateForResultOptions(this,
                        null, SelectFolderView.VIEW_NAME, String::class,
                        String.serializer(), SAVED_STATE_KEY_URI,
                        arguments = args)
        )
    }

    companion object {

        const val SAVED_STATE_KEY_URI = "URI"

        const val SAVEDSTATE_KEY_LANGUAGE = "Language"

        const val SAVED_STATE_KEY_METADATA = "importedMetadata"

        const val SAVED_STATE_CONTENTENTRY_PICTURE = "contentEntryPicture"

        const val HTTP_PARAM_CONVERSION_PARAMS = "conversionParams"


    }

}