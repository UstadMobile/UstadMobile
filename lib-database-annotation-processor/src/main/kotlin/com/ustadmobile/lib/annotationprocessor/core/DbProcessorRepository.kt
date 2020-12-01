package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.*
import com.ustadmobile.door.daos.*
import com.ustadmobile.door.annotation.LastChangedBy
import io.ktor.client.HttpClient
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import com.ustadmobile.door.annotation.Repository
import com.ustadmobile.door.annotation.GetAttachmentData
import com.ustadmobile.door.annotation.SetAttachmentData
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_ANDROID_OUTPUT
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_JVM_DIRS
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorJdbcKotlin.Companion.SUFFIX_JDBC_KT2
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorRepository.Companion.BOUNDARY_CALLBACK_CLASSNAME
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorRepository.Companion.DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorRepository.Companion.SUFFIX_REPOSITORY2
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorSync.Companion.CLASSNAME_SYNC_HELPERENTITIES_DAO
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorSync.Companion.SUFFIX_SYNCDAO_ABSTRACT
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorSync.Companion.SUFFIX_SYNCDAO_IMPL
import kotlinx.coroutines.GlobalScope
import java.io.File
import java.util.*
import javax.annotation.processing.ProcessingEnvironment

internal fun newRepositoryClassBuilder(daoType: ClassName, addSyncHelperParam: Boolean = false,
        extraConstructorParams: List<ParameterSpec> = listOf()): TypeSpec.Builder {
    val idGetterLambdaType = LambdaTypeName.get(
            parameters = *arrayOf(DoorDatabase::class.asClassName()), returnType = Int::class.asClassName())
    val repoClassSpec = TypeSpec.classBuilder("${daoType.simpleName}_${DbProcessorRepository.SUFFIX_REPOSITORY}")
            .addProperty(PropertySpec.builder("_db", DoorDatabase::class)
                    .initializer("_db").build())
            .addProperty(PropertySpec.builder("_repo", DoorDatabaseRepository::class)
                    .initializer("_repo").build())
            .addProperty(PropertySpec.builder("_dao",
                    daoType).initializer("_dao").build())
            .addProperty(PropertySpec.builder("_httpClient",
                    HttpClient::class).initializer("_httpClient").build())
            .addProperty(PropertySpec.builder("_clientId", Int::class)
                    .getter(FunSpec.getterBuilder().addCode("return _clientIdFn(_db)\n")
                            .build())
                    .build())
            .addProperty(PropertySpec.builder("_clientIdFn",
                idGetterLambdaType).initializer("_clientIdFn").build())
            .addProperty(PropertySpec.builder("_endpoint", String::class)
                    .initializer("_endpoint").build())
            .addProperty(PropertySpec.builder("_dbPath", String::class)
                    .initializer("_dbPath").build())
            .addProperty(PropertySpec.builder("_attachmentsDir", String::class)
                    .initializer("_attachmentsDir").build())
            .superclass(daoType)
            .addAnnotation(AnnotationSpec.builder(Suppress::class)
                    .addMember("%S", "REDUNDANT_PROJECTION")
                    .build())

    val primaryConstructorFn = FunSpec.constructorBuilder()
            .addParameter("_db", DoorDatabase::class)
            .addParameter("_repo", DoorDatabaseRepository::class)
            .addParameter("_dao", daoType)
            .addParameter("_httpClient", HttpClient::class)
            .addParameter("_clientIdFn", idGetterLambdaType)
            .addParameter("_endpoint", String::class)
            .addParameter("_dbPath", String::class)
            .addParameter("_attachmentsDir", String::class)
            .apply {
                takeIf { extraConstructorParams.isNotEmpty() }?.addParameters(extraConstructorParams)
            }


    if(addSyncHelperParam) {
        val syncHelperClassName = ClassName(daoType.packageName,
                "${daoType.simpleName}_SyncHelper")
        primaryConstructorFn.addParameter("_syncHelper",
                syncHelperClassName)
        repoClassSpec.addProperty(PropertySpec.builder("_syncHelper", syncHelperClassName)
                .initializer("_syncHelper").build())
    }
    repoClassSpec.primaryConstructor(primaryConstructorFn.build())

    return repoClassSpec
}

/**
 * Where this TypeElement represents a Database class, this is the property name which should be
 * used for the property name for the SyncDao repo class. It will always be _DatabaseName_SyncDao
 */
private val TypeElement.syncDaoPropName: String
    get() = "_${this.simpleName}$SUFFIX_SYNCDAO_ABSTRACT"

/**
 * Where this TypeElement represents a Database class, this is the ClassName which should be used
 * for the abstract SyncDao. It will always be in the form of DatabaseName_SyncDao
 */
private val TypeElement.abstractSyncDaoClassName: ClassName
    get() = asClassNameWithSuffix(SUFFIX_SYNCDAO_ABSTRACT)

/**
 * Generate the table id map of entity names (strings) to the table id as per the syncableentity
 * annotation
 */
private fun TypeSpec.Builder.addTableIdMapProperty(dbTypeElement: TypeElement, processingEnv: ProcessingEnvironment) : TypeSpec.Builder {
    addProperty(PropertySpec.builder("TABLE_ID_MAP",
            Map::class.asClassName().parameterizedBy(String::class.asClassName(), INT))
            .initializer(CodeBlock.builder()
                    .add("mapOf(")
                    .apply {
                        dbTypeElement.allSyncableDbEntities(processingEnv).forEachIndexed { index, syncableEl ->
                            if(index > 0)
                                add(",")

                            val syncableEntityInfo = SyncableEntityInfo(syncableEl.asClassName(),
                                    processingEnv)
                            add("%S to %L\n", syncableEntityInfo.syncableEntity.simpleName,
                                    syncableEntityInfo.tableId)
                        }
                    }
                    .add(")\n")
                    .build())
            .build())

    return this
}

/**
 * Add a TypeSpec to the given FileSpec Builder that is an implementation of the repository for a
 * database as per the dbTypeElement parameter.
 */
