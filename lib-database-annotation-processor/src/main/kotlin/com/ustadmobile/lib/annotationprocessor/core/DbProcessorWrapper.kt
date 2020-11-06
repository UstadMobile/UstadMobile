package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Database
import androidx.room.Dao
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorWrapper.Companion.SUFFIX_WRAPPER
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import com.ustadmobile.door.SyncableDoorDatabaseWrapper

fun newDaoWrapperTypeSpecBuilder(daoTypeElement: TypeElement) : TypeSpec.Builder {
    return TypeSpec.classBuilder(daoTypeElement.asClassNameWithSuffix(SUFFIX_WRAPPER))
            .superclass(daoTypeElement.asClassName())
            .primaryConstructor(FunSpec.constructorBuilder()
                    .addParameter("_dao", daoTypeElement.asClassName())
                    .build())
            .addProperty(PropertySpec.builder("_dao", daoTypeElement.asClassName(),
                    KModifier.PRIVATE)
                    .initializer("_dao")
                    .build())
}


fun TypeSpec.Builder.addAllDbWrapperDaoAccessorsForDb(dbTypeElement: TypeElement,
                                              processingEnv: ProcessingEnvironment,
                                              allKnownEntityTypesMap: Map<String, TypeElement>): TypeSpec.Builder {
    //iterate over all DAO getters
    dbTypeElement.allDbClassDaoGetters(processingEnv).forEach {daoGetter ->
        addWrapperAccessorFunction(daoGetter, processingEnv, allKnownEntityTypesMap)
    }

    return this
}

/**
 * Add a DAO accessor for a database wrapper (e.g. property or function). If the given DAO
 * has queries that modify a syncable entity, the return will be wrapped. If there are no such queries,
 * then the original DAO from the database will be returned by the generated code.
 */
fun TypeSpec.Builder.addWrapperAccessorFunction(daoGetter: ExecutableElement,
                                                processingEnv: ProcessingEnvironment,
                                                allKnownEntityTypesMap: Map<String, TypeElement>) : TypeSpec.Builder {
    val daoModifiesSyncableEntities = daoGetter.returnType.asTypeElement(processingEnv)
            ?.daoHasSyncableWriteMethods(processingEnv, allKnownEntityTypesMap) == true

    if(!daoModifiesSyncableEntities) {
        addAccessorOverride(daoGetter, CodeBlock.of("return _db.${daoGetter.accessAsPropertyOrFunctionInvocationCall()}\n"))
    }else {
        val daoType = daoGetter.returnType.asTypeElement(processingEnv)
                ?: throw IllegalStateException("Dao return type is not TypeElement")
        val wrapperClassName = daoType.asClassNameWithSuffix(SUFFIX_WRAPPER)
        addProperty(PropertySpec.builder("_${daoType.simpleName}",
                daoType.asClassName()).delegate(
                CodeBlock.builder().beginControlFlow("lazy ")
                        .add("%T(_db.${daoGetter.accessAsPropertyOrFunctionInvocationCall()})\n",
                                wrapperClassName)
                        .endControlFlow()
                        .build())
                .build())
        addAccessorOverride(daoGetter, CodeBlock.of("return _${daoType.simpleName}\n"))
    }

    return this
}


fun TypeSpec.Builder.addDaoFunctionDelegate(daoMethod: ExecutableElement,
        daoTypeEl: TypeElement, processingEnv: ProcessingEnvironment,
        allKnownEntityTypesMap: Map<String, TypeElement>) : TypeSpec.Builder {

    val methodResolved = daoMethod.asMemberOf(daoTypeEl, processingEnv)

    val overridingFunction = overrideAndConvertToKotlinTypes(daoMethod, daoTypeEl.asType() as DeclaredType,
            processingEnv,
            forceNullableReturn = methodResolved.suspendedSafeReturnType.isNullableAsSelectReturnResult)
            .build()

    if(daoMethod.isDaoMethodModifyingSyncableEntity(daoTypeEl, processingEnv, allKnownEntityTypesMap)) {
        addFunction(overridingFunction.toBuilder()
                .addCode("throw %T(%S)\n", IllegalStateException::class,
                        "Cannot use DB to modify syncable entity")
                .build())
    }else {
        addFunction(overridingFunction.toBuilder()
                .addCode(CodeBlock.builder()
                        .apply {
                            if(overridingFunction.returnType != null && overridingFunction.returnType != UNIT) {
                                add("return ")
                            }
                        }
                        .addDelegateFunctionCall("_dao", overridingFunction)
                        .build())
                .build())
    }

    return this
}

