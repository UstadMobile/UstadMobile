package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.ustadmobile.door.annotation.LastChangedBy
import io.ktor.client.HttpClient
import java.io.File
import java.util.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import com.ustadmobile.door.SyncableDoorDatabase
import com.ustadmobile.door.DoorLiveData
import kotlinx.coroutines.GlobalScope

class DbProcessorRepository: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)
        val outputArg = processingEnv.options[OPTION_OUTPUT_DIR]
        val outputDir = if(outputArg == null || outputArg == "filer") processingEnv.options["kapt.kotlin.generated"]!! else outputArg
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)

        for(dbTypeEl in dbs) {
            val dbFileSpec = generateDbRepositoryClass(dbTypeEl as TypeElement)
            dbFileSpec.writeTo(File(outputDir))
        }

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        for(daoElement in daos) {
            val daoTypeEl = daoElement as TypeElement
            val daoFileSpec = generateDaoRepositoryClass(daoTypeEl)
            daoFileSpec.writeTo(File(outputDir))
        }

        return true
    }


    fun generateDbRepositoryClass(dbTypeElement: TypeElement,
                                  syncDaoMode: Int = REPO_SYNCABLE_DAO_CONSTRUCT): FileSpec {
        val dbRepoFileSpec = FileSpec.builder(pkgNameOfElement(dbTypeElement, processingEnv),
                "${dbTypeElement.simpleName}_$SUFFIX_REPOSITORY")


        val dbRepoType = TypeSpec.classBuilder("${dbTypeElement.simpleName}_$SUFFIX_REPOSITORY")
                .superclass(dbTypeElement.asClassName())
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("_db", dbTypeElement.asClassName() ).build())
                        .addParameter(ParameterSpec.builder("_clientId", INT).build())
                        .addParameter(ParameterSpec.builder("_endpoint", String::class.asClassName()).build())
                        .addParameter(ParameterSpec.builder("_httpClient", HttpClient::class.asClassName()).build())
                        .build())
                .addProperties(listOf(
                        PropertySpec.builder("_db",
                            dbTypeElement.asClassName()).initializer("_db").build(),
                        PropertySpec.builder("_clientId",
                                INT).initializer("_clientId").build(),
                        PropertySpec.builder("_endpoint",
                                String::class.asClassName()).initializer("_endpoint").build(),
                        PropertySpec.builder("_httpClient",
                            HttpClient::class.asClassName()).initializer("_httpClient").build()
                ))
                .addFunction(FunSpec.builder("clearAllTables")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("throw %T(%S)\n", IllegalAccessException::class, "Cannot use a repository to clearAllTables!")
                        .build())
                .addFunction(FunSpec.builder("createAllTables")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("throw %T(%S)\n", IllegalAccessException::class, "Cannot use a repository to createAllTables!")
                        .build())

        val syncableDbType = processingEnv.elementUtils
                .getTypeElement(SyncableDoorDatabase::class.java.canonicalName).asType()
        if(processingEnv.typeUtils.isAssignable(dbTypeElement.asType(), syncableDbType)) {
            val syncableDaoClassName = ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                    "${dbTypeElement.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}")
            val syncDaoProperty = PropertySpec.builder("_syncDao", syncableDaoClassName)
            if(syncDaoMode == REPO_SYNCABLE_DAO_CONSTRUCT) {
                syncDaoProperty.delegate("lazy {%T(this) }",
                        ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                                "${dbTypeElement.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_IMPL}"))
            }
            dbRepoType.addProperty(syncDaoProperty.build())
        }

        methodsToImplement(dbTypeElement, dbTypeElement.asType() as DeclaredType, processingEnv)
            .filter{it.kind == ElementKind.METHOD }.map {it as ExecutableElement }.forEach {

            var daoFromDbGetter = ""
            val daoTypeEl = processingEnv.typeUtils.asElement(it.returnType) as TypeElement?
            if(daoTypeEl == null)
                return@forEach

            if(it.simpleName.toString().startsWith("get")) {
                daoFromDbGetter += it.simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + it.simpleName.substring(4)
            }else {
                daoFromDbGetter += "${it.simpleName}()"
            }

            val repoImplClassName = ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                    "${daoTypeEl.simpleName}_$SUFFIX_REPOSITORY")
            val syncDaoParam = if(syncableEntitiesOnDao(daoTypeEl.asClassName(), processingEnv).isNotEmpty()) {
                ", _syncDao"
            }else {
                ""
            }

            dbRepoType.addProperty(PropertySpec.builder("_${daoTypeEl.simpleName}",  daoTypeEl.asType().asTypeName())
                    .delegate("lazy { %T(_db.$daoFromDbGetter, _httpClient, _clientId, _endpoint $syncDaoParam) }",
                            repoImplClassName).build())

            if(it.simpleName.toString().startsWith("get")) {
                val propName = it.simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + it.simpleName.substring(4)
                val getterFunSpec = FunSpec.getterBuilder().addStatement("return _${daoTypeEl.simpleName}")
                dbRepoType.addProperty(PropertySpec.builder(propName, daoTypeEl.asType().asTypeName(),
                        KModifier.OVERRIDE).getter(getterFunSpec.build()).build())
            }else {
                dbRepoType.addFunction(FunSpec.overriding(it)
                        .addStatement("return _${daoTypeEl.simpleName}")
                        .build())
            }
        }

        dbRepoFileSpec.addType(dbRepoType.build())
        return dbRepoFileSpec.build()
    }




    fun generateDaoRepositoryClass(daoTypeElement: TypeElement): FileSpec {
        val repoImplFile = FileSpec.builder(pkgNameOfElement(daoTypeElement, processingEnv),
                "${daoTypeElement.simpleName}_${SUFFIX_REPOSITORY}")
        repoImplFile.addImport("com.ustadmobile.door", "DoorDbType")
        val syncableEntitiesOnDao = syncableEntitiesOnDao(daoTypeElement.asClassName(),
                processingEnv)


        val repoClassSpec = TypeSpec.classBuilder("${daoTypeElement.simpleName}_${SUFFIX_REPOSITORY}")
                .addProperty(PropertySpec.builder("_dao",
                        daoTypeElement.asType().asTypeName()).initializer("_dao").build())
                .addProperty(PropertySpec.builder("_httpClient",
                        HttpClient::class).initializer("_httpClient").build())
                .addProperty(PropertySpec.builder("_clientId", Int::class)
                        .initializer("_clientId").build())
                .addProperty(PropertySpec.builder("_endpoint", String::class)
                        .initializer("_endpoint").build())
                .superclass(daoTypeElement.asClassName())

        val primaryConstructorFn = FunSpec.constructorBuilder()
                .addParameter("_dao", daoTypeElement.asType().asTypeName())
                .addParameter("_httpClient", HttpClient::class)
                .addParameter("_clientId", Int::class)
                .addParameter("_endpoint", String::class)

        if(!syncableEntitiesOnDao.isNullOrEmpty()) {
            val syncHelperClassName = ClassName(pkgNameOfElement(daoTypeElement, processingEnv),
                    "${daoTypeElement.simpleName}_SyncHelper")
            primaryConstructorFn.addParameter("_syncHelper",
                    syncHelperClassName)
            repoClassSpec.addProperty(PropertySpec.builder("_syncHelper", syncHelperClassName)
                    .initializer("_syncHelper").build())
        }

        repoClassSpec.primaryConstructor(primaryConstructorFn.build())


        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv).forEach { daoSubEl ->
            if (daoSubEl.kind != ElementKind.METHOD)
                return@forEach

            val daoMethodEl = daoSubEl as ExecutableElement

            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType

            // The return type of the method - e.g. List<Entity>, LiveData<List<Entity>>, String, etc.
            val returnTypeResolved = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()
            val resultType = resolveQueryResultType(returnTypeResolved)
            val incSyncableHttpRequest = !isUpdateDeleteOrInsertMethod(daoSubEl) && resultType != UNIT

            //TODO: tidy up forcenullable so this is not violating the DRY principle
            val (overrideFunSpec, daoFunSpec) = (0..1).map {overrideAndConvertToKotlinTypes(daoMethodEl,
                    daoTypeElement.asType() as DeclaredType, processingEnv,
                    forceNullableReturn = isNullableResultType(returnTypeResolved),
                    forceNullableParameterTypeArgs = isLiveData(returnTypeResolved)
                            && isNullableResultType((returnTypeResolved as ParameterizedTypeName).typeArguments[0])) }
                    .zipWithNext()[0]

            daoFunSpec.addAnnotations(daoMethodEl.annotationMirrors.map { AnnotationSpec.get(it) })

            val codeBlock = CodeBlock.builder()


            if(incSyncableHttpRequest) {
                codeBlock.add(generateRepositoryGetSyncableEntitiesFun(daoFunSpec.build(),
                        daoTypeElement.simpleName.toString()))
            }else {
                codeBlock.add(generateRepositoryDelegateToDaoFun(daoFunSpec.build()))
            }

            overrideFunSpec.addCode(codeBlock.build())
            repoClassSpec.addFunction(overrideFunSpec.build())
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

    fun generateRepositoryGetSyncableEntitiesFun(daoFunSpec: FunSpec, daoName: String): CodeBlock {
        val codeBlock = CodeBlock.builder()
        val daoFunReturnType = daoFunSpec.returnType!!
        val resultType = resolveQueryResultType(daoFunReturnType)
        val isLiveDataOrDataSourceFactory = daoFunReturnType is ParameterizedTypeName
                && daoFunReturnType.rawType in
                listOf(DoorLiveData::class.asClassName(), DataSource.Factory::class.asClassName())

        if (KModifier.SUSPEND !in daoFunSpec.modifiers) {
            if(isLiveDataOrDataSourceFactory) {
                codeBlock.beginControlFlow("%T.%M",
                        GlobalScope::class, MemberName("kotlinx.coroutines", "launch"))
            }else {
                codeBlock.beginControlFlow("%M",
                        MemberName("kotlinx.coroutines", "runBlocking"))
            }
        }


        codeBlock.add(generateKtorRequestCodeBlockForMethod(
                daoName = daoName,
                methodName = daoFunSpec.name,
                httpResultType = resultType,
                requestBuilderCodeBlock = CodeBlock.of("%M(%S, _clientId)\n",
                    MemberName("io.ktor.client.request", "header"),
                        "X-nid"),
                params = daoFunSpec.parameters))
        codeBlock.add("val _requestId = _httpResponse.headers.get(%S)?.toInt() ?: -1\n",
                "X-reqid")

        codeBlock.add(generateReplaceSyncableEntityCodeBlock("_httpResult",
                afterInsertCode = {
                    CodeBlock.builder().beginControlFlow("_httpClient.%M<Unit>",
                            CLIENT_GET_MEMBER_NAME)
                            .beginControlFlow("url")
                            .add("%M(_endpoint)\n", MemberName("io.ktor.http", "takeFrom"))
                            .add("path(%S, %S)\n", daoName,
                                    "_update${SyncableEntityInfo(it, processingEnv).tracker.simpleName}Received")
                            .endControlFlow()
                            .add("%M(%S, _requestId)\n",
                                    MemberName("io.ktor.client.request", "parameter"),
                                    "reqId")
                            .endControlFlow()
                            .build()
                },
                resultType = resultType, processingEnv = processingEnv))

        if(KModifier.SUSPEND !in daoFunSpec.modifiers) {
            codeBlock.endControlFlow()
        }

        codeBlock.add("return _dao.${daoFunSpec.name}(")
                .add(daoFunSpec.parameters.filter { !isContinuationParam(it.type)}.joinToString { it.name })
                .add(")\n")

        return codeBlock.build()
    }


    companion object {
        const val SUFFIX_REPOSITORY = "Repo"

        const val OPTION_OUTPUT_DIR = "door_repo_output"

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

    }



}