fun FileSpec.Builder.addDbRepoType(dbTypeElement: TypeElement,
                                   processingEnv: ProcessingEnvironment,
                                   syncDaoMode: Int = DbProcessorRepository.REPO_SYNCABLE_DAO_CONSTRUCT,
                                   overrideClearAllTables: Boolean = true,
                                   overrideSyncDao: Boolean = false,
                                   overrideOpenHelper: Boolean = false,
                                   addDbVersionProp: Boolean = false,
                                   overrideKtorHelpers: Boolean = false): FileSpec.Builder {
    addType(TypeSpec.classBuilder(dbTypeElement.asClassNameWithSuffix(SUFFIX_REPOSITORY2))
            .superclass(dbTypeElement.asClassName())
            .apply {
                if(dbTypeElement.isDbSyncable(processingEnv)) {
                    addSuperinterface(DoorDatabaseSyncRepository::class)
                }else {
                    addSuperinterface(DoorDatabaseRepository::class)
                }
            }
            .primaryConstructor(FunSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder("_db", dbTypeElement.asClassName() ).build())
                .addParameter(ParameterSpec.builder("db", dbTypeElement.asClassName()).build())
                .addParameter(ParameterSpec.builder("_endpoint",
                        String::class.asClassName()).build())
                .addParameter("_accessToken", String::class)
                .addParameter(ParameterSpec.builder("_httpClient",
                        HttpClient::class.asClassName()).build())
                .addParameter(ParameterSpec.builder("_attachmentsDir",
                        String::class).build())
                .addParameter("_updateNotificationManager",
                        ServerUpdateNotificationManager::class.asClassName().copy(nullable = true))
                .addParameter("_useClientSyncManager", Boolean::class)
                .build())
            .addProperty(PropertySpec.builder("_db", dbTypeElement.asClassName())
                    .initializer("_db").build())
            .addProperty(PropertySpec.builder("db",
                    dbTypeElement.asClassName()).initializer("db")
                    .addModifiers(KModifier.OVERRIDE)
                    .build())
            .addProperty(PropertySpec.builder("_endpoint",
                    String::class.asClassName()).initializer("_endpoint").build())
            .addProperty(PropertySpec.builder("_accessToken", String::class)
                    .initializer("_accessToken").build())
            .addProperty(PropertySpec.builder("_httpClient",
                    HttpClient::class.asClassName()).initializer("_httpClient").build())
            .addProperty(PropertySpec.builder("endpoint", String::class)
                    .getter(FunSpec.getterBuilder().addCode("return _endpoint\n").build())
                    .addModifiers(KModifier.OVERRIDE)
                    .build())
            .addProperty(PropertySpec.builder("auth", String::class)
                .getter(FunSpec.getterBuilder().addCode("return _accessToken\n").build())
                .addModifiers(KModifier.OVERRIDE)
                .build())
            .addProperty(PropertySpec.builder("dbPath", String::class)
                    .getter(FunSpec.getterBuilder().addCode("return ${DbProcessorRepository.DB_NAME_VAR}\n").build())
                    .addModifiers(KModifier.OVERRIDE)
                    .build())
            .addProperty(PropertySpec.builder("httpClient", HttpClient::class)
                    .getter(FunSpec.getterBuilder().addCode("return _httpClient\n").build())
                    .addModifiers(KModifier.OVERRIDE)
                    .build())
            .addProperty(PropertySpec.builder("_attachmentsDir", String::class)
                    .initializer("_attachmentsDir")
                    .build())
            .addProperty(PropertySpec.builder("_updateNotificationManager",
                    ServerUpdateNotificationManager::class.asClassName().copy(nullable = true))
                    .initializer("_updateNotificationManager")
                    .build())
            .addProperty(PropertySpec.builder("_repositoryHelper", RepositoryHelper::class)
                    .initializer("%T(%M(%S))", RepositoryHelper::class,
                            MemberName("kotlinx.coroutines", "newSingleThreadContext"),
                            "Repo-${dbTypeElement.simpleName}")
                    .build())
            .addProperty(PropertySpec.builder("_clientSyncManager",
                    ClientSyncManager::class.asClassName().copy(nullable = true))
                    .initializer(CodeBlock.builder().beginControlFlow("if(_useClientSyncManager)")
                            .add("%T(this, _db.%M(), _repositoryHelper.connectivityStatus, httpClient)\n",
                                    ClientSyncManager::class, MemberName("com.ustadmobile.door.ext", "dbSchemaVersion"))
                            .nextControlFlow("else")
                            .add("null\n")
                            .endControlFlow()
                            .build())
                    .build())
            .addProperty(PropertySpec.builder("tableIdMap",
                    Map::class.asClassName().parameterizedBy(String::class.asClassName(), INT))
                    .getter(FunSpec.getterBuilder().addCode("return TABLE_ID_MAP\n").build())
                    .addModifiers(KModifier.OVERRIDE)
                    .build())
            .addFunction(FunSpec.builder("clearAllTables")
                    .addModifiers(KModifier.OVERRIDE)
                    .addCode("throw %T(%S)\n", IllegalAccessException::class, "Cannot use a repository to clearAllTables!")
                    .build())
            .applyIf(dbTypeElement.isDbSyncable(processingEnv)) {
                addFunction(FunSpec.builder("invalidateAllTables")
                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                        .addCode("_clientSyncManager?.invalidateAllTables()\n")
                        .build())
            }

            .addRepositoryHelperDelegateCalls("_repositoryHelper",
                    "_clientSyncManager")
            .applyIf(overrideClearAllTables) {
                addFunction(FunSpec.builder("createAllTables")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("throw %T(%S)\n",
                                IllegalAccessException::class,
                                "Cannot use a repository to createAllTables!")
                        .build())
            }
            .applyIf(overrideSyncDao) {
                addFunction(FunSpec.builder("_syncDao")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("return _db._syncDao()\n")
                        .returns(dbTypeElement.abstractSyncDaoClassName)
                        .build())
            }
            .applyIf(overrideSyncDao && dbTypeElement.isDbSyncable(processingEnv)){
                addFunction(FunSpec.builder("_syncHelperEntitiesDao")
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(ClassName("com.ustadmobile.door.daos",
                                "SyncHelperEntitiesDao"))
                        .addCode("return _db._syncHelperEntitiesDao()\n")
                        .build())
            }
            .applyIf(overrideOpenHelper) {
                addRoomCreateInvalidationTrackerFunction()
                addRoomDatabaseCreateOpenHelperFunction()
            }
            .applyIf(addDbVersionProp) {
                addDbVersionProperty(dbTypeElement)
            }
            .applyIf(dbTypeElement.isDbSyncable(processingEnv) &&
                    syncDaoMode == DbProcessorRepository.REPO_SYNCABLE_DAO_CONSTRUCT) {
                addProperty(PropertySpec.builder("_syncDao",
                                dbTypeElement.abstractSyncDaoClassName)
                        .delegate(CodeBlock.builder().beginControlFlow("lazy")
                                .add("%T(_db)\n", dbTypeElement.asClassNameWithSuffix(SUFFIX_SYNCDAO_IMPL))
                                .endControlFlow().build())
                        .build())
                addProperty(PropertySpec.builder("_syncHelperEntitiesDao",
                            CLASSNAME_SYNC_HELPERENTITIES_DAO)
                        .delegate(CodeBlock.builder().beginControlFlow("lazy")
                                .add("%T(db)\n", CLASSNAME_SYNC_HELPERENTITIES_DAO.withSuffix(SUFFIX_JDBC_KT2))
                                .endControlFlow()
                                .build())
                        .build())
            }.applyIf(dbTypeElement.isDbSyncable(processingEnv) &&
                    syncDaoMode == DbProcessorRepository.REPO_SYNCABLE_DAO_FROMDB) {
                addProperty(PropertySpec.builder("_syncDao",
                        dbTypeElement.abstractSyncDaoClassName)
                        .getter(FunSpec.getterBuilder()
                                .addCode("return _db._syncDao()\n")
                                .build())
                        .build())
                addProperty(PropertySpec.builder("_syncHelperEntitiesDao",
                        CLASSNAME_SYNC_HELPERENTITIES_DAO)
                        .getter(FunSpec.getterBuilder()
                                .addCode("return _db._syncHelperEntitiesDao()\n")
                                .build())
                        .build())
            }.applyIf(dbTypeElement.isDbSyncable(processingEnv)) {
                addProperty(PropertySpec.builder("syncHelperEntitiesDao",
                        ISyncHelperEntitiesDao::class)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder()
                            .addCode("return _syncHelperEntitiesDao")
                            .build())
                    .build())
                addProperty(PropertySpec.builder("_clientId", INT)
                        .delegate("lazy { _syncDao._findSyncNodeClientId() }").build())
                addProperty(PropertySpec.builder("_clientIdFn",
                        LambdaTypeName.get(parameters = *arrayOf(DoorDatabase::class.asClassName()),
                                returnType = Int::class.asClassName()))
                        .initializer("{ _db -> _clientId  }")
                        .build())
                addProperty(PropertySpec.builder("clientId", INT)
                        .getter(FunSpec.getterBuilder().addCode("return _clientId").build())
                        .addModifiers(KModifier.OVERRIDE)
                        .build())
                addProperty(PropertySpec.builder("master", BOOLEAN)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(FunSpec.getterBuilder().addCode("return _db.master").build())
                        .build())
                addProperty(PropertySpec.builder(dbTypeElement.syncDaoPropName,
                            dbTypeElement.asClassNameWithSuffix("$SUFFIX_SYNCDAO_ABSTRACT$SUFFIX_REPOSITORY2"))
                        .delegate(CodeBlock.builder().beginControlFlow("lazy")
                                .add("%T(_db, this, _syncDao, _httpClient, _clientIdFn, _endpoint," +
                                        " ${DbProcessorRepository.DB_NAME_VAR}, _attachmentsDir," +
                                        " _updateNotificationManager) ",
                                        dbTypeElement
                                                .asClassNameWithSuffix("$SUFFIX_SYNCDAO_ABSTRACT$SUFFIX_REPOSITORY2"))
                                .endControlFlow().build())
                        .build())
                addFunction(FunSpec.builder("sync")
                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                        .addParameter("tablesToSync", List::class.parameterizedBy(Int::class)
                        .copy(nullable = true))
                        .returns(List::class.parameterizedBy(SyncResult::class))
                        .addCode("return ${dbTypeElement.syncDaoPropName}.sync(tablesToSync)\n")
                        .build())
                addFunction(FunSpec.builder("dispatchUpdateNotifications")
                        .addParameter("tableId", INT)
                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                        .addCode("${dbTypeElement.syncDaoPropName}.dispatchUpdateNotifications(tableId)\n")
                        .build())
                addProperty(PropertySpec.builder("_sqlitePkManager",
                            DoorSqlitePrimaryKeyManager::class)
                        .initializer("%T(this)", DoorSqlitePrimaryKeyManager::class)
                        .build())
                addFunction(FunSpec.builder("getAndIncrementSqlitePk")
                        .addParameter("tableId", INT)
                        .addParameter("increment", INT)
                        .addModifiers(KModifier.OVERRIDE, KModifier.SUSPEND)
                        .addCode("return _sqlitePkManager.getAndIncrementSqlitePk(tableId, increment)\n")
                        .returns(LONG)
                        .build())
            }
            .apply {
                dbTypeElement.allDbClassDaoGetters(processingEnv).forEach { daoGetter ->
                    addRepoDbDaoAccessor(daoGetter, overrideKtorHelpers, processingEnv)
                }
            }
            .addType(TypeSpec.companionObjectBuilder()
                    .addTableIdMapProperty(dbTypeElement, processingEnv)
                    .addProperty(PropertySpec.builder(DbProcessorRepository.DB_NAME_VAR, String::class)
                            .addModifiers(KModifier.CONST)
                            .initializer("%S", dbTypeElement.simpleName)
                            .mutable(false).build())
                    .build())
            .build())

    return this
}

