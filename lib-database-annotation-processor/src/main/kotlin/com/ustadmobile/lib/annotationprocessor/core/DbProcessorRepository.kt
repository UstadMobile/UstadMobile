package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
import com.ustadmobile.door.DoorDatabaseSyncRepository
import com.ustadmobile.door.annotation.Repository
import kotlinx.coroutines.GlobalScope
import kotlin.reflect.KClass

internal fun newRepositoryClassBuilder(daoType: ClassName, addSyncHelperParam: Boolean = false): TypeSpec.Builder {
    val repoClassSpec = TypeSpec.classBuilder("${daoType.simpleName}_${DbProcessorRepository.SUFFIX_REPOSITORY}")
            .addProperty(PropertySpec.builder("_dao",
                    daoType).initializer("_dao").build())
            .addProperty(PropertySpec.builder("_httpClient",
                    HttpClient::class).initializer("_httpClient").build())
            .addProperty(PropertySpec.builder("_clientId", Int::class)
                    .initializer("_clientId").build())
            .addProperty(PropertySpec.builder("_endpoint", String::class)
                    .initializer("_endpoint").build())
            .superclass(daoType)

    val primaryConstructorFn = FunSpec.constructorBuilder()
            .addParameter("_dao", daoType)
            .addParameter("_httpClient", HttpClient::class)
            .addParameter("_clientId", Int::class)
            .addParameter("_endpoint", String::class)

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
                    syncDaoMode = REPO_SYNCABLE_DAO_CONSTRUCT), AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            writeFileSpecToOutputDirs(generateDbRepositoryClass(dbTypeEl as TypeElement,
                    syncDaoMode = REPO_SYNCABLE_DAO_FROMDB, overrideClearAllTables = false,
                    overrideSyncDao = true, overrideOpenHelper = true),
                    AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT)
        }

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        for(daoElement in daos) {
            val daoTypeEl = daoElement as TypeElement
            val daoFileSpec = generateDaoRepositoryClass(daoTypeEl)
            writeFileSpecToOutputDirs(daoFileSpec, AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            writeFileSpecToOutputDirs(daoFileSpec, AnnotationProcessorWrapper.OPTION_ANDROID_OUTPUT)
        }

        return true
    }


    fun generateDbRepositoryClass(dbTypeElement: TypeElement,
                                  syncDaoMode: Int = REPO_SYNCABLE_DAO_CONSTRUCT,
                                  overrideClearAllTables: Boolean = true,
                                  overrideSyncDao: Boolean = false,
                                  overrideOpenHelper: Boolean = false): FileSpec {
        val dbRepoFileSpec = FileSpec.builder(pkgNameOfElement(dbTypeElement, processingEnv),
                "${dbTypeElement.simpleName}_$SUFFIX_REPOSITORY")


        val dbRepoType = TypeSpec.classBuilder("${dbTypeElement.simpleName}_$SUFFIX_REPOSITORY")
                .superclass(dbTypeElement.asClassName())
                .primaryConstructor(FunSpec.constructorBuilder()
                        .addParameter(ParameterSpec.builder("_db", dbTypeElement.asClassName() ).build())
                        .addParameter(ParameterSpec.builder("_endpoint", String::class.asClassName()).build())
                        .addParameter("_accessToken", String::class)
                        .addParameter(ParameterSpec.builder("_httpClient", HttpClient::class.asClassName()).build())
                        .build())
                .addProperties(listOf(
                        PropertySpec.builder("_db",
                            dbTypeElement.asClassName()).initializer("_db").build(),
                        PropertySpec.builder("_endpoint",
                                String::class.asClassName()).initializer("_endpoint").build(),
                        PropertySpec.builder("_accessToken", String::class)
                                .initializer("_accessToken").build(),
                        PropertySpec.builder("_httpClient",
                            HttpClient::class.asClassName()).initializer("_httpClient").build()
                ))
                .addFunction(FunSpec.builder("clearAllTables")
                        .addModifiers(KModifier.OVERRIDE)
                        .addCode("throw %T(%S)\n", IllegalAccessException::class, "Cannot use a repository to clearAllTables!")
                        .build())

        if(overrideClearAllTables) {
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
            dbRepoType.addFunction(FunSpec.builder("createOpenHelper")
                    .addParameter("config", ClassName("androidx.room", "DatabaseConfiguration"))
                    .returns(ClassName("androidx.sqlite.db", "SupportSQLiteOpenHelper"))
                    .addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
                    .addCode("throw IllegalAccessException(%S)\n", "Cannot use open helper on repository")
                    .build())
            dbRepoType.addFunction(FunSpec.builder("createInvalidationTracker")
                    .returns(ClassName("androidx.room", "InvalidationTracker"))
                    .addModifiers(KModifier.OVERRIDE, KModifier.PROTECTED)
                    .addCode("throw IllegalAccessException(%S)\n", "Cannot use invalidationtracker on repository")
                    .build())
        }


        if(isSyncableDb(dbTypeElement, processingEnv)) {
            val syncableDaoClassName = ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                    "${dbTypeElement.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}")
            val syncDaoProperty = PropertySpec.builder("_syncDao", syncableDaoClassName)
            if(syncDaoMode == REPO_SYNCABLE_DAO_CONSTRUCT) {
                syncDaoProperty.delegate("lazy {%T(_db) }",
                        ClassName(pkgNameOfElement(dbTypeElement, processingEnv),
                                "${dbTypeElement.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_IMPL}"))
            }else if(syncDaoMode == REPO_SYNCABLE_DAO_FROMDB) {
                syncDaoProperty.delegate("lazy {_db._syncDao() }")
            }

            dbRepoType.addProperty(syncDaoProperty.build())

            dbRepoType.addProperty(PropertySpec.builder("_clientId", INT)
                    .delegate("lazy { _syncDao._findSyncNodeClientId() }").build())

            dbRepoType.addProperty(PropertySpec.builder("master", BOOLEAN)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(FunSpec.getterBuilder().addCode("return _db.master").build())
                    .build())

            val repoImplClassName = ClassName(dbTypeElement.asClassName().packageName,
                    "${syncableDaoClassName.simpleName}_$SUFFIX_REPOSITORY")
            dbRepoType.addProperty(PropertySpec
                    .builder("_${syncableDaoClassName.simpleName}", repoImplClassName)
                    .delegate("lazy { %T(_syncDao, _httpClient, _clientId, _endpoint) }", repoImplClassName).build())
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

            var daoFromDbGetter = ""
            val daoTypeEl = processingEnv.typeUtils.asElement(it.returnType) as TypeElement?
            if(daoTypeEl == null)
                return@forEach

            if(it.simpleName.toString().startsWith("get")) {
                daoFromDbGetter += it.simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + it.simpleName.substring(4)
            }else {
                daoFromDbGetter += "${it.simpleName}()"
            }

            val repoImplClassName = ClassName(pkgNameOfElement(daoTypeEl, processingEnv),
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


        val repoClassSpec = newRepositoryClassBuilder(daoTypeElement.asClassName(),
                syncableEntitiesOnDao.isNotEmpty())


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

            val codeBlock = CodeBlock.builder()

            when(repoMethodType) {
                Repository.METHOD_SYNCABLE_GET ->
                        codeBlock.add(generateRepositoryGetSyncableEntitiesFun(daoFunSpec.build(),
                        daoTypeElement.simpleName.toString()))
                Repository.METHOD_DELEGATE_TO_DAO ->
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

    }



}