fun TypeSpec.Builder.addAllDaoFunctionDelegates(daoTypeEl: TypeElement,
                                                processingEnv: ProcessingEnvironment,
                                                allKnownEntityTypesMap: Map<String, TypeElement>) : TypeSpec.Builder {

    daoTypeEl.allOverridableMethods(processingEnv).forEach {daoMethodEl ->
        addDaoFunctionDelegate(daoMethodEl, daoTypeEl, processingEnv, allKnownEntityTypesMap)
    }

    return this
}

/**
 * Add a TypeSpec representing a database wrapper for the given database to the filespec
 */
fun FileSpec.Builder.addDbWrapperTypeSpec(dbTypeEl: TypeElement,
                                         processingEnv: ProcessingEnvironment,
                                         allKnownEntityTypesMap: Map<String, TypeElement>): FileSpec.Builder {
    val dbClassName = dbTypeEl.asClassName()
    addType(
            TypeSpec.classBuilder("${dbTypeEl.simpleName}$SUFFIX_WRAPPER")
                    .superclass(dbClassName)
                    .addSuperinterface(SyncableDoorDatabaseWrapper::class.asClassName()
                            .parameterizedBy(dbClassName))
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameter("_db", dbClassName)
                            .build())
                    .addProperty(PropertySpec.builder("_db", dbClassName, KModifier.PRIVATE)
                            .initializer("_db").build())
                    .addDbVersionProperty(dbTypeEl)
                    .addAllDbWrapperDaoAccessorsForDb(dbTypeEl, processingEnv,
                            allKnownEntityTypesMap)
                    .addProperty(PropertySpec.builder("realDatabase", dbTypeEl.asClassName())
                            .addModifiers(KModifier.OVERRIDE)
                            .getter(FunSpec.getterBuilder().addCode("return _db\n")
                                    .build())
                            .build())
                    .addFunction(FunSpec.builder("clearAllTables")
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode("_db.clearAllTables()\n")
                            .build())
                    .addFunction(FunSpec.builder("createAllTables")
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode("_db.createAllTables()\n")
                            .build())
                    .apply {
                        if(dbTypeEl.isDbSyncable(processingEnv)) {
                            addProperty(PropertySpec.builder("master", Boolean::class)
                                    .addModifiers(KModifier.OVERRIDE)
                                    .getter(FunSpec.getterBuilder()
                                            .addCode("return _db.master\n")
                                            .build())
                                    .build())
                        }
                    }
                    .build())
            .build()

    return this
}

/**
 *
 */
private fun TypeElement.daoHasSyncableWriteMethods(
        processingEnv: ProcessingEnvironment, allKnownEntityTypesMap: Map<String, TypeElement>): Boolean {

    return ancestorsAsList(processingEnv).any {
        it.allDaoClassModifyingQueryMethods().any { daoMethodEl ->
            daoMethodEl.isDaoMethodModifyingSyncableEntity(this, processingEnv, allKnownEntityTypesMap)
        }
    }
}

/**
 * Generates the DbWrapper class to prevent accidental usage of insert, update, and delete functions
 * for syncable entities on the database which must be routed via the repository for sync to work.
 */
class DbProcessorWrapper: AbstractDbProcessor()  {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)
        roundEnv.getElementsAnnotatedWith(Database::class.java).map { it as TypeElement }.forEach {dbTypeEl ->
            //jvm version
            if(dbTypeEl.isDbSyncable(processingEnv)) {
                writeFileSpecToOutputDirs(
                        FileSpec.builder(dbTypeEl.qualifiedPackageName(processingEnv),
                                "${dbTypeEl.simpleName}$SUFFIX_WRAPPER")
                                .addDbWrapperTypeSpec(dbTypeEl, processingEnv, allKnownEntityTypesMap)
                                .build(),
                        AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            }
        }

        roundEnv.getElementsAnnotatedWith(Dao::class.java).map { it as TypeElement }.forEach {daoTypeEl ->
            if(daoTypeEl.daoHasSyncableWriteMethods(processingEnv, allKnownEntityTypesMap)) {
                writeFileSpecToOutputDirs(generateDaoFileSpec(daoTypeEl),
                        AnnotationProcessorWrapper.OPTION_JVM_DIRS)
            }
        }

        return true
    }

    fun generateDaoFileSpec(daoTypeEl: TypeElement) : FileSpec {
        return FileSpec.builder(daoTypeEl.packageName, "${daoTypeEl.simpleName}$SUFFIX_WRAPPER")
                .addType(newDaoWrapperTypeSpecBuilder(daoTypeEl)
                        .addAllDaoFunctionDelegates(daoTypeEl, processingEnv, allKnownEntityTypesMap)
                        .build())
                .build()
    }



    companion object {
        const val SUFFIX_WRAPPER = "_DbWrapper"
    }

}