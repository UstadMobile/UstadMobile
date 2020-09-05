package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.*
import com.google.gson.Gson
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.SyncResult
import com.ustadmobile.door.UpdateNotificationManager
import com.ustadmobile.door.annotation.EntityWithAttachment
import com.ustadmobile.door.annotation.PgOnConflict
import com.ustadmobile.door.annotation.SyncableEntity
import com.ustadmobile.door.entities.UpdateNotification
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.DI_INSTANCE_MEMBER
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.DI_ON_MEMBER
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.InputProvider
import io.ktor.content.TextContent
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import org.kodein.type.TypeToken
import java.io.File
import java.io.FileInputStream
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import kotlin.reflect.KClass

/**
 * Generate a Tracker Entity for a Syncable Entity
 */
internal fun generateTrackerEntity(entityClass: TypeElement, processingEnv: ProcessingEnvironment) : TypeSpec {
    val pkFieldTypeName = getEntityPrimaryKey(entityClass)!!.asType().asTypeName()
    return TypeSpec.classBuilder("${entityClass.simpleName}_trk")
            .addProperties(listOf(
                    PropertySpec.builder(DbProcessorSync.TRACKER_PK_FIELDNAME, LONG)
                            .addAnnotation(AnnotationSpec.builder(PrimaryKey::class).addMember("autoGenerate = true").build())
                            .initializer(DbProcessorSync.TRACKER_PK_FIELDNAME)
                            .build(),
                    PropertySpec.builder(DbProcessorSync.TRACKER_ENTITY_PK_FIELDNAME, pkFieldTypeName)
                            .initializer(DbProcessorSync.TRACKER_ENTITY_PK_FIELDNAME)
                            .build(),
                    PropertySpec.builder(DbProcessorSync.TRACKER_DESTID_FIELDNAME, INT)
                            .initializer(DbProcessorSync.TRACKER_DESTID_FIELDNAME)
                            .build(),
                    PropertySpec.builder(DbProcessorSync.TRACKER_CHANGESEQNUM_FIELDNAME, INT)
                            .initializer(DbProcessorSync.TRACKER_CHANGESEQNUM_FIELDNAME)
                            .build(),
                    PropertySpec.builder(DbProcessorSync.TRACKER_RECEIVED_FIELDNAME, BOOLEAN)
                            .initializer(DbProcessorSync.TRACKER_RECEIVED_FIELDNAME)
                            .build(),
                    PropertySpec.builder(DbProcessorSync.TRACKER_REQUESTID_FIELDNAME, INT)
                            .initializer(DbProcessorSync.TRACKER_REQUESTID_FIELDNAME)
                            .build(),
                    PropertySpec.builder(DbProcessorSync.TRACKER_TIMESTAMP_FIELDNAME, LONG)
                            .initializer(DbProcessorSync.TRACKER_TIMESTAMP_FIELDNAME)
                            .build()
            ))
            .addAnnotation(AnnotationSpec.builder(Entity::class)
                    .addMember("indices = [%T(value = [%S, %S, %S]),%T(value = [%S, %S], unique = true)]",
                            //Index for query speed linking the destid, entity pk, and the change seq num
                            Index::class,
                            DbProcessorSync.TRACKER_DESTID_FIELDNAME,
                            DbProcessorSync.TRACKER_ENTITY_PK_FIELDNAME,
                            DbProcessorSync.TRACKER_CHANGESEQNUM_FIELDNAME,
                            //Unique index to enforce that there should be one tracker per entity pk / destination id combo
                            Index::class,
                            DbProcessorSync.TRACKER_ENTITY_PK_FIELDNAME,
                            DbProcessorSync.TRACKER_DESTID_FIELDNAME)
                    .build())
            .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_PK_FIELDNAME, LONG)
                            .defaultValue("0L").build())
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_ENTITY_PK_FIELDNAME,
                            pkFieldTypeName).defaultValue("0L").build())
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_DESTID_FIELDNAME,
                            INT).defaultValue("0").build())
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_CHANGESEQNUM_FIELDNAME,
                            INT).defaultValue("0").build())
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_RECEIVED_FIELDNAME,
                            BOOLEAN).defaultValue("false").build())
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_REQUESTID_FIELDNAME,
                            INT).defaultValue("0").build())
                    .addParameter(ParameterSpec.builder(DbProcessorSync.TRACKER_TIMESTAMP_FIELDNAME,
                            LONG).defaultValue("0L").build())
                    .build())
            .addModifiers(KModifier.DATA)
            .build()

}

class DbProcessorSync: AbstractDbProcessor() {