/**
 * Add an accessor function for the given dao accessor (and any related ktor helper daos if
 * specified).
 */
private fun TypeSpec.Builder.addRepoDbDaoAccessor(daoGetter: ExecutableElement,
                                          overrideKtorHelpers: Boolean,
                                          processingEnv: ProcessingEnvironment) : TypeSpec.Builder{
    val daoTypeEl = daoGetter.returnType.asTypeElement(processingEnv)
            ?: throw IllegalArgumentException("Dao getter has no return type")
    if(!daoTypeEl.hasAnnotation(Repository::class.java)) {
        addAccessorOverride(daoGetter, CodeBlock.of("throw %T(%S)\n",
                IllegalStateException::class,
                "${daoTypeEl.simpleName} is not annotated with @Repository"))
        return this
    }

    val daoTypeSpec = daoTypeEl.asTypeSpecStub(processingEnv)
    val daoHasSyncableEntities = daoTypeSpec.isDaoWithSyncableEntitiesInSelectResults(processingEnv)

    val syncDaoParam = if(daoHasSyncableEntities) {
        ", _syncDao"
    }else {
        ""
    }

    addProperty(PropertySpec.builder("_${daoTypeEl.simpleName}",
                daoTypeEl.asClassNameWithSuffix(SUFFIX_REPOSITORY2))
            .delegate(CodeBlock.builder().beginControlFlow("lazy")
                    .add("%T(_db, this, _db.%L, _httpClient, _clientIdFn, _endpoint, ${DbProcessorRepository.DB_NAME_VAR}, " +
                            "_attachmentsDir $syncDaoParam) ",
                            daoTypeEl.asClassNameWithSuffix(SUFFIX_REPOSITORY2),
                            daoGetter.makeAccessorCodeBlock())
                    .endControlFlow()
                    .build())
            .build())

    addAccessorOverride(daoGetter, CodeBlock.of("return  _${daoTypeEl.simpleName}"))

    if(overrideKtorHelpers) {
        listOf("Master", "Local").forEach {suffix ->
            val ktorHelperClassName = daoTypeEl.asClassNameWithSuffix(
                    "${DbProcessorKtorServer.SUFFIX_KTOR_HELPER}$suffix")
            addFunction(FunSpec.builder("_${ktorHelperClassName.simpleName}")
                    .returns(ktorHelperClassName)
                    .addModifiers(KModifier.OVERRIDE)
                    .addCode("throw %T(%S)", IllegalAccessException::class,
                            "Cannot access KTOR HTTP Helper from Repository")
                    .build())
        }
    }

    return this
}

