package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Database
import androidx.room.PrimaryKey
import androidx.room.Query
import com.squareup.kotlinpoet.*
import com.ustadmobile.door.annotation.*
import java.io.File
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy


fun getEntitySyncTracker(entityEl: Element, processingEnv: ProcessingEnvironment): TypeMirror? {
    val syncEntityAnnotationIndex = entityEl.annotationMirrors.map {processingEnv.typeUtils.asElement(it.annotationType) as TypeElement }
            .indexOfFirst { it.qualifiedName.toString() == "com.ustadmobile.door.annotation.SyncableEntity" }
    if(syncEntityAnnotationIndex == -1)
        return null

    val annotationValue = entityEl.annotationMirrors[syncEntityAnnotationIndex].elementValues
            .filter { it.key.simpleName.toString() == "syncTrackerEntity" }.values.toList()[0]
    return annotationValue.value as TypeMirror
}

class DbProcessorSync: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)

        val (abstractOutputArg, implOutputArg) = Pair(processingEnv.options[OPTION_ABSTRACT_OUTPUT_DIR],
                processingEnv.options[OPTION_IMPL_OUTPUT_DIR])
        val (abstractOutputDir, implOutputDir) = listOf(abstractOutputArg, implOutputArg)
                .map {
                    if (it == null || it == "filer") {
                        processingEnv.options["kapt.kotlin.generated"]!!
                    } else {
                        implOutputArg!!
                    }
                }.zipWithNext()[0]
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)

        for(dbTypeEl in dbs) {
            val (abstractFileSpec, implFileSpec) = generateSyncDaoInterfaceAndImpl(dbTypeEl as TypeElement)

            abstractFileSpec.writeTo(File(abstractOutputDir))
            implFileSpec.writeTo(File(implOutputDir))
        }

        return true
    }

    /**
     *
     * @return Pair of FileSpecs: first = the abstract DAO filespec, the second one is the implementation
     */
    fun generateSyncDaoInterfaceAndImpl(dbType: TypeElement): Pair<FileSpec, FileSpec> {
        val abstractDaoSimpleName = "${dbType.simpleName}$SUFFIX_SYNCDAO_ABSTRACT"
        val abstractDaoClassName = ClassName(pkgNameOfElement(dbType, processingEnv),
                abstractDaoSimpleName)
        val abstractDaoTypeSpec = TypeSpec.classBuilder(abstractDaoSimpleName)
                .addAnnotation(Dao::class.asClassName())
                .addModifiers(KModifier.ABSTRACT)
        val abstractFileSpec = FileSpec.builder(pkgNameOfElement(dbType, processingEnv),
                abstractDaoSimpleName)


        val implDaoSimpleName = "${dbType.simpleName}$SUFFIX_SYNCDAO_IMPL"
        val implDaoClassName = ClassName(pkgNameOfElement(dbType, processingEnv),
                implDaoSimpleName)
        val implDaoTypeSpec = jdbcDaoTypeSpecBuilder(implDaoSimpleName, abstractDaoClassName)
        val implFileSpec = FileSpec.builder(pkgNameOfElement(dbType, processingEnv),
                implDaoSimpleName)

        entityTypesOnDb(dbType, processingEnv).filter { it.getAnnotation(SyncableEntity::class.java) != null}.forEach {entityType ->
            val entityPkField = entityType.enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null }
            val entityLastModifiedField = entityType.enclosedElements.first { it.getAnnotation(LastChangedBy::class.java) != null}
            val entitySyncTracker = getEntitySyncTracker(entityType, processingEnv)
            val entitySyncTrackerEl = processingEnv.typeUtils.asElement(entitySyncTracker)
            val entityLocalCsnFieldEl = entityType.enclosedElements
                    .first { it.getAnnotation(LocalChangeSeqNum::class.java) != null}
            val entitySyncTrackCsnField = entitySyncTrackerEl.enclosedElements
                    .first { it.getAnnotation(TrackerChangeSeqNum::class.java) != null }
            val entitySyncTrackerPkField = entitySyncTrackerEl.enclosedElements
                    .first {it.getAnnotation(TrackerEntityPrimaryKey::class.java) != null}
            val entitySyncTrackerDestField = entitySyncTrackerEl.enclosedElements
                    .first {it.getAnnotation(TrackDestId::class.java) != null}
            val entityListClassName = List::class.asClassName().parameterizedBy(entityType.asClassName())

            //Generate the find local unsent changes function for this entity
            val findLocalUnsentSql = "SELECT * FROM " +
                    "(SELECT * FROM ${entityType.simpleName} ) AS ${entityType.simpleName} " +
                    "WHERE " +
                    "${entityLastModifiedField.simpleName} = (SELECT dbNodeId FROM DoorDatabaseSyncInfo) AND " +
                    "(${entityType.simpleName}.$entityLocalCsnFieldEl > " +
                    "COALESCE((SELECT ${entitySyncTrackCsnField.simpleName} FROM ${entitySyncTrackerEl.simpleName} " +
                    "WHERE ${entitySyncTrackerPkField.simpleName} = ${entityType.simpleName}.${entityPkField.simpleName} " +
                    "AND ${entitySyncTrackerDestField.simpleName} = :destClientId), 0)" +
                    ") LIMIT :limit"


            val findUnsentParamsList = listOf(ParameterSpec.builder("destClientId", INT).build(),
                    ParameterSpec.builder("limit", INT).build())
            val (abstractLocalUnsentChangeFun, implLocalUnsetChangeFun) =
                    generateAbstractAndImplQueryFunSpecs(findLocalUnsentSql,
                            "_find${entityType.simpleName}LocalUnsentChanges",
                            entityListClassName, findUnsentParamsList)
            abstractDaoTypeSpec.addFunction(abstractLocalUnsentChangeFun)
            implDaoTypeSpec.addFunction(implLocalUnsetChangeFun)

            //Generate the find master unsent changes function for this entity
            val findMasterUnsentSql = "SELECT * FROM " +
                    "(SELECT * FROM ${entityType.simpleName} ) AS ${entityType.simpleName} " +
                    "WHERE ${entityLastModifiedField.simpleName}  != :destClientId AND " +
                    "(${entityType.simpleName}.$entityLocalCsnFieldEl > " +
                    "COALESCE((SELECT ${entitySyncTrackCsnField.simpleName} FROM ${entitySyncTrackerEl.simpleName} " +
                    "WHERE ${entitySyncTrackerPkField.simpleName} = ${entityType.simpleName}.${entityPkField.simpleName} " +
                    "AND ${entitySyncTrackerDestField.simpleName} = :destClientId), 0)" +
                    ") LIMIT :limit"

            val (abstractMasterUnsentChangeFun, implMasterUnsentChangeFun) =
                    generateAbstractAndImplQueryFunSpecs(findMasterUnsentSql,
                            "_find${entityType.simpleName}MasterUnsentChanges",
                            entityListClassName, findUnsentParamsList)
            abstractDaoTypeSpec.addFunction(abstractMasterUnsentChangeFun)
            implDaoTypeSpec.addFunction(implMasterUnsentChangeFun)
        }


        abstractFileSpec.addType(abstractDaoTypeSpec.build())
        implFileSpec.addType(implDaoTypeSpec.build())
        return Pair(abstractFileSpec.build(), implFileSpec.build())
    }

    private fun generateAbstractAndImplQueryFunSpecs(querySql: String,
                                             funName: String,
                                             returnType: TypeName,
                                             params: List<ParameterSpec>): Pair<FunSpec, FunSpec> {
        val funBuilders = (0..1).map {
            FunSpec.builder(funName)
                    .returns(returnType)
                    .addParameters(params)
        }

        funBuilders[0].addModifiers(KModifier.ABSTRACT)
        funBuilders[1].addModifiers(KModifier.OVERRIDE)

        funBuilders[0].addAnnotation(AnnotationSpec.builder(Query::class)
                .addMember("value = %S", querySql).build())

        funBuilders[1].addCode(generateQueryCodeBlock(returnType,
                params.map { it.name to it.type}.toMap(), querySql,
                null, null))
                .addCode("return _result\n")

        return Pair(funBuilders[0].build(), funBuilders[1].build())
    }


    companion object {

        const val OPTION_IMPL_OUTPUT_DIR = "door_sync_impl_output"

        const val OPTION_ABSTRACT_OUTPUT_DIR = "door_sync_interface_output"

        const val SUFFIX_SYNCDAO_ABSTRACT = "SyncDao"

        const val SUFFIX_SYNCDAO_IMPL = "SyncDaoImpl"


    }

}