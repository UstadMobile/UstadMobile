package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.*
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
import kotlinx.coroutines.newSingleThreadContext
import java.io.File
import java.util.*
import kotlin.reflect.KClass

internal fun newRepositoryClassBuilder(daoType: ClassName, addSyncHelperParam: Boolean = false): TypeSpec.Builder {
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
                    .initializer("_clientId").build())
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
            .addParameter("_clientId", Int::class)
            .addParameter("_endpoint", String::class)
            .addParameter("_dbPath", String::class)
            .addParameter("_attachmentsDir", String::class)


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



class DbProcessorRepository: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)

        for(dbTypeEl in dbs) {
            writeFileSpecToOutputDirs(generateDbRepositoryClass(dbTypeEl as TypeElement,
                    syncDaoMode = REPO_SYNCABLE_DAO_CONSTRUCT, addDbVersionProp = true),
                    AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            writeFileSpecToOutputDirs(generateDbRepositoryClass(dbTypeEl as TypeElement,
                    syncDaoMode = REPO_SYNCABLE_DAO_FROMDB, overrideClearAllTables = false,
                    overrideSyncDao = true, overrideOpenHelper = true,
                    addBoundaryCallbackGetters = true,
                    overrideKtorHelpers = true),
                    AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT)
        }

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        for(daoElement in daos) {
            val daoTypeEl = daoElement as TypeElement
            writeFileSpecToOutputDirs(generateDaoRepositoryClass(daoTypeEl),
                    AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            val androidRepoFileSpec = generateDaoRepositoryClass(daoTypeEl,
                    pagingBoundaryCallbackEnabled = true)
            writeFileSpecToOutputDirs(androidRepoFileSpec,
                    AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT)
        }

        return true
    }


    fun generateDbRepositoryClass(dbTypeElement: TypeElement,
                                  syncDaoMode: Int = REPO_SYNCABLE_DAO_CONSTRUCT,
                                  overrideClearAllTables: Boolean = true,
                                  overrideSyncDao: Boolean = false,
                                  overrideOpenHelper: Boolean = false,
                                  addDbVersionProp: Boolean = false,
                                  addBoundaryCallbackGetters: Boolean = false,
                                  overrideKtorHelpers: Boolean = false): FileSpec {
        val dbRepoFileSpec = FileSpec.builder(pkgNameOfElement(dbTypeElement, processingEnv),
                "${dbTypeElement.simpleName}_$SUFFIX_REPOSITORY")
        val isDbTypeSyncable = isSyncableDb(dbTypeElement, processingEnv)

        val repoInterface = if(isDbTypeSyncable) {
            DoorDatabaseRepository::class.asClassName()
        } else {
            DoorDatabaseSyncRepository::class.asClassName()
        }

        val dbRepoType = TypeSpec.classBuilder("${dbTypeElement.simpleName}_$SUFFIX_REPOSITORY")
                .superclass(dbTypeElement.asClassName())
                .addSuperinterface(repoInterface)
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("_db", dbTypeElement.asClassName() ).build())
                        .addParameter(ParameterSpec.builder("_endpoint", String::class.asClassName()).build())
                        .addParameter("_accessToken", String::class)
                        .addParameter(ParameterSpec.builder("_httpClient", HttpClient::class.asClassName()).build())
                        .addParameter(ParameterSpec.builder("_attachmentsDir", String::class).build())
                        .build())
                .addProperties(listOf(
                        PropertySpec.builder("_db",
                            dbTypeElement.asClassName()).initializer("_db").build(),
                        PropertySpec.builder("_endpoint",
                                String::class.asClassName()).initializer("_endpoint").build(),
                        PropertySpec.builder("_accessToken", String::class)
                                .initializer("_accessToken").build(),
                        PropertySpec.builder("_httpClient",
                            HttpClient::class.asClassName()).initializer("_httpClient").build(),
                        PropertySpec.builder("endpoint", String::class)
                                .getter(FunSpec.getterBuilder().addCode("return _endpoint\n").build())
                                .addModifiers(KModifier.OVERRIDE)
                                .build(),
                        PropertySpec.builder("auth", String::class)
                                .getter(FunSpec.getterBuilder().addCode("return _accessToken\n").build())
                                .addModifiers(KModifier.OVERRIDE)
                                .build(),
                        PropertySpec.builder("dbPath", String::class)
                                .getter(FunSpec.getterBuilder().addCode("return $DB_NAME_VAR\n").build())
                                .addModifiers(KModifier.OVERRIDE)
                                .build(),
                        PropertySpec.builder("httpClient", HttpClient::class)
                                .getter(FunSpec.getterBuilder().addCode("return _httpClient\n").build())
                                .addModifiers(KModifier.OVERRIDE)
                                .build(),
                        PropertySpec.builder("_attachmentsDir", String::class)
                                .initializer("_attachmentsDir")
                                .build(),
                        PropertySpec.builder("_repositoryHelper", RepositoryHelper::class)
                                .initializer("%T(%M(%S))", RepositoryHelper::class,
                                        MemberName("kotlinx.coroutines", "newSingleThreadContext"),
                                        "Repo-${dbTypeElement.simpleName}")
                                .build()
                ))
                .addFunction(FunSpec.builder("clearAllTables")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("throw %T(%S)\n", IllegalAccessException::class, "Cannot use a repository to clearAllTables!")
                        .build())
                .addType(TypeSpec.companionObjectBuilder()
                        .addProperty(PropertySpec.builder(DB_NAME_VAR, String::class)
                                .addModifiers(KModifier.CONST)
                                .initializer("%S", dbTypeElement.simpleName)
                                .mutable(false).build())
                        .build())


        dbRepoType.addRepositoryHelperDelegateCalls("_repositoryHelper")

        if(overrideClearAllTables) {
            newSingleThreadContext("")
            dbRepoType.addFunction(FunSpec.builder("createAllTables")
                    .addModifiers(KModifier.OVERRIDE)
                    .addCode("throw %T(%S)\n", IllegalAccessException::class, "Cannot use a repository to createAllTables!")
                    .build())
        }

        if(overrideSyncDao) {
            val dbTypeClassName = dbTypeElement.asClassName()
            dbRepoType.addFunction(FunSpec.builder("_syncDao")
                    .addModifiers(KModifier.OVERRIDE)
                    .addCode("return _db._syncDao()")
                    .returns(ClassName(dbTypeClassName.packageName,
                            "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}"))
                    .build())
        }

        if(overrideOpenHelper) {
            val invalidationTrackerClassName = ClassName("androidx.room", "InvalidationTracker")
            dbRepoType.addFunction(FunSpec.builder("createOpenHelper")
                    .addParameter("config", ClassName("androidx.room", "DatabaseConfiguration"))
                    .returns(ClassName("androidx.sqlite.db", "SupportSQLiteOpenHelper"))
                    .addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
                    .addCode("throw IllegalAccessException(%S)\n", "Cannot use open helper on repository")
                    .build())
            dbRepoType.addFunction(FunSpec.builder("createInvalidationTracker")
                    .returns(invalidationTrackerClassName)
                    .addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
                    .addCode("return %T.createDummyInvalidationTracker(this)\n",
                            ClassName("com.ustadmobile.door","DummyInvalidationTracker"))
                    .build())
        }

        if(addDbVersionProp) {
            dbRepoType.addProperty(PropertySpec.builder("dbVersion", INT)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder()
                            .addCode("return _db.dbVersion")
                            .build())
                    .build())
        }


        if(isDbTypeSyncable) {
            val syncableDaoClassName = ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                    "${dbTypeElement.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}")
            val syncDaoProperty = PropertySpec.builder("_syncDao", syncableDaoClassName)
            if(syncDaoMode == REPO_SYNCABLE_DAO_CONSTRUCT) {
                syncDaoProperty.delegate(
                        CodeBlock.builder().beginControlFlow("lazy")
                                .add("%T(_db) ", ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                                        "${dbTypeElement.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_IMPL}"))
                                .endControlFlow().build())
            }else if(syncDaoMode == REPO_SYNCABLE_DAO_FROMDB) {
                syncDaoProperty.delegate("lazy {_db._syncDao() }")
            }

            dbRepoType.addProperty(syncDaoProperty.build())

            dbRepoType.addProperty(PropertySpec.builder("_clientId", INT)
                    .delegate("lazy { _syncDao._findSyncNodeClientId() }").build())
            dbRepoType.addProperty(PropertySpec.builder("clientId", INT)
                    .getter(FunSpec.getterBuilder().addCode("return _clientId").build())
                    .addModifiers(KModifier.OVERRIDE)
                    .build())

            dbRepoType.addProperty(PropertySpec.builder("master", BOOLEAN)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder().addCode("return _db.master").build())
                    .build())

            val repoImplClassName = ClassName(dbTypeElement.asClassName().packageName,
                    "${syncableDaoClassName.simpleName}_$SUFFIX_REPOSITORY")
            dbRepoType.addProperty(PropertySpec
                    .builder("_${syncableDaoClassName.simpleName}", repoImplClassName)
                    .delegate(CodeBlock.builder().beginControlFlow("lazy")
                            .add("%T(_db, this, _syncDao, _httpClient, _clientId, _endpoint, $DB_NAME_VAR, _attachmentsDir) ", repoImplClassName)
                            .endControlFlow().build())
                    .build())
            dbRepoType.addSuperinterface(DoorDatabaseSyncRepository::class)
            dbRepoType.addFunction(FunSpec.builder("sync")
                    .addModifiers(KModifier.OVERRIDE,KModifier.SUSPEND)
                    .addParameter("entities", List::class.asClassName().parameterizedBy(
                            KClass::class.asClassName().parameterizedBy(STAR)).copy(nullable = true))
                    .addCode("_${syncableDaoClassName.simpleName}.sync(entities)\n")
                    .build())

        }

        methodsToImplement(dbTypeElement, dbTypeElement.asType() as DeclaredType, processingEnv)
            .filter{it.kind == ElementKind.METHOD }.map {it as ExecutableElement }.forEach {

            val daoTypeEl = processingEnv.typeUtils.asElement(it.returnType) as TypeElement?
            if(daoTypeEl == null)
                return@forEach

            val daoClassName = daoTypeEl.asClassName()
            val repoImplClassName = ClassName(pkgNameOfElement(daoTypeEl, processingEnv),
                    "${daoTypeEl.simpleName}_$SUFFIX_REPOSITORY")
            val daoHasSyncableEntities = syncableEntitiesOnDao(daoTypeEl.asClassName(), processingEnv)
                    .isNotEmpty()
            val syncDaoParam = if(daoHasSyncableEntities) {
                ", _syncDao"
            }else {
                ""
            }

            dbRepoType.addProperty(PropertySpec.builder("_${daoTypeEl.simpleName}",  repoImplClassName)
                    .delegate(CodeBlock.builder().beginControlFlow("lazy")
                            .add("%T(_db, this, _db.%L, _httpClient, _clientId, _endpoint, $DB_NAME_VAR, " +
                                    "_attachmentsDir $syncDaoParam) ",
                                repoImplClassName, it.makeAccessorCodeBlock())
                            .endControlFlow()
                            .build())
                    .build())
            dbRepoType.addAccessorOverride(it, CodeBlock.of("return  _${daoTypeEl.simpleName}"))

            if(daoHasSyncableEntities && overrideKtorHelpers) {
                listOf("Master", "Local").forEach {suffix ->
                    val ktorHelperClassName = ClassName(daoClassName.packageName,
                            "${daoClassName.simpleName}${DbProcessorKtorServer.SUFFIX_KTOR_HELPER}$suffix")
                    dbRepoType.addFunction(FunSpec.builder("_${ktorHelperClassName.simpleName}")
                            .returns(ktorHelperClassName)
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode("throw %T(%S)", IllegalAccessException::class,
                                    "Cannot access KTOR HTTP Helper from Repository")
                            .build())
                }
            }



        }

        dbRepoFileSpec.addType(dbRepoType.build())
        return dbRepoFileSpec.build()
    }




    fun generateDaoRepositoryClass(daoTypeElement: TypeElement,
                                   pagingBoundaryCallbackEnabled: Boolean = false): FileSpec{
        val repoImplFile = FileSpec.builder(pkgNameOfElement(daoTypeElement, processingEnv),
                "${daoTypeElement.simpleName}_${SUFFIX_REPOSITORY}")
        repoImplFile.addImport("com.ustadmobile.door", "DoorDbType")

        val syncableEntitiesOnDao = syncableEntitiesOnDao(daoTypeElement.asClassName(),
                processingEnv)


        val repoClassSpec = newRepositoryClassBuilder(daoTypeElement.asClassName(),
                syncableEntitiesOnDao.isNotEmpty())


        var pagingBoundarySourceToKeyMap = null as PropertySpec?

        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv).forEach { daoSubEl ->
            if (daoSubEl.kind != ElementKind.METHOD)
                return@forEach

            val daoMethodEl = daoSubEl as ExecutableElement

            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType

            // The return type of the method - e.g. List<Entity>, LiveData<List<Entity>>, String, etc.
            val returnTypeResolved = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()
            val resultType = resolveQueryResultType(returnTypeResolved)
            val entityType = resolveEntityFromResultType(resultType)
            val incSyncableHttpRequest = !isUpdateDeleteOrInsertMethod(daoSubEl) && resultType != UNIT
                    && entityType is ClassName && findSyncableEntities(entityType, processingEnv).isNotEmpty()


            var repoMethodType = if(daoMethodEl.getAnnotation(Repository::class.java) != null) {
                daoMethodEl.getAnnotation(Repository::class.java).methodType
            }else {
                Repository.METHOD_AUTO
            }

            if(repoMethodType == Repository.METHOD_AUTO) {
                repoMethodType = when {
                    incSyncableHttpRequest -> Repository.METHOD_SYNCABLE_GET
                    else -> Repository.METHOD_DELEGATE_TO_DAO
                }
            }

            //TODO: tidy up forcenullable so this is not violating the DRY principle
            val (overrideFunSpec, daoFunSpec) = (0..1).map {overrideAndConvertToKotlinTypes(daoMethodEl,
                    daoTypeElement.asType() as DeclaredType, processingEnv,
                    forceNullableReturn = isNullableResultType(returnTypeResolved),
                    forceNullableParameterTypeArgs = isLiveData(returnTypeResolved)
                            && isNullableResultType((returnTypeResolved as ParameterizedTypeName).typeArguments[0])) }
                    .zipWithNext()[0]

            daoFunSpec.addAnnotations(daoMethodEl.annotationMirrors.map { AnnotationSpec.get(it) })

            var generateBoundaryCallback = false
            if(pagingBoundaryCallbackEnabled && repoMethodType == Repository.METHOD_SYNCABLE_GET
                    && returnTypeResolved is ParameterizedTypeName
                    && returnTypeResolved.rawType == DataSource.Factory::class.asClassName()) {
                if(pagingBoundarySourceToKeyMap == null) {
                    pagingBoundarySourceToKeyMap = PropertySpec.builder(
                            DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME,
                            BOUNDARY_CALLBACK_MAP_CLASSNAME)
                            .initializer("%T()", WeakHashMap::class)
                            .build()
                    repoClassSpec.addProperty(pagingBoundarySourceToKeyMap!!)
                    val typeVarName = TypeVariableName("T")
                    val boundaryCallbackTypeName = BOUNDARY_CALLBACK_CLASSNAME.parameterizedBy(typeVarName).copy(nullable = true)
                    repoClassSpec.addSuperinterface(ClassName("com.ustadmobile.door",
                            "DoorBoundaryCallbackProvider"))
                            .addFunction(FunSpec.builder("getBoundaryCallback")
                                    .addAnnotation(AnnotationSpec.builder(Suppress::class)
                                            .addMember("%S", "UNCHECKED_CAST")
                                            .build())
                                .addTypeVariable(typeVarName)
                                .addParameter("dataSource", DataSource.Factory::class.asClassName().parameterizedBy(INT, typeVarName))
                                .addModifiers(KModifier.OVERRIDE)
                                .returns(boundaryCallbackTypeName)
                                .addCode("return $DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME[dataSource] as %T\n",
                                        boundaryCallbackTypeName)
                                .build())
                }

                generateBoundaryCallback = true
                repoMethodType = Repository.METHOD_DELEGATE_TO_DAO
            }

            val codeBlock = CodeBlock.builder()

            val daoFunSpecBuilt = daoFunSpec.build()
            when(repoMethodType) {
                Repository.METHOD_SYNCABLE_GET -> {
                    codeBlock.add(generateRepositoryGetSyncableEntitiesFun(daoFunSpecBuilt,
                            daoTypeElement.simpleName.toString(), addReturnDaoResult = !generateBoundaryCallback))
                }
                Repository.METHOD_DELEGATE_TO_DAO -> {
                    if(generateBoundaryCallback) {
                        daoFunSpec.addParameter(PARAM_NAME_LIMIT, INT)
                        codeBlock.add("val _dataSource = ")
                                .addDelegateFunctionCall("_dao", daoFunSpecBuilt).add("\n")
                                .add("val $PARAM_NAME_LIMIT = 50\n")
                                .add(CodeBlock.builder()
                                        .add(generateRepositoryGetSyncableEntitiesFun(daoFunSpec.build(),
                                                daoTypeElement.simpleName.toString(),
                                                generateGlobalScopeLaunchBlockForLiveDataTypes = false,
                                                addReturnDaoResult = false,
                                                autoRetryEmptyMirrorResult = true))
                                        .build())

                        codeBlock.add("$DATASOURCEFACTORY_TO_BOUNDARYCALLBACK_VARNAME[_dataSource] = %T(_loadHelper)\n",
                                BOUNDARY_CALLBACK_CLASSNAME.parameterizedBy(entityType))
                                .add("return _dataSource\n")
                    }else {
                        codeBlock.add(generateRepositoryDelegateToDaoFun(daoFunSpec.build()))
                    }
                }

                Repository.METHOD_DELEGATE_TO_WEB -> {
                    codeBlock.beginControlFlow("val _loaderHelper = %T(_repo,·uri·=·%S)",
                            RepositoryLoadHelper::class,
                            "${daoTypeElement.simpleName}/${daoFunSpecBuilt.name}")
                            .add("_endpointToTry -> \n")
                        .add(generateKtorRequestCodeBlockForMethod(
                            httpEndpointVarName = "_endpointToTry",
                            daoName = daoTypeElement.simpleName.toString(),
                            dbPathVarName = "_dbPath",
                            methodName = daoFunSpecBuilt.name,
                            httpResultType = resultType,
                            requestBuilderCodeBlock = CodeBlock.of("%M(%S, _clientId)\n",
                                    MemberName("io.ktor.client.request", "header"),
                                    "X-nid"),
                            params = daoFunSpecBuilt.parameters))
                        .add("_httpResult\n")
                        .endControlFlow()

                    if(returnTypeResolved != UNIT) {
                        codeBlock.add("return ")
                    }
                    codeBlock.add("_loaderHelper.doRequest()\n")
                }
            }

            overrideFunSpec.addCode(codeBlock.build())
            repoClassSpec.addFunction(overrideFunSpec.build())
        }

        val attachmentFuns =daoTypeElement.enclosedElements
                .filter { it.getAnnotation(GetAttachmentData::class.java) != null
                        || it.getAnnotation(SetAttachmentData::class.java) != null}

        attachmentFuns.forEach {
            val funEl = it as ExecutableElement
            val funElResolved = processingEnv.typeUtils.asMemberOf(
                    daoTypeElement.asType() as DeclaredType, funEl) as ExecutableType
            val entityParam = funEl.parameters[0]

            val overridingFunSpec = overrideAndConvertToKotlinTypes(funEl,
                    daoTypeElement.asType() as DeclaredType, processingEnv)

            val entityTypeMirror = entityTypeFromFirstParam(funEl, daoTypeElement.asType() as DeclaredType,
                    processingEnv)
            val entityTypeEl = processingEnv.typeUtils.asElement(entityTypeMirror) as TypeElement
            val pkEl = entityTypeEl.enclosedElements
                    .first { it.getAnnotation(PrimaryKey::class.java) != null}
            val codeBlock = CodeBlock.builder()
            if(funEl.getAnnotation(SetAttachmentData::class.java) != null) {
                val dataParam = funEl.parameters[1]
                codeBlock.add("val _entityAttachmentsDir = %T(_attachmentsDir, %S)\n", File::class,
                        entityTypeEl.simpleName)
                        .add("val _destFile = File(_entityAttachmentsDir, ${entityParam.simpleName}.${pkEl.simpleName}.toString())\n")
                        .beginControlFlow("if(!_entityAttachmentsDir.exists())")
                        .add("_entityAttachmentsDir.mkdirs()\n")
                        .endControlFlow()
                        .add("%T(${dataParam.simpleName}).%M(_destFile, overwrite = true)\n",
                            File::class, MemberName("kotlin.io", "copyTo"))
            }else if(funEl.getAnnotation(GetAttachmentData::class.java) != null) {
                codeBlock.add("return File(_attachmentsDir, %S + %T.separator + ${entityParam.simpleName}.${pkEl.simpleName}.toString()).absolutePath\n",
                        entityTypeEl.simpleName, File::class)
            }

            overridingFunSpec.addCode(codeBlock.build())
            repoClassSpec.addFunction(overridingFunSpec.build())
        }

        repoImplFile.addType(repoClassSpec.build())
        return repoImplFile.build()
    }

    /**
     * Generates a repository method that will delegate to the DAO. If this is an Insert, Delete, or
     * Update method and the parameter type is syncable, then the last changed by field on the
     * syncable entity will be updated
     */
    fun generateRepositoryDelegateToDaoFun(daoFunSpec: FunSpec): CodeBlock {
        val codeBlock = CodeBlock.builder()
        if(isUpdateDeleteOrInsertMethod(daoFunSpec)) {
            val entityParam = daoFunSpec.parameters[0]
            val entityType = resolveEntityFromResultType(daoFunSpec.parameters[0].type) as ClassName
            val lastChangedByField = processingEnv.elementUtils.getTypeElement(entityType.canonicalName)
                    .enclosedElements.firstOrNull { it.kind == ElementKind.FIELD && it.getAnnotation(LastChangedBy::class.java) != null}

            if(lastChangedByField != null) {
                if(isListOrArray(entityParam.type)) {
                    codeBlock.add("${entityParam.name}.forEach { it.${lastChangedByField.simpleName} = _clientId }\n")
                }else {
                    codeBlock.add("${entityParam.name}.${lastChangedByField.simpleName} = _clientId\n")
                }
            }
        }

        if(daoFunSpec.returnType != UNIT)
            codeBlock.add("val _result = ")

        codeBlock.add("_dao.${daoFunSpec.name}(")
                .add(daoFunSpec.parameters.filter { !isContinuationParam(it.type)}.joinToString { it.name })
                .add(")\n")

        if(daoFunSpec.returnType != UNIT)
            codeBlock.add("return _result\n")

        return codeBlock.build()
    }


    companion object {
        const val SUFFIX_REPOSITORY = "Repo"

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

        val BOUNDARY_CALLBACK_CLASSNAME = ClassName("com.ustadmobile.door", "" +
                "RepositoryBoundaryCallback")

        val BOUNDARY_CALLBACK_MAP_CLASSNAME = WeakHashMap::class.asClassName().parameterizedBy(
                DataSource.Factory::class.asClassName().parameterizedBy(INT, STAR),
                BOUNDARY_CALLBACK_CLASSNAME.parameterizedBy(STAR))

    }



}