/**
 * Add a TypeSpec repository implementation for the given DAO as given by daoTypeSpec
 *
 * @param daoTypeSpec The TypeSpec containing the FunSpecs for this DAO
 * @param daoClassName Classname for the abstract DAO class
 * @param processingEnv processing environment
 * @param pagingBoundaryCallbackEnabled true/false : whether or not an Android paging boundary
 * callback will be generated
 * @param isAlwaysSqlite true if the function being generated will always run on SQLite (eg
 * on Android), false otherwise (e.g. JDBC server)
 *
 */
fun FileSpec.Builder.addDaoRepoType(daoTypeSpec: TypeSpec,
                                    daoClassName: ClassName,
                                    processingEnv: ProcessingEnvironment,
                                    allKnownEntityTypesMap: Map<String, TypeElement>,
                                    pagingBoundaryCallbackEnabled: Boolean = false,
                                    isAlwaysSqlite: Boolean = false,
                                    extraConstructorParams: List<ParameterSpec> = listOf()): FileSpec.Builder {
    val idGetterLambdaType = LambdaTypeName.get(
            parameters = *arrayOf(DoorDatabase::class.asClassName()), returnType = Int::class.asClassName())

    addType(TypeSpec.classBuilder("${daoTypeSpec.name}$SUFFIX_REPOSITORY2")
            .addProperty(PropertySpec.builder("_db", DoorDatabase::class)
                    .initializer("_db").build())
            .addProperty(PropertySpec.builder("_repo", DoorDatabaseRepository::class)
                    .initializer("_repo").build())
            .addProperty(PropertySpec.builder("_dao",
                    daoClassName).initializer("_dao").build())
            .addProperty(PropertySpec.builder("_httpClient",
                    HttpClient::class).initializer("_httpClient").build())
            .addProperty(PropertySpec.builder("_clientId", Int::class)
                    .getter(FunSpec.getterBuilder().addCode("return _clientIdFn(_db)\n")
                            .build())
                    .build())
            .addProperty(PropertySpec.builder("_clientIdFn",
                    idGetterLambdaType).initializer("_clientIdFn").build())
            .addProperty(PropertySpec.builder("_endpoint", String::class)
                    .initializer("_endpoint").build())
            .addProperty(PropertySpec.builder("_dbPath", String::class)
                    .initializer("_dbPath").build())
            .addProperty(PropertySpec.builder("_attachmentsDir", String::class)
                    .initializer("_attachmentsDir").build())
            .applyIf(daoTypeSpec.isDaoWithSyncableEntitiesInSelectResults(processingEnv)) {
                addProperty(PropertySpec.builder("_syncHelper",
                        daoClassName.withSuffix("_SyncHelper"))
                        .initializer("_syncHelper")
                        .build())
            }
            .superclass(daoClassName)
            .addAnnotation(AnnotationSpec.builder(Suppress::class)
                    .addMember("%S, %S, %S", "REDUNDANT_PROJECTION", "LocalVariableName",
                        "ClassName")
                    .build())
            .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("_db", DoorDatabase::class)
                    .addParameter("_repo", DoorDatabaseRepository::class)
                    .addParameter("_dao", daoClassName)
                    .addParameter("_httpClient", HttpClient::class)
                    .addParameter("_clientIdFn", idGetterLambdaType)
                    .addParameter("_endpoint", String::class)
                    .addParameter("_dbPath", String::class)
                    .addParameter("_attachmentsDir", String::class)
                    .apply {
                        takeIf { extraConstructorParams.isNotEmpty() }?.addParameters(extraConstructorParams)
                    }
                    .applyIf(daoTypeSpec.isDaoWithSyncableEntitiesInSelectResults(processingEnv)) {
                        addParameter("_syncHelper", daoClassName.withSuffix("_SyncHelper"))
                    }
                    .build())
            //TODO: Ideally check and see if any of the return function types are DataSource.Factory
            .applyIf(pagingBoundaryCallbackEnabled &&
                    daoTypeSpec.isDaoWithSyncableEntitiesInSelectResults(processingEnv)) {
                addProperty(PropertySpec.builder(
                        DbProcessorRepository.DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME,
                        DbProcessorRepository.BOUNDARY_CALLBACK_MAP_CLASSNAME)
                        .initializer("%T()", WeakHashMap::class)
                        .build())
                addSuperinterface(ClassName("com.ustadmobile.door",
                    "DoorBoundaryCallbackProvider"))
                addFunction(FunSpec.builder("getBoundaryCallback")
                        .addAnnotation(AnnotationSpec.builder(Suppress::class)
                                .addMember("%S", "UNCHECKED_CAST")
                                .build())
                        .addTypeVariable(TypeVariableName("T"))
                        .addParameter("dataSource",
                                DataSource.Factory::class.asClassName().parameterizedBy(INT,
                                        TypeVariableName("T")))
                        .addModifiers(KModifier.OVERRIDE)
                        .returns(DbProcessorRepository.BOUNDARY_CALLBACK_CLASSNAME
                                .parameterizedBy(TypeVariableName("T")).copy(nullable = true))
                        .addCode("return ${DbProcessorRepository.DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME}[dataSource] as %T\n",
                                DbProcessorRepository.BOUNDARY_CALLBACK_CLASSNAME
                                .parameterizedBy(TypeVariableName("T")).copy(nullable = true))
                        .build())

            }
            .apply {
                daoTypeSpec.funSpecs.forEach {
                    addDaoRepoFun(it, daoClassName.simpleName, processingEnv,
                            allKnownEntityTypesMap, pagingBoundaryCallbackEnabled, isAlwaysSqlite)
                }
            }
            .build())

    return this
}