    data class OutputDirs(val abstractOutputArg: String?, val implOutputArg: String?,
                          val ktorRouteOutputArg: String?)
    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)

        //For all databases that are being compiled now, find those entities that require tracker entities
        // to be generated. Filter out any for which the entity was already generated.
        dbs.flatMap { entityTypesOnDb(it as TypeElement, processingEnv) }
                .filter { it.getAnnotation(SyncableEntity::class.java) != null
                        && processingEnv.elementUtils
                        .getTypeElement("${it.asClassName().packageName}.${it.simpleName}$TRACKER_SUFFIX") == null}
                .forEach {
                    val trackerFileSpec = FileSpec.builder(it.asClassName().packageName, "${it.simpleName}$TRACKER_SUFFIX")
                            .addType(generateTrackerEntity(it, processingEnv)).build()

                    writeFileSpecToOutputDirs(trackerFileSpec, AnnotationProcessorWrapper.OPTION_JVM_DIRS)
                    writeFileSpecToOutputDirs(trackerFileSpec, AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT,
                            useFilerAsDefault = false)
                }

        for(dbTypeEl in dbs) {
            val (abstractFileSpec, implFileSpec, repoImplSpec) = generateSyncDaoInterfaceAndImpls(dbTypeEl as TypeElement)

            writeFileSpecToOutputDirs(abstractFileSpec, AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            writeFileSpecToOutputDirs(abstractFileSpec, AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT,
                    useFilerAsDefault = false)
            writeFileSpecToOutputDirs(implFileSpec, AnnotationProcessorWrapper.OPTION_JVM_DIRS)

            val syncRepoFileSpec = generateSyncRepository(dbTypeEl)
            writeFileSpecToOutputDirs(syncRepoFileSpec, AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            writeFileSpecToOutputDirs(syncRepoFileSpec, AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT,
                    useFilerAsDefault = false)

            val syncRouteFileSpec = generateSyncKtorRoute(dbTypeEl as TypeElement)
            syncRouteFileSpec.writeAllSpecs(writeImpl = true, writeKtor = true,
                    outputSpecArgName = AnnotationProcessorWrapper.OPTION_KTOR_OUTPUT,
                    useFilerAsDefault = true)
        }

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)
        daos.filter { !it.simpleName.endsWith(SUFFIX_SYNCDAO_ABSTRACT) }.forEach {daoElement ->
            val daoTypeEl = daoElement as TypeElement
            val daoFileSpec = generateDaoSyncHelperInterface(daoTypeEl)
            writeFileSpecToOutputDirs(daoFileSpec, AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            writeFileSpecToOutputDirs(daoFileSpec, AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT,
                    useFilerAsDefault = false)
        }


        return true
    }

    /**
     * Generates an interface that will be used for
     */
    fun generateDaoSyncHelperInterface(daoType: TypeElement): FileSpec {
        val syncableEntitiesOnDao = syncableEntitiesOnDao(daoType.asClassName(), processingEnv)
        val syncHelperInterface = TypeSpec.interfaceBuilder("${daoType.simpleName}_SyncHelper")

        syncableEntitiesOnDao.forEach {
            syncHelperInterface.addFunction(
                    FunSpec.builder("_replace${it.simpleName}")
                            .addParameter("entityList", List::class.asClassName().parameterizedBy(it))
                            .addModifiers(KModifier.ABSTRACT)
                            .build())

            val entitySyncTrackerClassName = ClassName(it.packageName,
                    "${it.simpleName}$TRACKER_SUFFIX")
            syncHelperInterface.addFunction(
                    FunSpec.builder("_replace${entitySyncTrackerClassName.simpleName}")
                            .addParameter("entityTrackerList",
                                    List::class.asClassName().parameterizedBy(entitySyncTrackerClassName))
                            .addModifiers(KModifier.ABSTRACT)
                            .build())

            syncHelperInterface.addFunction(
                    FunSpec.builder("_update${entitySyncTrackerClassName.simpleName}Received")
                            .addParameter("status", BOOLEAN)
                            .addParameter("requestId", INT)
                            .addModifiers(KModifier.ABSTRACT)
                            .build())

        }

        return FileSpec.builder(pkgNameOfElement(daoType, processingEnv),
                "${daoType.simpleName}_SyncHelper")
                .addType(syncHelperInterface.build())
                .build()
    }


    fun generateSyncKtorRoute(dbType: TypeElement): KtorDaoFileSpecs {
        val abstractDaoSimpleName = "${dbType.simpleName}$SUFFIX_SYNCDAO_ABSTRACT"
        val packageName = dbType.asClassName().packageName
        val specs = KtorDaoSpecs(packageName, abstractDaoSimpleName)

        val abstractDaoClassName = ClassName(packageName,
                abstractDaoSimpleName)
        specs.ktorRoute.fileSpec.addImport("io.ktor.response", "header")


        val ktorHelperDaoClassName = ClassName(packageName, "$abstractDaoClassName${DbProcessorKtorServer.SUFFIX_KTOR_HELPER}")
        val daoRouteFn = FunSpec.builder("${dbType.simpleName}$SUFFIX_SYNC_ROUTE")
                .receiver(Route::class)
                .addTypeVariable(TypeVariableName.invoke("T", DoorDatabase::class))
                .addParameter("_typeToken", TypeToken::class.asClassName().parameterizedBy(TypeVariableName("T")))
                .addParameter("_daoFn", LambdaTypeName.get(parameters = *arrayOf(TypeVariableName("T")),
                    returnType = abstractDaoClassName))
                .addParameter("_ktorHelperDaoFn", LambdaTypeName.get(
                        parameters = *arrayOf(TypeVariableName("T")),
                        returnType = ktorHelperDaoClassName))

        val codeBlock = CodeBlock.builder()
        codeBlock.beginControlFlow("%M(%S)",
                MemberName("io.ktor.routing", "route"), abstractDaoSimpleName)

        //Route for clients to subscribe for updates
        codeBlock.beginControlFlow("%M(%S)", DbProcessorKtorServer.GET_MEMBER, "_subscribe")
                .add("val _repo: %T by %M().%M(call).%M(tag = %T.TAG_REPO)\n",
                    dbType, MemberName("org.kodein.di.ktor", "di"),
                    DI_ON_MEMBER, DI_INSTANCE_MEMBER, DoorTag::class)
                .add("call.%M(_repo as %T)\n",
                        MemberName("com.ustadmobile.door.ktor", "respondUpdateNotifications"),
                        DoorDatabaseSyncRepository::class)
                .endControlFlow()

        codeBlock.beginControlFlow("%M(%S)", DbProcessorKtorServer.GET_MEMBER, "_deleteUpdateNotifications")
                .addRequestDi()
                .add(generateGetParamFromRequestCodeBlock(LONG, "timestamp", "_timestamp",
                        serverType = DbProcessorKtorServer.SERVER_TYPE_KTOR))
                .add(generateGetParamFromRequestCodeBlock(INT, "tableId", "_tableId",
                        serverType = DbProcessorKtorServer.SERVER_TYPE_KTOR))
                .addGetClientIdHeader("_clientId", DbProcessorKtorServer.SERVER_TYPE_KTOR)
                .add("val _dao = _daoFn(_db)\n")
                .add("_dao._deleteUpdateNotifications(_clientId, _tableId, _timestamp)\n")
                .add("call.respond(%T.NoContent, %S)\n", HttpStatusCode::class, "")
                .endControlFlow()



        syncableEntityTypesOnDb(dbType, processingEnv).forEach { entityType ->
            val syncableEntityInfo = SyncableEntityInfo(entityType.asClassName(), processingEnv)
            val entityTypeListClassName = List::class.asClassName().parameterizedBy(entityType.asClassName())
            val syncFindAllSql = entityType.getAnnotation(SyncableEntity::class.java)?.syncFindAllQuery
            val getAllSql = if(syncFindAllSql?.isNotEmpty() == true) {
                syncFindAllSql
            }else {
                "SELECT * FROM ${entityType.simpleName}"
            }
            val getAllFunSpec = FunSpec.builder("_findMasterUnsent${entityType.simpleName}")
                    .returns(entityTypeListClassName)
                    .addAnnotation(AnnotationSpec.builder(Query::class)
                            .addMember("%S", getAllSql).build())
                    .build()
            codeBlock.beginControlFlow("%M(%S)", DbProcessorKtorServer.GET_MEMBER,
                    "_findMasterUnsent${entityType.simpleName}")
                    .addRequestDi()
                    .add("val _dao = _daoFn(_db)\n")
                    .add("val _ktorHelperDao = _ktorHelperDaoFn(_db)\n")
                .add(generateKtorRouteSelectCodeBlock(getAllFunSpec, syncHelperDaoVarName = "_dao"))
                .endControlFlow()
            val helperFunSpec = getAllFunSpec.toBuilder()
            helperFunSpec.annotations.clear()
            helperFunSpec.addParameter("clientId", INT)
            specs.addHelperQueryFun(helperFunSpec.build(), getAllSql, entityType.asClassName(), false)

            val replaceEntityFunSpec = FunSpec.builder("_replace${entityType.simpleName}")
                    .addParameter("entities", entityTypeListClassName)
                    .returns(UNIT)
                    .build()

            codeBlock.beginControlFlow("%M(%S)", DbProcessorKtorServer.POST_MEMBER,
                    "_replace${entityType.simpleName}")
                    .addRequestDi()
                    .add("val _dao = _daoFn(_db)\n")
                    .add("val _gson: %T by _di.%M()\n", Gson::class, DI_INSTANCE_MEMBER)
            val hasAttachments = findEntitiesWithAnnotation(entityType.asClassName(), EntityWithAttachment::class.java,
                    processingEnv).isNotEmpty()

            if(hasAttachments) {
                val multipartHelperVarName = "_multipartHelper"
                codeBlock.add("val _attachmentsDir: String by _di.%M(call).%M(tag = %T.TAG_ATTACHMENT_DIR)\n",
                                DI_ON_MEMBER, DI_INSTANCE_MEMBER, DoorTag::class)
                        .add("val $multipartHelperVarName = %T()\n",
                        ClassName("com.ustadmobile.door", "DoorAttachmentsMultipartHelper"))
                        .add("_multipartHelper.digestMultipart(%M.%M())\n",
                                DbProcessorKtorServer.CALL_MEMBER,
                                MemberName("io.ktor.request", "receiveMultipart"))
                val entityParamName = "__" + replaceEntityFunSpec.parameters[0].name
                val pkEl = processingEnv.elementUtils.getTypeElement(
                    entityType.qualifiedName).enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null}
                codeBlock.add(generateHttpServerPassToDaoCodeBlock(replaceEntityFunSpec, multipartHelperVarName,
                        beforeDaoCallCode = CodeBlock.builder()
                                .beginControlFlow("if(_multipartHelper.containsAllAttachments($entityParamName.map{it.${pkEl.simpleName}.toString()}))")
                                .add("_multipartHelper.moveTmpFiles(%T(_attachmentsDir, %S))\n",
                                        File::class, entityType.simpleName)
                                .build(),
                        afterDaoCallCode = CodeBlock.builder()
                                .nextControlFlow("else")
                                .add("%M.%M(%T.BadRequest, \"\")\n", DbProcessorKtorServer.CALL_MEMBER,
                                    DbProcessorKtorServer.RESPOND_MEMBER, HttpStatusCode::class)
                                .endControlFlow()
                                .build()))
            }else {
                codeBlock.add(generateHttpServerPassToDaoCodeBlock(replaceEntityFunSpec))
            }

            codeBlock.endControlFlow()
            if(hasAttachments) {
                codeBlock.add(generateGetAttachmentDataCodeBlock(entityType))
            }
            codeBlock.add(generateEntitiesAckKtorRoute(syncableEntityInfo.tracker,
                    syncableEntityInfo.syncableEntity, syncHelperVarName = "_dao",
                    syncHelperFnName = "_daoFn"))

        }
        codeBlock.endControlFlow()

        daoRouteFn.addCode(codeBlock.build())
        specs.ktorRoute.fileSpec.addFunction(daoRouteFn.build())
        return specs.toBuiltFileSpecs()
    }


    data class SyncFileSpecs(val abstractFileSpec: FileSpec, val daoImplFileSpec: FileSpec, val repoImplFileSpec: FileSpec)


    fun generateSyncRepository(dbType: TypeElement): FileSpec {
        val dbClassName = dbType.asClassName()
        val syncDaoSimpleName = "${dbClassName.simpleName}${SUFFIX_SYNCDAO_ABSTRACT}"
        val syncRepoSimpleName =
                "${syncDaoSimpleName}_${DbProcessorRepository.SUFFIX_REPOSITORY}"
        val repoFileSpec = FileSpec.builder(dbClassName.packageName,
                syncRepoSimpleName)
        val daoClassName = ClassName(dbClassName.packageName,
                "${dbClassName.simpleName}$SUFFIX_SYNCDAO_ABSTRACT")
        val repoTypeSpec = newRepositoryClassBuilder(daoClassName, false,
            extraConstructorParams = listOf(ParameterSpec.builder("_updateNotificationManager",
                UpdateNotificationManager::class.asClassName().copy(nullable = true)).build()))
                .addSuperinterface(DoorDatabaseSyncRepository::class as KClass<*>)
                .addProperty(PropertySpec.builder("auth", String::class)
                        .getter(FunSpec.getterBuilder().addCode("return %S\n", "").build())
                        .addModifiers(KModifier.OVERRIDE)
                        .build())
                .addProperty(PropertySpec.builder("clientId", INT)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder().addCode("return _findSyncNodeClientId()\n")
                        .build()).build())
                .addProperty(PropertySpec.builder("tableIdMap",
                    Map::class.asClassName().parameterizedBy(String::class.asClassName(), INT))
                        .getter(FunSpec.getterBuilder().addCode("return _repo.tableIdMap\n").build())
                        .addModifiers(KModifier.OVERRIDE)
                    .build())
                .addProperty(PropertySpec.builder("dbPath", String::class)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder().addCode("return _dbPath\n").build())
                        .build())
                .addProperty(PropertySpec.builder("endpoint", String::class)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder().addCode("return _endpoint\n").build())
                        .build())
                .addProperty(PropertySpec.builder("httpClient", HttpClient::class)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder().addCode("return _httpClient\n").build())
                        .build())
                .addProperty(PropertySpec.builder("_updateNotificationManager",
                    UpdateNotificationManager::class.asClassName().copy(nullable = true))
                        .initializer("_updateNotificationManager")
                        .build())
                .addRepositoryHelperDelegateCalls("_repo")

        val syncFnCodeBlock = CodeBlock.builder()
                .add("val _allResults = mutableListOf<%T>()\n", SyncResult::class)

        repoTypeSpec.addFunction(FunSpec.builder("_findSyncNodeClientId")
                .addModifiers(KModifier.OVERRIDE)
                .returns(INT)
                .addCode("return _dao._findSyncNodeClientId()\n")
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("_insertSyncResult")
                .addParameter("result", SyncResult::class)
                .addModifiers(KModifier.OVERRIDE)
                .addCode("_dao._insertSyncResult(result)\n")
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("findPendingUpdateNotifications")
                .addParameter("deviceId", INT)
                .returns(List::class.parameterizedBy(UpdateNotification::class))
                .addModifiers(KModifier.OVERRIDE)
                .addModifiers(KModifier.SUSPEND)
                .addCode("return _dao.findPendingUpdateNotifications(deviceId)\n")
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("_deleteUpdateNotifications")
                .addParameter("deviceId", INT)
                .addParameter("tableId", INT)
                .addParameter("timestamp", LONG)
                .addCode("return _dao._deleteUpdateNotifications(deviceId, tableId, timestamp)\n")
                .addModifiers(KModifier.OVERRIDE)
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("dispatchUpdateNotifications")
                .addParameter("tableId", INT)
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("when(tableId)")
                        .apply {
                            syncableEntityTypesOnDb(dbType, processingEnv).forEach {
                                val syncableEntityInfo = SyncableEntityInfo(it.asClassName(), processingEnv)
                                if(syncableEntityInfo.notifyOnUpdate.isNotBlank()) {
                                    add("%L -> _findDevicesToNotify${it.simpleName}()\n", syncableEntityInfo.tableId)
                                }
                            }
                        }
                        .endControlFlow()
                        .build())
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("onPendingChangeLog")
                .addParameter("tableIds", Set::class.asClassName().parameterizedBy(INT))
                .addModifiers(KModifier.OVERRIDE)
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("_replaceUpdateNotifications")
                .addParameter("entities", List::class.parameterizedBy(UpdateNotification::class))
                .addCode(CodeBlock.of("_dao._replaceUpdateNotifications(entities)\n"))
                .addModifiers(KModifier.OVERRIDE)
                .build())

        repoTypeSpec.addFunction(FunSpec.builder("_updateChangeLogDispatched")
                .addParameter("tableId", INT)
                .addParameter("dispatched", BOOLEAN)
                .addModifiers(KModifier.OVERRIDE)
                .build())

        syncableEntityTypesOnDb(dbType, processingEnv).forEach { entityType ->
            val syncableEntityInfo = SyncableEntityInfo(entityType.asClassName(), processingEnv)
            val entityListTypeName = List::class.asClassName().parameterizedBy(entityType.asClassName())
            val entityPkEl = entityType.enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null }

            val replaceEntitiesFn = FunSpec.builder("_replace${entityType.simpleName}")
                    .addParameter("_entities", List::class.asClassName().parameterizedBy(entityType.asClassName()))
                    .addModifiers(KModifier.OVERRIDE)
                    .addCode("_dao._replace${entityType.simpleName}(_entities)\n")
                    .build()
            repoTypeSpec.addFunction(replaceEntitiesFn)


            repoTypeSpec.addFunction(FunSpec.builder("_replace${syncableEntityInfo.tracker.simpleName}")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("entities",
                            List::class.asClassName().parameterizedBy(syncableEntityInfo.tracker))
                    .addCode("_dao._replace${syncableEntityInfo.tracker.simpleName}(entities)\n")
                    .build())

            repoTypeSpec.addFunction(FunSpec.builder("_findLocalUnsent${entityType.simpleName}")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("destClientId", INT)
                    .addParameter("limit", INT)
                    .addCode("return _dao._findLocalUnsent${entityType.simpleName}(destClientId, limit)\n")
                    .returns(List::class.asClassName().parameterizedBy(syncableEntityInfo.syncableEntity))
                    .build())

            repoTypeSpec.addFunction(FunSpec.builder("_update${syncableEntityInfo.tracker.simpleName}Received")
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter("status", BOOLEAN)
                    .addParameter("requestId", INT)
                    .addCode("_dao._update${syncableEntityInfo.tracker.simpleName}Received(status, requestId)\n")
                    .build())


            val findMasterUnsentFnSpec = FunSpec.builder("_findMasterUnsent${entityType.simpleName}")
                    .returns(entityListTypeName)
                    .addModifiers(KModifier.SUSPEND)
                    .addAnnotation(AnnotationSpec.builder(Query::class)
                            .addMember(CodeBlock.of("%S", "SELECT * FROM ${entityType.simpleName}")).build())

            val entitySyncCodeBlock = CodeBlock.builder()
                    .add(generateRepositoryGetSyncableEntitiesFun(findMasterUnsentFnSpec.build(),
                            syncDaoSimpleName, syncHelperDaoVarName = "_dao", addReturnDaoResult = false))
                    .add("_loadHelper.doRequest()\n")

            val findDevicesSql = syncableEntityInfo.notifyOnUpdate
            if(findDevicesSql != "") {
                repoTypeSpec.addFunction(FunSpec.builder("_findDevicesToNotify${entityType.simpleName}")
                        .returns(List::class.asClassName().parameterizedBy(INT))
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode(CodeBlock.builder()
                                .add("val _devicesToNotify = _dao._findDevicesToNotify${entityType.simpleName}()\n")
                                .add("val _timeNow = %M()\n",
                                        MemberName("com.ustadmobile.door.util", "systemTimeInMillis"))
                                .add("val _updateNotifications = _devicesToNotify.map·{·%T(pnDeviceId·=·it,·pnTableId·=·${syncableEntityInfo.tableId},·pnTimestamp·=·_timeNow)·}\n",
                                    UpdateNotification::class)
                                .add("_replaceUpdateNotifications(_updateNotifications)\n")
                                .add("_updateNotificationManager?.onNewUpdateNotifications(_updateNotifications)\n")
                                .add("_dao._updateChangeLogDispatched(${syncableEntityInfo.tableId}, true)\n")
                                .add("return _devicesToNotify\n")
                                .build())
                        .build())
            }

            val hasAttachments = entityType.getAnnotation(EntityWithAttachment::class.java) != null

            entitySyncCodeBlock.add("val _entities = _findLocalUnsent${entityType.simpleName}(0, 100)\n")
                    .beginControlFlow("if(!_entities.isEmpty())")
            var multipartPartsVarName: String? = null
            if(hasAttachments) {
                entitySyncCodeBlock.add("val _entityAttachmentsDir = %T(_attachmentsDir, %S)\n",
                        File::class, entityType.simpleName)
                val pkFieldName = entityPkEl.simpleName
                entitySyncCodeBlock
                        .add("val _multipartJsonStr = (%M().write(_entities) as %T).text\n",
                                MemberName("io.ktor.client.features.json", "defaultSerializer"),
                                TextContent::class)
                        .beginControlFlow("val·_multipartParts = %M",
                                MemberName("io.ktor.client.request.forms", "formData"))
                        .add("append(%S, _multipartJsonStr)\n", "entities")
                        .beginControlFlow("_entities.forEach")
                        .add("val _pkStr = it.$pkFieldName.toString()\n")
                        .add("val _attachFile = %T(_entityAttachmentsDir, _pkStr)\n",
                                File::class)
                        .beginControlFlow("val _mpHeaders = %T.build", Headers::class)
                        .add("append(%T.ContentLength, _attachFile.length())\n", HttpHeaders::class)
                        .add("append(%T.ContentDisposition,·\"form-data;·name=\\\"\$_pkStr\\\";·filename=\\\"\$_pkStr\\\"\")\n",
                                HttpHeaders::class)
                        .add("%M(_db)\n",
                                MemberName("com.ustadmobile.door.ext", "appendDbVersionHeader"))

                        .endControlFlow()
                        .add("append(_attachFile.name,·%T(_attachFile.length()){%T(_attachFile).%M()}, _mpHeaders)\n",
                                InputProvider::class, FileInputStream::class,
                                MemberName("io.ktor.utils.io.streams", "asInput"))

                        .endControlFlow()
                        .endControlFlow()
                        .add("\n")
                multipartPartsVarName = "_multipartParts"
            }


            entitySyncCodeBlock.add(generateKtorRequestCodeBlockForMethod(httpEndpointVarName = "_endpoint",
                            dbPathVarName = "_dbPath",
                            daoName = syncDaoSimpleName, methodName = replaceEntitiesFn.name,
                            httpResultVarName = "_sendResult", httpStatementVarName = "_sendHttpResponse",
                            httpResultType = UNIT, params = replaceEntitiesFn.parameters,
                            useMultipartPartsVarName = multipartPartsVarName))
                    .add(generateReplaceSyncableEntitiesTrackerCodeBlock("_entities",
                            entityListTypeName, syncHelperDaoVarName = "_dao", clientIdVarName = "0",
                            reqIdVarName = "0", processingEnv = processingEnv, isPrimaryDb = false))
                    .endControlFlow()

            entitySyncCodeBlock.add("""return %T(tableId = ${syncableEntityInfo.tableId},
                |status = %T.STATUS_SUCCESS, timestamp = %M())
                """.trimMargin(), SyncResult::class, SyncResult::class, SYSTEMTIME_MEMBER_NAME)

            entitySyncCodeBlock.add("\n")

            val entitySyncFn = FunSpec.builder("sync${entityType.simpleName}")
                    .addModifiers(KModifier.SUSPEND, KModifier.PRIVATE)
                    .returns(SyncResult::class)
                    .addCode(entitySyncCodeBlock.build())

            syncFnCodeBlock.beginControlFlow("if(tablesToSync == null || ${syncableEntityInfo.tableId} in tablesToSync)",
                            entityType)
                    .beginControlFlow("try")
                    .add("val _syncResult = sync${entityType.simpleName}()\n")
                    .add("_insertSyncResult(_syncResult)\n")
                    .nextControlFlow("catch(e: %T)", Exception::class)
                    .add("""_insertSyncResult(%T(tableId = ${syncableEntityInfo.tableId}, 
                        |status = %T.STATUS_FAILED, timestamp = %M()))
                        |""".trimMargin(),
                            SyncResult::class, SyncResult::class, SYSTEMTIME_MEMBER_NAME)
                    .endControlFlow()
                    .endControlFlow()
            repoTypeSpec.addFunction(entitySyncFn.build())
        }


        syncFnCodeBlock.beginControlFlow(
                "val _syncRunStatus = if(_allResults.all { it.status == %T.STATUS_SUCCESS })",
                SyncResult::class)
                .add("%T.STATUS_SUCCESS\n", SyncResult::class)
                .nextControlFlow("else")
                .add("%T.STATUS_FAILED\n", SyncResult::class)
                .endControlFlow()
                .add("_insertSyncResult(%T(tableId = 0, status = _syncRunStatus, timestamp = %M()))\n",
                        SyncResult::class, SYSTEMTIME_MEMBER_NAME)

        repoTypeSpec.addFunction(FunSpec.builder("sync")
                .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                .addParameter("tablesToSync", List::class.parameterizedBy(Int::class).copy(nullable = true))
                .addCode(syncFnCodeBlock.build())
                .build())

        return repoFileSpec.addType(repoTypeSpec.build()).build()
    }

    /**
     *
     * @return Pair of FileSpecs: first = the abstract DAO filespec, the second one is the implementation
     */
    fun generateSyncDaoInterfaceAndImpls(dbType: TypeElement): SyncFileSpecs {
        val abstractDaoSimpleName = "${dbType.simpleName}$SUFFIX_SYNCDAO_ABSTRACT"
        val abstractDaoClassName = ClassName(pkgNameOfElement(dbType, processingEnv),
                abstractDaoSimpleName)
        val abstractDaoTypeSpec = TypeSpec.classBuilder(abstractDaoSimpleName)
                .addAnnotation(Dao::class.asClassName())
                .addModifiers(KModifier.ABSTRACT)
                .addSuperinterface(ClassName(pkgNameOfElement(dbType, processingEnv), "I$abstractDaoSimpleName"))

        daosOnDb(dbType.asClassName(), processingEnv, excludeDbSyncDao = true)
                .filter { syncableEntitiesOnDao(it, processingEnv).isNotEmpty()}
                .forEach {
                    abstractDaoTypeSpec.addSuperinterface(
                            ClassName(it.packageName, "${it.simpleName}_SyncHelper"))
                }

        val abstractDaoInterfaceTypeSpec = TypeSpec.interfaceBuilder("I$abstractDaoSimpleName")

        val abstractFileSpec = FileSpec.builder(pkgNameOfElement(dbType, processingEnv),
                abstractDaoSimpleName)
                .addImport("com.ustadmobile.door", "DoorDbType")


        val implDaoSimpleName = "${dbType.simpleName}$SUFFIX_SYNCDAO_IMPL"
        val implDaoClassName = ClassName(pkgNameOfElement(dbType, processingEnv),
                implDaoSimpleName)
        val implDaoTypeSpec = jdbcDaoTypeSpecBuilder(implDaoSimpleName, abstractDaoClassName)
        val implFileSpec = FileSpec.builder(pkgNameOfElement(dbType, processingEnv),
                implDaoSimpleName)
                .addImport("com.ustadmobile.door", "DoorDbType")

        //TODO: this is no longer made here - remove it from this fn
        val repoImplSimpleName = "${dbType.simpleName}${DbProcessorRepository.SUFFIX_REPOSITORY}"
        val repoImplSpec = FileSpec.builder(pkgNameOfElement(dbType, processingEnv),
                repoImplSimpleName)

        val (abstractDaoFindSyncClientIdFn, implFindSyncClientIdFn) = generateAbstractAndImplQueryFunSpecs(
                "SELECT nodeClientId FROM SyncNode", "_findSyncNodeClientId",
                INT, listOf(), addReturnStmt = true)
        abstractDaoTypeSpec.addFunction(abstractDaoFindSyncClientIdFn)
        implDaoTypeSpec.addFunction(implFindSyncClientIdFn)

        val syncResultTypeSpec = processingEnv.elementUtils.getTypeElement(
                SyncResult::class.qualifiedName) as TypeElement
        val (abstractInsertSyncResultFn, implInsertSyncResultFn, abstractInterfaceInsertSyncResultFn)
                = generateAbstractAndImplUpsertFuns("_insertSyncResult",
                ParameterSpec.builder("result", SyncResult::class).build(),
                syncResultTypeSpec.asEntityTypeSpec(), implDaoTypeSpec, abstractFunIsOverride = true)
        abstractDaoTypeSpec.addFunction(abstractInsertSyncResultFn)
        abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceInsertSyncResultFn)
        implDaoTypeSpec.addFunction(implInsertSyncResultFn)


        //Find pending UpdateNotification query
        val (abstractFindUpdateFn, implFindUpdateFN, abstractInterfaceFindUpdateFn) =
                generateAbstractAndImplQueryFunSpecs("SELECT * FROM UpdateNotification WHERE pnDeviceId = :deviceId",
                        "findPendingUpdateNotifications",
                        List::class.parameterizedBy(UpdateNotification::class),
                        listOf(ParameterSpec.builder("deviceId", INT).build()),
                        addReturnStmt = true, abstractFunIsOverride = true, suspended = true)
        abstractDaoTypeSpec.addFunction(abstractFindUpdateFn)
        implDaoTypeSpec.addFunction(implFindUpdateFN)
        abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceFindUpdateFn)

        val (abstractDeleteUpdateNotification, implDeleteUpdateNotification, abstractInterfaceDeleteUpdateNotification) =
                generateAbstractAndImplQueryFunSpecs("DELETE FROM UpdateNotification WHERE " +
                        "pnDeviceId = :deviceId AND pnTableId = :tableId AND pnTimestamp = :timestamp",
                        "_deleteUpdateNotifications", UNIT,
                        listOf(ParameterSpec("deviceId", INT),
                                ParameterSpec("tableId", INT),
                                ParameterSpec("timestamp", LONG)),
                        addReturnStmt = false, abstractFunIsOverride = true)
        abstractDaoTypeSpec.addFunction(abstractDeleteUpdateNotification)
        implDaoTypeSpec.addFunction(implDeleteUpdateNotification)
        abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceDeleteUpdateNotification)



        val updateNotificationClassName = UpdateNotification::class.asClassName()
        val updateNotificationTypeEl = processingEnv.elementUtils.getTypeElement(
            "${updateNotificationClassName.packageName}.${updateNotificationClassName.simpleName}")

        //Generate _replaceUpdateNotifications - used by dispatchPushNotifications to create / update UpdateNotifications
        val (abstractReplaceUpdateNotification, implReplaceUpdateNotification, abstractDaoReplaceUpdateNotification) =
                generateAbstractAndImplUpsertFuns(
                        "_replaceUpdateNotifications",
                        ParameterSpec.builder("entities", List::class.parameterizedBy(UpdateNotification::class)).build(),
                        updateNotificationTypeEl.asEntityTypeSpec(),
                        implDaoTypeSpec, abstractFunIsOverride = true,
                        pgOnConflict = "ON CONFLICT (pnDeviceId, pnTableId) DO UPDATE SET pnTimestamp = excluded.pnTimestamp")
        abstractDaoTypeSpec.addFunction(abstractReplaceUpdateNotification)
        implDaoTypeSpec.addFunction(implReplaceUpdateNotification)
        abstractDaoInterfaceTypeSpec.addFunction(abstractDaoReplaceUpdateNotification)

        val (abstractUpdateChangeLogDispatched, implUpdateChangeLogDispatched, abstractDaoUpdateChangeLogDispatched) =
                generateAbstractAndImplQueryFunSpecs(
                        """UPDATE ChangeLog SET dispatched = :dispatched 
                        |WHERE 
                        |chTableId = :tableId
                        |AND chTime < (SELECT max(pnTimestamp) FROM UpdateNotification WHERE pnTableId = :tableId)
                        |""".trimMargin(),
                        "_updateChangeLogDispatched", UNIT,
                        listOf(ParameterSpec("tableId", INT),
                                ParameterSpec("dispatched", BOOLEAN)),
                        addReturnStmt = false,abstractFunIsOverride = true)
        abstractDaoTypeSpec.addFunction(abstractUpdateChangeLogDispatched)
        implDaoTypeSpec.addFunction(implUpdateChangeLogDispatched)
        abstractDaoInterfaceTypeSpec.addFunction(abstractDaoUpdateChangeLogDispatched)

        syncableEntityTypesOnDb(dbType, processingEnv).forEach {entityType ->
            val syncableEntityInfo = SyncableEntityInfo(entityType.asClassName(), processingEnv)
            val entityListClassName = List::class.asClassName().parameterizedBy(entityType.asClassName())
            val entitySyncTrackerListClassName = List::class.asClassName().parameterizedBy(syncableEntityInfo.tracker)

            //Generate the find local unsent changes function for this entity
            val findLocalUnsentSql = "SELECT * FROM " +
                    "(SELECT * FROM ${entityType.simpleName} ) AS ${entityType.simpleName} " +
                    "WHERE " +
                    "${syncableEntityInfo.entityLastChangedByField.name} = (SELECT nodeClientId FROM SyncNode) AND " +
                    "(${entityType.simpleName}.${syncableEntityInfo.entityLocalCsnField.name} > " +
                    "COALESCE((SELECT ${syncableEntityInfo.trackerCsnField.name} FROM ${syncableEntityInfo.tracker.simpleName} " +
                    "WHERE ${syncableEntityInfo.trackerPkField.name} = ${entityType.simpleName}.${syncableEntityInfo.entityPkField.name} " +
                    "AND ${syncableEntityInfo.trackerDestField.name} = :destClientId), 0)" +
                    ") LIMIT :limit"


            val findUnsentParamsList = listOf(ParameterSpec.builder("destClientId", INT).build(),
                    ParameterSpec.builder("limit", INT).build())
            val (abstractLocalUnsentChangeFun, implLocalUnsetChangeFun) =
                    generateAbstractAndImplQueryFunSpecs(findLocalUnsentSql,
                            "_findLocalUnsent${entityType.simpleName}",
                            entityListClassName, findUnsentParamsList)
            abstractDaoTypeSpec.addFunction(abstractLocalUnsentChangeFun)
            implDaoTypeSpec.addFunction(implLocalUnsetChangeFun)

            //generate an upsert function for the entity itself
            val (abstractInsertEntityFun, implInsertEntityFun, abstractInterfaceInsertEntityFun) =
                generateAbstractAndImplUpsertFuns(
                    "_replace${entityType.simpleName}",
                    ParameterSpec.builder("entities", entityListClassName).build(),
                    entityType.asEntityTypeSpec(),
                    implDaoTypeSpec, abstractFunIsOverride = true)
            abstractDaoTypeSpec.addFunction(abstractInsertEntityFun)
            abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceInsertEntityFun)
            implDaoTypeSpec.addFunction(implInsertEntityFun)

            val (abstractInsertTrackerFun, implInsertTrackerFun, abstractInterfaceInsertTrackerFun) =
                    generateAbstractAndImplUpsertFuns(
                    "_replace${syncableEntityInfo.tracker.simpleName}",
                    ParameterSpec.builder("entities", entitySyncTrackerListClassName).build(),
                    generateTrackerEntity(entityType, processingEnv),
                    implDaoTypeSpec, abstractFunIsOverride = true,
                            pgOnConflict = " ON CONFLICT(epk, clientId) DO UPDATE SET csn = excluded.csn")
            abstractDaoTypeSpec.addFunction(abstractInsertTrackerFun)
            implDaoTypeSpec.addFunction(implInsertTrackerFun)
            abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceInsertTrackerFun)

            //generate an update function that can be used to set the status of the sync tracker
            val updateTrackerReceivedSql = "UPDATE ${syncableEntityInfo.tracker.simpleName} SET " +
                    "${syncableEntityInfo.trackerReceivedField.name} = :status WHERE " +
                    "${syncableEntityInfo.trackerReqIdField.name} = :requestId"
            val (abstractUpdateTrackerFun, implUpdateTrackerFun, abstractInterfaceUpdateTrackerFun) =
                    generateAbstractAndImplQueryFunSpecs(updateTrackerReceivedSql,
                            "_update${syncableEntityInfo.tracker.simpleName}Received",
                            UNIT, listOf(ParameterSpec.builder("status", BOOLEAN).build(),
                            ParameterSpec.builder("requestId", INT).build()),
                            addReturnStmt = false, abstractFunIsOverride = true)
            abstractDaoTypeSpec.addFunction(abstractUpdateTrackerFun)
            implDaoTypeSpec.addFunction(implUpdateTrackerFun)
            abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceUpdateTrackerFun)


            val findDevicesSql = syncableEntityInfo.notifyOnUpdate
            if(findDevicesSql != "") {
                val (abstractFindDevicesFun, implFindDevicesFun, abstractInterfaceFindDevicesFun) =
                        generateAbstractAndImplQueryFunSpecs(syncableEntityInfo.notifyOnUpdate,
                                "_findDevicesToNotify${entityType.simpleName}",
                                List::class.asClassName().parameterizedBy(INT), listOf(),
                                abstractFunIsOverride = true)
                abstractDaoTypeSpec.addFunction(abstractFindDevicesFun)
                implDaoTypeSpec.addFunction(implFindDevicesFun)
                abstractDaoInterfaceTypeSpec.addFunction(abstractInterfaceFindDevicesFun)
            }
        }


        abstractFileSpec.addType(abstractDaoInterfaceTypeSpec.build())
        abstractFileSpec.addType(abstractDaoTypeSpec.build())

        implFileSpec.addType(implDaoTypeSpec.build())
        return SyncFileSpecs(abstractFileSpec.build(), implFileSpec.build(), repoImplSpec.build())
    }

    data class AbstractImplAndInterfaceFunSpecs(val abstractFunSpec: FunSpec, val implFunSpec: FunSpec,
                                                val interfaceFunSpec: FunSpec)
    private fun generateAbstractAndImplUpsertFuns(funName: String, paramSpec: ParameterSpec,
                                                  entityTypeSpec: TypeSpec,
                                                  daoTypeBuilder: TypeSpec.Builder,
                                                  abstractFunIsOverride: Boolean = false,
                                                  pgOnConflict: String? = null): AbstractImplAndInterfaceFunSpecs {
        val funBuilders = (0..2).map {
            FunSpec.builder(funName)
                    .returns(UNIT)
                    .addParameter(paramSpec)
        }
        funBuilders[0].addModifiers(KModifier.ABSTRACT)
        funBuilders[2].addModifiers(KModifier.ABSTRACT)

        if(abstractFunIsOverride) {
            funBuilders[0].addModifiers(KModifier.OVERRIDE)
        }

        funBuilders[0].addAnnotation(AnnotationSpec.builder(Insert::class)
                .addMember("onConflict = %T.REPLACE", OnConflictStrategy::class).build())
        if(pgOnConflict != null) {
            funBuilders[0].addAnnotation(AnnotationSpec.builder(PgOnConflict::class)
                    .addMember("value = %S", pgOnConflict)
                    .build())
        }

        funBuilders[1].addModifiers(KModifier.OVERRIDE)
        funBuilders[1].addCode(generateInsertCodeBlock(paramSpec, UNIT, entityTypeSpec,
                daoTypeBuilder,true, pgOnConflict = pgOnConflict))

        return AbstractImplAndInterfaceFunSpecs(funBuilders[0].build(), funBuilders[1].build(),
                funBuilders[2].build())
    }

    private fun generateAbstractAndImplQueryFunSpecs(querySql: String,
                                             funName: String,
                                             returnType: TypeName,
                                             params: List<ParameterSpec>,
                                             addReturnStmt: Boolean = true,
                                             abstractFunIsOverride: Boolean = false,
                                             suspended: Boolean = false): AbstractImplAndInterfaceFunSpecs {
        val funBuilders = (0..2).map {
            FunSpec.builder(funName)
                    .returns(returnType)
                    .addParameters(params)
                    .apply { takeIf { suspended }?.addModifiers(KModifier.SUSPEND) }
        }

        funBuilders[0].addModifiers(KModifier.ABSTRACT)
        if(abstractFunIsOverride)
            funBuilders[0].addModifiers(KModifier.OVERRIDE)
        funBuilders[1].addModifiers(KModifier.OVERRIDE)
        funBuilders[2].addModifiers(KModifier.ABSTRACT)


        funBuilders[0].addAnnotation(AnnotationSpec.builder(Query::class)
                .addMember("value = %S", querySql).build())

        funBuilders[1].addCode(generateQueryCodeBlock(returnType,
                params.map { it.name to it.type}.toMap(), querySql,
                null, null))

        if(addReturnStmt) {
            funBuilders[1].addCode("return _result\n")
        }

        return AbstractImplAndInterfaceFunSpecs(funBuilders[0].build(),
                funBuilders[1].build(), funBuilders[2].build())
    }


    companion object {

        const val SUFFIX_SYNCDAO_ABSTRACT = "SyncDao"

        const val SUFFIX_SYNCDAO_IMPL = "SyncDao_JdbcKt"

        const val SUFFIX_SYNC_ROUTE = "SyncDao_KtorRoute"

        const val TRACKER_SUFFIX = "_trk"

        const val TRACKER_PK_FIELDNAME = "pk"

        const val TRACKER_ENTITY_PK_FIELDNAME = "epk"

        const val TRACKER_DESTID_FIELDNAME = "clientId"

        const val TRACKER_CHANGESEQNUM_FIELDNAME = "csn"

        const val TRACKER_RECEIVED_FIELDNAME = "rx"

        const val TRACKER_REQUESTID_FIELDNAME = "reqId"

        const val TRACKER_TIMESTAMP_FIELDNAME = "ts"

        val SYSTEMTIME_MEMBER_NAME = MemberName("com.ustadmobile.door.util", "systemTimeInMillis")
    }

}