/**
 * Add a repo implementation of the given DAO FunSpec
 * @param daoFunSpec the function spec for which an implementation is being generated
 * @param daoName the name of the DAO class (simple name e.g. SomeDao)
 * @param processingEnv processing environment
 * @param pagingBoundaryCallbackEnabled true if an Android pagingboundarycallback is being
 * generated, false otherwise
 * @param isAlwaysSqlite true if the function will always run on SQLite, false otherwise
 */
fun TypeSpec.Builder.addDaoRepoFun(daoFunSpec: FunSpec,
                                   daoName: String,
                                   processingEnv: ProcessingEnvironment,
                                   allKnownEntityTypesMap: Map<String, TypeElement>,
                                   pagingBoundaryCallbackEnabled: Boolean,
                                   isAlwaysSqlite: Boolean = false) : TypeSpec.Builder {

    var repoMethodType = daoFunSpec.getAnnotationSpec(Repository::class.java)
            ?.memberToString(memberName = "methodType")?.toInt() ?: Repository.METHOD_AUTO

    if(repoMethodType == Repository.METHOD_AUTO) {
        repoMethodType = when {
            daoFunSpec.isQueryWithSyncableResults(processingEnv) -> Repository.METHOD_SYNCABLE_GET
            else -> Repository.METHOD_DELEGATE_TO_DAO
        }
    }

    var generateBoundaryCallback = false
    val returnTypeVal = daoFunSpec.returnType

    if(pagingBoundaryCallbackEnabled
            && repoMethodType == Repository.METHOD_SYNCABLE_GET
            && returnTypeVal is ParameterizedTypeName
            && returnTypeVal.rawType == DataSource.Factory::class.asClassName()) {
        generateBoundaryCallback = true
        repoMethodType = Repository.METHOD_DELEGATE_TO_DAO
    }

    addFunction(daoFunSpec.toBuilder()
            .addCode(CodeBlock.builder().apply {
                when(repoMethodType) {
                    Repository.METHOD_SYNCABLE_GET -> {
                        addRepositoryGetSyncableEntitiesCode(daoFunSpec,
                                daoName, processingEnv,
                                addReturnDaoResult = !generateBoundaryCallback)
                    }
                    Repository.METHOD_DELEGATE_TO_DAO -> {
                        if(generateBoundaryCallback) {
                            addBoundaryCallbackCode(daoFunSpec, daoName, processingEnv)
                        }else {
                            addRepoDelegateToDaoCode(daoFunSpec, isAlwaysSqlite, processingEnv,
                                    allKnownEntityTypesMap)
                        }

                    }
                    Repository.METHOD_DELEGATE_TO_WEB -> {
                        addDelegateToWebCode(daoFunSpec, daoName)
                    }

                }
            }.build())
            .build())

    return this
}

/**
 * Add code which fetches any new syncable entities from the server and returns the results from the
 * DAO.
 *
 * @param daoFunSpec the DAO function spec for which this code is being generated
 * @param daoName the simple name of the DAO class
 * @param processingEnv processing environment
 * @param syncHelperDaoVarName the variable name of the sync helper dao
 * @param addReturnDaoResult true to add a return statement to the end of the code
 * @param generateGlobalScopeLaunchBlockForLiveDataTypes true to put the http fetch for functions
 * that return a LiveData object in a GlobalScope.launch so they run asynchronously. True by default
 * @param autoRetryEmptyMirrorResult will be removed
 * @param receiveCountVarName if not null, a variable that will hold a count of how many entities
 * are received
 */
fun CodeBlock.Builder.addRepositoryGetSyncableEntitiesCode(daoFunSpec: FunSpec, daoName: String,
                                                           processingEnv: ProcessingEnvironment,
                                                           syncHelperDaoVarName: String = "_syncHelper",
                                                           addReturnDaoResult: Boolean  = true,
                                                           generateGlobalScopeLaunchBlockForLiveDataTypes: Boolean = true,
                                                           autoRetryEmptyMirrorResult: Boolean = false,
                                                           receiveCountVarName: String? = null) : CodeBlock.Builder {

    val isLiveDataOrDataSourceFactory = daoFunSpec.returnType?.isDataSourceFactoryOrLiveData() == true
    val isLiveData = daoFunSpec.returnType?.isLiveData() == true

    if(isLiveDataOrDataSourceFactory && addReturnDaoResult) {
        add("val _daoResult = ").addDelegateFunctionCall("_dao", daoFunSpec).add("\n")
    }

    if(isLiveDataOrDataSourceFactory && generateGlobalScopeLaunchBlockForLiveDataTypes) {
        beginControlFlow("%T.%M", GlobalScope::class,
                MemberName("kotlinx.coroutines", "launch"))
        beginControlFlow("try")
    }

    //Create the loadhelper that would actually run the request
    val liveDataLoadHelperArg = if(isLiveData) "autoRetryOnEmptyLiveData=_daoResult," else ""
    beginControlFlow("val _loadHelper = %T(_repo,·" +
            "autoRetryEmptyMirrorResult·=·$autoRetryEmptyMirrorResult,·$liveDataLoadHelperArg·" +
            "uri·=·%S)", RepositoryLoadHelper::class, "$daoName/${daoFunSpec.name}")
    add("_endpointToTry -> \n")

    add("val _httpResult = ")
    addKtorRequestForFunction(daoFunSpec, dbPathVarName = "_dbPath", daoName = daoName,
        httpEndpointVarName = "_endpointToTry", addClientIdHeaderVar = "_clientId")
    addReplaceSyncableEntitiesIntoDbCode("_httpResult",
            daoFunSpec.returnType!!.unwrapLiveDataOrDataSourceFactory(), processingEnv,
                daoName = daoName)
    if(receiveCountVarName != null) {
        add("$receiveCountVarName += _httpResult.size\n")
    }

    //end the LoadHelper block
    add("_httpResult\n")
    endControlFlow()

    if(isLiveDataOrDataSourceFactory && generateGlobalScopeLaunchBlockForLiveDataTypes) {
        add("_loadHelper.doRequest()\n")
        nextControlFlow("catch(_e: %T)", Exception::class)
        add("%M(%S)\n", MemberName("kotlin.io", "println"), "Caught doRequest exception:")
        endControlFlow()
        endControlFlow()
    }

    if(addReturnDaoResult) {
        if(!isLiveDataOrDataSourceFactory) {
            //use the repoloadhelper to actually run the request and get the result
            add("var _daoResult: %T\n", daoFunSpec.returnType?.unwrapLiveDataOrDataSourceFactory())
                    .beginControlFlow("do"
                    ).applyIf(KModifier.SUSPEND !in daoFunSpec.modifiers) {
                        beginControlFlow("%M",
                                MemberName("kotlinx.coroutines", "runBlocking"))
                    }
                    .beginControlFlow("try")
                    .add("_loadHelper.doRequest()\n")
                    .nextControlFlow("catch(_e: %T)", Exception::class)
                    .add("%M(%S)", MemberName("kotlin.io", "println"), "Caught doRequest exception: \\\$_e")
                    .endControlFlow()
                    .applyIf(KModifier.SUSPEND !in daoFunSpec.modifiers) {
                        endControlFlow()
                    }
                    .add("_daoResult = ").addDelegateFunctionCall("_dao", daoFunSpec).add("\n")
                    .endControlFlow()
                    .add("while(_loadHelper.shouldTryAnotherMirror())\n")
                    .add("return _daoResult\n")
        }else {
            add("return _daoResult\n")
        }
    }

    return this
}


/**
 * Add code that will handle receiving new syncable entities from the server. The syncable entities
 * should only be those that are new to the client. The entities will be inserted using a replace
 * function on the SyncHelper, and then an http request will be made to the server to acknowledge
 * receipt of the entities.
 */
fun CodeBlock.Builder.addReplaceSyncableEntitiesIntoDbCode(resultVarName: String, resultType: TypeName,
                                                           processingEnv: ProcessingEnvironment,
                                                           daoName: String,
                                                           syncHelperDaoVarName: String = "_syncHelper") : CodeBlock.Builder{
    val componentType = resultType.unwrapQueryResultComponentType()
    if(componentType !is ClassName)
        return this


    //Block at the end which will run all inserts of newly received entities
    val transactionCodeBlock = CodeBlock.builder()

    val sendTrkEntitiesCodeBlock = CodeBlock.builder()

    componentType.findAllSyncableEntities(processingEnv).forEach {
        val sEntityInfo = SyncableEntityInfo(it.value, processingEnv)

        val replaceEntityFnName ="_replace${sEntityInfo.syncableEntity.simpleName}"

        val accessorVarName = "_se${sEntityInfo.syncableEntity.simpleName}"
        add("val $accessorVarName = $resultVarName")

        if(resultType.isListOrArray()) {
            it.key.forEach {embedVarName ->
                beginControlFlow(".mapNotNull ")
                add("it.$embedVarName", it.value.copy(nullable = true))
                endControlFlow()
            }

            if(it.key.isEmpty() && it.value != sEntityInfo.syncableEntity) {
                add(".map { it as %T }", sEntityInfo.syncableEntity)
            }

            add("\n")
            transactionCodeBlock.add("${syncHelperDaoVarName}.$replaceEntityFnName($accessorVarName)\n")
        }else {
            if(it.key.isNotEmpty())
                add("?.")

            add(it.key.joinToString (prefix = "", separator = "?.", postfix = ""))
            add("\n")
            transactionCodeBlock.
                beginControlFlow("if($accessorVarName != null)")
                    .add("${syncHelperDaoVarName}.$replaceEntityFnName(listOf($accessorVarName))\n")
                    .endControlFlow()
        }

        sendTrkEntitiesCodeBlock.beginIfNotNullOrEmptyControlFlow(accessorVarName,
                resultType.isListOrArray())
                .add("val _ackList = ")
                .apply {
                    if(!resultType.isListOrArray())
                        add("listOf($accessorVarName)")
                    else
                        add(accessorVarName)
                }
                .beginControlFlow(".map")
                .add("·%T(epk·=·it.${sEntityInfo.entityPkField.name},·" +
                        "csn·=·it.${sEntityInfo.entityMasterCsnField.name}", EntityAck::class)
                .applyIf(sEntityInfo.entityMasterCsnField.type == LONG) {
                    add(".toInt()")
                }
                .add(")\n")
                .endControlFlow()
                .add("\n")
                .add("_httpClient.%M(_ackList, _endpoint, \"${'$'}_dbPath/$daoName/_ack${it.value.simpleName}Received\", _db)\n",
                    MemberName("com.ustadmobile.door.ext", "postEntityAck"))
                .endControlFlow()
    }

    beginControlFlow("_db.runInTransaction(%T", Runnable::class)
    add(transactionCodeBlock.build())
    endControlFlow()
    add(")\n")
    add(sendTrkEntitiesCodeBlock.build())

    return this
}

/**
 * Add a CodeBlock for a repo delegate to DAO function. This will
 *
 * 1) Set the primary key on any entities that don't have a primary key set if running
 * on SQLite when running an insert
 * 2) Update the change sequence numbers when running an update
 * 3) Pass the work to the DAO and return the result
 *
 * TODO: Update last changed by field, return primary key values from pk manager if applicable
 */
fun CodeBlock.Builder.addRepoDelegateToDaoCode(daoFunSpec: FunSpec, isAlwaysSqlite: Boolean,
                                   processingEnv: ProcessingEnvironment,
                                   allKnownEntityTypesMap: Map<String, TypeElement>) : CodeBlock.Builder{

    var syncableEntityInfo: SyncableEntityInfo? = null

    if(daoFunSpec.hasAnyAnnotation(Update::class.java, Delete::class.java, Insert::class.java)) {
        val entityParam = daoFunSpec.parameters.first()
        val entityComponentType = entityParam.type.unwrapListOrArrayComponentType()
        if(entityComponentType.hasSyncableEntities(processingEnv)) {
            syncableEntityInfo = SyncableEntityInfo(entityComponentType as ClassName, processingEnv)
            if(daoFunSpec.hasAnyAnnotation(Update::class.java)) {
                add("val _isSyncablePrimary = _db.%M\n",
                        MemberName("com.ustadmobile.door.ext", "syncableAndPrimary"))
            }

            if(daoFunSpec.hasAnnotation(Update::class.java)) {
                var entityVarName = entityParam.name
                if(entityParam.type.isListOrArray()) {
                    beginControlFlow("${entityParam.name}.forEach")
                    entityVarName = "it"
                }

                add("$entityVarName.${syncableEntityInfo.entityLastChangedByField.name} = _clientId\n")
                beginControlFlow("if(_isSyncablePrimary)")
                add("$entityVarName.${syncableEntityInfo.entityMasterCsnField.name} = 0\n")
                nextControlFlow("else")
                add("$entityVarName.${syncableEntityInfo.entityLocalCsnField.name} = 0\n")
                endControlFlow()

                if(entityParam.type.isListOrArray()) {
                    endControlFlow()
                }
            }



            //Use the SQLite Primary key manager if this is an SQLite insert
            if(daoFunSpec.hasAnnotation(Insert::class.java)) {
                if(!isAlwaysSqlite)
                    beginControlFlow("if(_db.jdbcDbType == %T.SQLITE)", DoorDbType::class)

                if(entityParam.type.isListOrArray()) {
                    add("var _nextPk = ")
                    if(!daoFunSpec.isSuspended) {
                        beginRunBlockingControlFlow()
                    }
                    add("(_repo as %T).getAndIncrementSqlitePk(" +
                            "${syncableEntityInfo.tableId}, ${entityParam.name}.size)\n",
                            DoorDatabaseSyncRepository::class)
                    if(!daoFunSpec.isSuspended) {
                        endControlFlow()
                    }

                    beginControlFlow("${entityParam.name}.forEach")
                    add("it.${syncableEntityInfo.entityLastChangedByField.name} = _clientId\n")
                    add("it.takeIf { it.${syncableEntityInfo.entityPkField.name} == 0L}?." +
                            "${syncableEntityInfo.entityPkField.name} = _nextPk++\n")
                    endControlFlow()
                }else {
                    add("${entityParam.name}.${syncableEntityInfo.entityLastChangedByField.name}·=·_clientId\n")
                    add("${entityParam.name}.takeIf·{·it.${syncableEntityInfo.entityPkField.name}·==·0L·}" +
                            "?.${syncableEntityInfo.entityPkField.name}·=·")

                    if(!daoFunSpec.isSuspended)
                        beginRunBlockingControlFlow()

                    add("(_repo·as·%T).getAndIncrementSqlitePk(" +
                            "${syncableEntityInfo.tableId}, 1)\n", DoorDatabaseSyncRepository::class)

                    if(!daoFunSpec.isSuspended)
                        endControlFlow()
                }

                if(!isAlwaysSqlite)
                    endControlFlow()

            }
        }
    }



    if(daoFunSpec.hasReturnType)
        add("val _result = ")

    add("_dao.${daoFunSpec.name}(")
            .add(daoFunSpec.parameters.joinToString { it.name })
            .add(")\n")

    //Check if a table is modified by this query
    val tableChanged = if(daoFunSpec.hasAnyAnnotation(Update::class.java, Insert::class.java,
            Delete::class.java)) {
        (daoFunSpec.entityParamComponentType as ClassName).simpleName
    }else if(daoFunSpec.isAQueryThatModifiesTables){
        daoFunSpec.getDaoFunEntityModifiedByQuery(allKnownEntityTypesMap)
    }else {
        null
    }

    if(tableChanged != null) {
        add("_repo")
        if(daoFunSpec.isAQueryThatModifiesTables && daoFunSpec.hasReturnType)
            add(".takeIf·{·_result·>·0·}?")
        add(".handleTableChanged(%S)\n", tableChanged)
    }

    if(daoFunSpec.hasReturnType && daoFunSpec.hasAnnotation(Insert::class.java)
            && syncableEntityInfo != null) {
        add("return ")
        if(!isAlwaysSqlite)
            beginControlFlow("if(_db.jdbcDbType == %T.SQLITE)", DoorDbType::class)

        if(daoFunSpec.parameters.first().type.isListOrArray()) {
            add("${daoFunSpec.parameters[0].name}.map·{·it.${syncableEntityInfo.entityPkField.name}·}")
            if(daoFunSpec.returnType?.isArrayType() == true)
                add(".%M()", MemberName("kotlin.collections", "toTypedArray"))

            add("\n")
        }else {
            add("${daoFunSpec.parameters[0].name}.${syncableEntityInfo.entityPkField.name}·\n")
        }

        if(!isAlwaysSqlite) {
            nextControlFlow("else")
            add("return _result\n")
            endControlFlow()
        }
    }else if(daoFunSpec.hasReturnType) {
        add("return _result\n")
    }

    return this
}

/**
 * Generate the Android paging boundary callback implementation
 */
fun CodeBlock.Builder.addBoundaryCallbackCode(daoFunSpec: FunSpec, daoName: String,
    processingEnv: ProcessingEnvironment) : CodeBlock.Builder{

    val boundaryCallbackFunSpec = daoFunSpec.toBuilder()
            .addParameter(PARAM_NAME_LIMIT, INT)
            .build()
    add("val _dataSource = ").addDelegateFunctionCall("_dao", daoFunSpec).add("\n")
    add("val $PARAM_NAME_LIMIT = 50\n")

    addRepositoryGetSyncableEntitiesCode(boundaryCallbackFunSpec, daoName, processingEnv,
            generateGlobalScopeLaunchBlockForLiveDataTypes = false,
            addReturnDaoResult = false)

    val unwrappedComponentType = daoFunSpec.returnType?.unwrapQueryResultComponentType()
            ?: throw IllegalArgumentException("${daoFunSpec.name} on $daoName has no return type")
    add("$DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME[_dataSource] = %T(_loadHelper)\n",
            BOUNDARY_CALLBACK_CLASSNAME.parameterizedBy(unwrappedComponentType))
    add("return _dataSource\n")

    return this
}

fun CodeBlock.Builder.addDelegateToWebCode(daoFunSpec: FunSpec, daoName: String) : CodeBlock.Builder {
    if(daoFunSpec.hasReturnType) {
        add("return ")
    }

    if(!daoFunSpec.isSuspended) {
        beginRunBlockingControlFlow()
    }

    addKtorRequestForFunction(daoFunSpec, dbPathVarName = "_dbPath", daoName = daoName)

    if(!daoFunSpec.isSuspended) {
        endControlFlow()
    }

    return this
}
class DbProcessorRepository: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)


        for(dbTypeEl in dbs) {
            FileSpec.builder(dbTypeEl.packageName, "${dbTypeEl.simpleName}$SUFFIX_REPOSITORY2")
                    .addDbRepoType(dbTypeEl as TypeElement, processingEnv,
                        syncDaoMode = REPO_SYNCABLE_DAO_CONSTRUCT, addDbVersionProp = true)
                    .build()
                    .writeToDirsFromArg(OPTION_JVM_DIRS)
            FileSpec.builder(dbTypeEl.packageName, "${dbTypeEl.simpleName}$SUFFIX_REPOSITORY2")
                    .addDbRepoType(dbTypeEl as TypeElement,
                            processingEnv,
                        syncDaoMode = REPO_SYNCABLE_DAO_FROMDB, overrideClearAllTables = false,
                        overrideSyncDao = true, overrideOpenHelper = true,
                        overrideKtorHelpers = true)
                    .build()
                    .writeToDirsFromArg(OPTION_ANDROID_OUTPUT)
        }

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        for(daoElement in daos) {
            val daoTypeEl = daoElement as TypeElement
            if(daoTypeEl.isDaoWithRepository) {
                FileSpec.builder(daoElement.packageName,
                        "${daoTypeEl.simpleName}$SUFFIX_REPOSITORY2")
                        .addDaoRepoType(daoTypeEl.asTypeSpecStub(processingEnv,
                                convertToImplementationStub = true),
                            daoTypeEl.asClassName(), processingEnv,
                            allKnownEntityTypesMap = allKnownEntityTypesMap)
                        .build()
                        .writeToDirsFromArg(OPTION_JVM_DIRS)

                FileSpec.builder(daoElement.packageName,
                        "${daoTypeEl.simpleName}$SUFFIX_REPOSITORY2")
                        .addDaoRepoType(daoTypeEl.asTypeSpecStub(processingEnv,
                                convertToImplementationStub = true),
                                daoTypeEl.asClassName(), processingEnv,
                                allKnownEntityTypesMap = allKnownEntityTypesMap,
                                pagingBoundaryCallbackEnabled = true,
                                isAlwaysSqlite = true)
                        .build()
                        .writeToDirsFromArg(OPTION_ANDROID_OUTPUT)
            }
        }

        return true
    }


    companion object {
        const val SUFFIX_REPOSITORY = "Repo"

        //including the underscore as it should
        const val SUFFIX_REPOSITORY2 = "_Repo"

        /**
         * When creating a repository, the Syncable DAO is constructed (JDBC). This is because
         * the database itself cannot have fields or method signatures that are themselves generated
         * classes
         */
        const val REPO_SYNCABLE_DAO_CONSTRUCT = 1

        /**
         * When creatin ga repository, the Syncable DAO is obtained from the database. This is done
         * on Room on Android, where the database class is slightly modified and all DAOs must come
         * from the database object
         */
        const val REPO_SYNCABLE_DAO_FROMDB = 2

        /**
         * A static string which is generated for the database name part of the http path, which is
         * passed from the database repository to the DAO repository so it can use the correct http
         * path e.g. endpoint/dbname/daoname
         */
        const val DB_NAME_VAR = "_DB_NAME"

        const val DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME = "_dataSourceFactoryToBoundaryCallbackMap"

        val BOUNDARY_CALLBACK_CLASSNAME = ClassName("com.ustadmobile.door",
                "RepositoryBoundaryCallback")

        val BOUNDARY_CALLBACK_MAP_CLASSNAME = WeakHashMap::class.asClassName().parameterizedBy(
                DataSource.Factory::class.asClassName().parameterizedBy(INT, STAR),
                BOUNDARY_CALLBACK_CLASSNAME.parameterizedBy(STAR))

    }



}