package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import androidx.room.*
import com.squareup.kotlinpoet.*
import java.lang.RuntimeException
import java.sql.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.Diagnostic
import org.sqlite.SQLiteDataSource
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.*
import com.ustadmobile.door.annotation.*
import io.ktor.client.response.HttpResponse
import java.util.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import com.ustadmobile.door.DoorLiveData
import kotlinx.coroutines.GlobalScope
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.PreparedStatementArrayProxy
import com.ustadmobile.door.EntityInsertionAdapter
import com.ustadmobile.door.SyncableDoorDatabase
import org.apache.commons.text.StringEscapeUtils
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import io.ktor.http.HttpStatusCode


fun isUpdateDeleteOrInsertMethod(methodEl: Element)
        = listOf(Update::class.java, Delete::class.java, Insert::class.java).any { methodEl.getAnnotation(it) != null }

fun isUpdateDeleteOrInsertMethod(funSpec: FunSpec) =
        funSpec.annotations.any { it.className in listOf(Insert::class.asClassName(),
                    Delete::class.asClassName(), Update::class.asClassName())}


fun isModifyingQueryMethod(methodEl: Element) : Boolean {
    if(isUpdateDeleteOrInsertMethod(methodEl)) {
        return true
    }

    val queryAnnotation = methodEl.getAnnotation(Query::class.java)
    val queryTrimmed = queryAnnotation?.value?.trim()
    if(queryTrimmed != null && (queryTrimmed.startsWith("UPDATE", ignoreCase = true)
            || queryTrimmed.startsWith("DELETE", ignoreCase = true))){
        return true
    }

    return false
}

val SQL_NUMERIC_TYPES = listOf(BYTE, SHORT, INT, LONG, FLOAT, DOUBLE)

fun defaultSqlQueryVal(typeName: TypeName) = if(typeName in SQL_NUMERIC_TYPES) {
    "0"
}else if(typeName == BOOLEAN){
    "false"
}else {
    "null"
}

/**
 * Get a list of all the syncable entities associated with a given POJO. This will look at parent
 * classes and embedded fields
 *
 * @param entityType the POJO to inspect to find syncable entities. This will inspect the class
 * itself, the parent classes, and any fields annotated with Embedded
 * @param processingEnv the annotation processor environment
 * @param embedPath the current embed path. This function is designed to work recursively.
 *
 * @return A map in the form of a list of the embedded variables to the syncable entity
 * e.g.
 * given
 *
 * <pre>
 * class SyncableEntityWithOtherSyncableEntity(@Embedded var embedded: OtherSyncableEntity?): SyncableEntity()
 * </pre>
 * This will result in:
 * <pre>
 * {
 * [] -> SyncableEntity,
 * ['embedded'] -> OtherSyncableEntity
 * }
 * </pre>
 */
fun findSyncableEntities(entityType: ClassName, processingEnv: ProcessingEnvironment,
                         embedPath: List<String> = listOf()): Map<List<String>, ClassName> {
    if(entityType in QUERY_SINGULAR_TYPES)
        return mapOf()

    val entityTypeEl = processingEnv.elementUtils.getTypeElement(entityType.canonicalName)
    val syncableEntityList = mutableMapOf<List<String>, ClassName>()
    ancestorsToList(entityTypeEl, processingEnv).forEach {
        if(it.getAnnotation(SyncableEntity::class.java) != null)
            syncableEntityList.put(embedPath, it.asClassName())

        it.enclosedElements.filter { it.getAnnotation(Embedded::class.java) != null}.forEach {
            val subEmbedPath = mutableListOf(*embedPath.toTypedArray()) + "${it.simpleName}"
            syncableEntityList.putAll(findSyncableEntities(it.asType().asTypeName() as ClassName,
                    processingEnv, subEmbedPath))
        }
    }

    return syncableEntityList.toMap()
}

fun jdbcDaoTypeSpecBuilder(simpleName: String, superTypeName: TypeName) = TypeSpec.classBuilder(simpleName)
        .primaryConstructor(FunSpec.constructorBuilder().addParameter("_db",
                DoorDatabase::class).build())
        .addProperty(PropertySpec.builder("_db", DoorDatabase::class).initializer("_db").build())
        .superclass(superTypeName)


fun daosOnDb(dbType: ClassName, processingEnv: ProcessingEnvironment, excludeDbSyncDao: Boolean = false): List<ClassName> {
    val dbTypeEl = processingEnv.elementUtils.getTypeElement(dbType.canonicalName) as TypeElement
    processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "DbProcessorSync: daosOnDb: ${dbType.simpleName}")
    val daoList = dbTypeEl.enclosedElements
            .filter { it.kind == ElementKind.METHOD && Modifier.ABSTRACT in it.modifiers}
            .map { it as ExecutableElement }
            .fold(mutableListOf<ClassName>(), {list, subEl ->
        list.add(subEl.returnType.asTypeName() as ClassName)
        list
    })

    return if(excludeDbSyncDao) {
        daoList.filter { it.simpleName != "${dbType.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}" }
    }else {
        daoList
    }
}

fun syncableEntityTypesOnDb(dbType: TypeElement, processingEnv: ProcessingEnvironment) =
        entityTypesOnDb(dbType, processingEnv).filter { it.getAnnotation(SyncableEntity::class.java) != null}

fun syncableEntitiesOnDao(daoClass: ClassName, processingEnv: ProcessingEnvironment): List<ClassName> {
    val daoType = processingEnv.elementUtils.getTypeElement(daoClass.canonicalName)
    val syncableEntitiesOnDao = mutableSetOf<ClassName>()
    daoType.enclosedElements.filter { it.getAnnotation(Query::class.java) != null}.forEach {methodEl ->
        //TODO: Add rest accessible methods
        val querySql = methodEl.getAnnotation(Query::class.java).value.toLowerCase(Locale.ROOT).trim()
        if(!(querySql.startsWith("update") || querySql.startsWith("delete"))) {
            val methodResolved = processingEnv.typeUtils
                    .asMemberOf(daoType.asType() as DeclaredType, methodEl) as ExecutableType
            val returnType = resolveReturnTypeIfSuspended(methodResolved)
            val entityType = resolveEntityFromResultType(resolveQueryResultType(returnType))
            syncableEntitiesOnDao.addAll(findSyncableEntities(entityType as ClassName,
                    processingEnv).values)
        }
    }

    return syncableEntitiesOnDao.toList()
}

/**
 * Refactor the given SQL
 */
fun refactorSyncSelectSql(sql: String, resultComponentClassName: ClassName,
                          processingEnv: ProcessingEnvironment,
                          clientIdParamName: String = "clientId"): String {
    val syncableEntities = findSyncableEntities(resultComponentClassName, processingEnv)
    if(syncableEntities.isEmpty())
        return sql

    var newSql = "SELECT * FROM ($sql) AS ${resultComponentClassName.simpleName} WHERE "
    val whereClauses = syncableEntities.values.map {
        val syncableEntityInfo = SyncableEntityInfo(it, processingEnv)

        """( ${syncableEntityInfo.entityMasterCsnField.name} > COALESCE((SELECT 
            |${syncableEntityInfo.trackerCsnField.name} FROM ${syncableEntityInfo.tracker.simpleName}  
            |WHERE  ${syncableEntityInfo.trackerDestField.name} = :$clientIdParamName 
            |AND ${syncableEntityInfo.trackerPkField.name} = 
            |${resultComponentClassName.simpleName}.${syncableEntityInfo.entityPkField.name} 
            |AND ${syncableEntityInfo.trackerReceivedField.name}), 0))
        """.trimMargin()
    }
    newSql += whereClauses.joinToString(prefix = "(", postfix = ")", separator = " OR ")

    return newSql
}


/**
 * Determine if the given
 */
internal fun isQueryParam(typeName: TypeName) = typeName in QUERY_SINGULAR_TYPES
        || (typeName is ParameterizedTypeName
        && (typeName.rawType == List::class.asClassName() && typeName.typeArguments[0] in QUERY_SINGULAR_TYPES))


/**
 * Given a list of parameters, get a list of those that get not pass as query parameters over http.
 * This is any parameters except primitive types, strings, or lists and arrays thereof
 *
 * @param params List of parameters to check for which ones cannot be passed as query parameters
 * @return List of parameters from the input list which cannot be passed as http query parameters
 */
internal fun getHttpBodyParams(params: List<ParameterSpec>) = params.filter {
    !isQueryParam(it.type) && !isContinuationParam(it.type)
}


/**
 * Given a list of parameters, get a list of those that get not pass as query parameters over http.
 * This is any parameters except primitive types, strings, or lists and arrays thereof
 *
 * @param daoMethodEl the method element itself, used to find parameter names
 * @param daoExecutableType the executable type, used to find parameter types (the executable type
 * may well come from typeUtils.asMemberOf to resolve type arguments
 */
internal fun getHttpBodyParams(daoMethodEl: ExecutableElement, daoExecutableType: ExecutableType) =
        getHttpBodyParams(daoMethodEl.parameters.mapIndexed { index, paramEl ->
            ParameterSpec.builder(paramEl.simpleName.toString(),
                    daoExecutableType.parameterTypes[index].asTypeName().javaToKotlinType()).build()
        })

/**
 * Given a list of http parameters, find the first, if any, which should be sent as the http body
 */
internal fun getRequestBodyParam(params: List<ParameterSpec>) = params.firstOrNull {
    !isQueryParam(it.type) && !isContinuationParam(it.type)
}


internal val CLIENT_GET_MEMBER_NAME = MemberName("io.ktor.client.request", "get")

internal val CLIENT_POST_MEMBER_NAME = MemberName("io.ktor.client.request", "post")

internal val CLIENT_RECEIVE_MEMBER_NAME = MemberName("io.ktor.client.call", "receive")

/**
 * Generates a CodeBlock that will make KTOR HTTP Client Request for a DAO method. It will set
 * the correct URL (e.g. endpoint/DatabaseName/DaoName/methodName and parameters (including the request body
 * if required). It will decide between using get or post based on the parameters.
 *
 * @param httpEndpointVarName the variable name that contains the base http endpoint to start with for the url
 * @param dbPathVarName the variable name that contains the name of the database
 * @param daoName the DAO name (e.g. simple class name of the DAO class)
 * @param methodName the name of the method that is being queried
 * @param httpResponseVarName the variable name that will be added to the codeblock that will contain
 * the http response object
 * @param httpResultType the type of response expected from the other end (e.g. the result type of
 * the method)
 * @param params a list of the parameters (e.g. from the method signature) that need to be sent
 */
internal fun generateKtorRequestCodeBlockForMethod(httpEndpointVarName: String = "_endpoint",
                                                   dbPathVarName: String,
                                                   daoName: String,
                                                   methodName: String,
                                                   httpResponseVarName: String = "_httpResponse",
                                                   httpResultVarName: String = "_httpResult",
                                                   requestBuilderCodeBlock: CodeBlock = CodeBlock.of(""),
                                                   httpResultType: TypeName,
                                                   params: List<ParameterSpec> ): CodeBlock {
    val nonQueryParams = getHttpBodyParams(params)
    val codeBlock = CodeBlock.builder()
            .beginControlFlow("val $httpResponseVarName = _httpClient.%M<%T>",
                    if(nonQueryParams.isNullOrEmpty()) CLIENT_GET_MEMBER_NAME else CLIENT_POST_MEMBER_NAME,
                    HttpResponse::class)
            .beginControlFlow("url")
            .add("%M($httpEndpointVarName)\n", MemberName("io.ktor.http", "takeFrom"))
            .add("encodedPath = \"\${encodedPath}\${$dbPathVarName}/%L/%L\"", daoName, methodName)
            .endControlFlow()
            .add(requestBuilderCodeBlock)

    params.filter { isQueryParam(it.type) }.forEach {
        codeBlock.addWithNullCheckIfNeeded(it.name, it.type,
                CodeBlock.of("%M(%S, ${it.name})\n",
                        MemberName("io.ktor.client.request", "parameter"),
                        it.name))
    }

    val requestBodyParam = getRequestBodyParam(params)

    if(requestBodyParam != null) {
        codeBlock.addWithNullCheckIfNeeded(requestBodyParam.name, requestBodyParam.type,
                CodeBlock.of("body = %M().write(${requestBodyParam.name})\n",
                        MemberName("io.ktor.client.features.json", "defaultSerializer")))
    }

    codeBlock.endControlFlow()

    val receiveCodeBlock = CodeBlock.of("$httpResponseVarName.%M<%T>()\n",
        CLIENT_RECEIVE_MEMBER_NAME, httpResultType)
    if(httpResultType.isNullable) {
        codeBlock.beginControlFlow("val $httpResultVarName = if(${httpResponseVarName}.status == %T.NoContent)",
            HttpStatusCode::class)
                .add("null\n")
                .nextControlFlow("else")
                .add(receiveCodeBlock)
                .endControlFlow()
    }else {
        codeBlock.add("val $httpResultVarName = ")
        codeBlock.add(receiveCodeBlock)
    }


    return codeBlock.build()
}

/**
 * Generate a CodeBlock that will use a synchelper DAO to insert sync status tracker entities
 * e.g. _syncHelper._replaceSyncEntityTracker(_result.map { SyncEntityTracker(...) })
 */
fun generateReplaceSyncableEntitiesTrackerCodeBlock(resultVarName: String, resultType: TypeName,
                                                    syncHelperDaoVarName: String = "_syncHelper",
                                                    clientIdVarName: String = "clientId",
                                                    reqIdVarName: String = "_reqId",
                                                    processingEnv: ProcessingEnvironment): CodeBlock {
    val codeBlock = CodeBlock.builder()
    val resultComponentType = resolveEntityFromResultType(resultType)
    if(resultComponentType !is ClassName)
        return codeBlock.build()

    val syncableEntitiesList = findSyncableEntities(resultComponentType, processingEnv)

    syncableEntitiesList.forEach {
        val sEntityInfo = SyncableEntityInfo(it.value, processingEnv)

        val isListOrArrayResult = isListOrArray(resultType)
        var wrapperFnName = Pair("", "")
        var varName = ""
        if(isListOrArrayResult) {
            var prefix = resultVarName
            it.key.forEach {embedVarName ->
                prefix += ".map { it!!.$embedVarName }.filter { it != null }"
            }
            wrapperFnName = Pair("$prefix.map {", "}")
            varName = "it!!"
        }else {
            var accessorName = resultVarName
            it.key.forEach {embedVarName ->
                accessorName += "?.$embedVarName"
            }

            varName = "_se${sEntityInfo.syncableEntity.simpleName}"
            codeBlock.add("val $varName = $accessorName\n")

            wrapperFnName = Pair("listOf(", ")")
        }


        if(!isListOrArrayResult) {
            codeBlock.beginControlFlow("if($varName != null)")
        }

        val entityCsnField = sEntityInfo.entityMasterCsnField
        val entityCsnSuffix = if(entityCsnField.type != INT) {
            ".toInt()"
        }else {
            ""
        }

        codeBlock.add("""$syncHelperDaoVarName._replace${sEntityInfo.tracker.simpleName}( ${wrapperFnName.first} %T(
                             |${sEntityInfo.trackerPkField.name} = $varName.${sEntityInfo.entityPkField.name},
                             |${sEntityInfo.trackerDestField.name} = $clientIdVarName,
                             |${sEntityInfo.trackerCsnField.name} = $varName.${sEntityInfo.entityMasterCsnField.name}$entityCsnSuffix,
                             |${sEntityInfo.trackerReqIdField.name} = $reqIdVarName
                             |) ${wrapperFnName.second} )
                             |""".trimMargin(), sEntityInfo.tracker)
        if(!isListOrArrayResult) {
            codeBlock.endControlFlow()
        }
    }

    return codeBlock.build()
}

fun generateReplaceSyncableEntityCodeBlock(resultVarName: String, resultType: TypeName,
                                           syncHelperDaoVarName: String = "_syncHelper",
                                           afterInsertCode: (ClassName) -> CodeBlock = {CodeBlock.of("")},
                                           processingEnv: ProcessingEnvironment): CodeBlock {
    val codeBlock = CodeBlock.builder()

    val resultComponentType = resolveEntityFromResultType(resultType)
    if(resultComponentType !is ClassName)
        return codeBlock.build()

    val syncableEntitiesList = findSyncableEntities(resultComponentType, processingEnv)

    syncableEntitiesList.forEach {
        val sEntityInfo = SyncableEntityInfo(it.value, processingEnv)

        val isListOrArrayResult = isListOrArray(resultType)

        val replaceEntityFnName ="_replace${sEntityInfo.syncableEntity.simpleName}"

        if(isListOrArrayResult) {
            codeBlock.add("${syncHelperDaoVarName}.$replaceEntityFnName($resultVarName")
            it.key.forEach {embedVarName ->
                codeBlock.add(".filter { it.$embedVarName != null }.map { it.$embedVarName as %T }",
                        it.value)
            }

            if(it.key.isEmpty() && it.value != sEntityInfo.syncableEntity) {
                codeBlock.add(".map { it as %T }", sEntityInfo.syncableEntity)
            }
            codeBlock.add(")\n")
        }else {
            val accessorVarName = "_se${sEntityInfo.syncableEntity.simpleName}"
            codeBlock.add("val $accessorVarName = $resultVarName")
                .add(it.key.joinToString (prefix = "", separator = "?.", postfix = ""))
                .add("\n")
                .beginControlFlow("if($accessorVarName != null)")
                .add("${syncHelperDaoVarName}.$replaceEntityFnName(listOf($accessorVarName))\n")
                .endControlFlow()
        }

        codeBlock.add(afterInsertCode(it.value))
    }

    return codeBlock.build()
}


/**
 * Will add the given codeblock, and surround it with if(varName != null) if the given typename
 * is nullable
 */
fun CodeBlock.Builder.addWithNullCheckIfNeeded(varName: String, typeName: TypeName,
                                               codeBlock: CodeBlock): CodeBlock.Builder {
    if(typeName.isNullable)
        beginControlFlow("if($varName != null)")

    add(codeBlock)

    if(typeName.isNullable)
        endControlFlow()

    return this
}

fun getEntityPrimaryKey(entityEl: TypeElement) = entityEl.enclosedElements
        .firstOrNull { it.kind == ElementKind.FIELD && it.getAnnotation(PrimaryKey::class.java) != null}


internal fun isSyncableDb(dbTypeEl: TypeElement, processingEnv: ProcessingEnvironment) =
        processingEnv.typeUtils.isAssignable(dbTypeEl.asType(),
                processingEnv.elementUtils.getTypeElement(SyncableDoorDatabase::class.java.canonicalName).asType())


abstract class AbstractDbProcessor: AbstractProcessor() {

    protected lateinit var messager: Messager

    protected var dbConnection: Connection? = null

    /**
     * When we generate the code for a Query annotation function that performs an update or delete,
     * we use this so that we can match the case of the table name.
     */
    protected val allKnownEntityNames = mutableListOf<String>()

    override fun init(p0: ProcessingEnvironment) {
        super.init(p0)
        messager = p0.messager
    }

    /**
     * Run create
     */
    internal fun setupDb(roundEnv: RoundEnvironment) {
        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)
        val dataSource = SQLiteDataSource()
        val dbTmpFile = File.createTempFile("dbprocessorkt", ".db")
        println("Db tmp file: ${dbTmpFile.absolutePath}")
        dataSource.url = "jdbc:sqlite:${dbTmpFile.absolutePath}"
        messager!!.printMessage(Diagnostic.Kind.NOTE, "Annotation processor db tmp file: ${dbTmpFile.absolutePath}")

        dbConnection = dataSource.connection
        dbs.flatMap { entityTypesOnDb(it as TypeElement, processingEnv) }.forEach {entity ->
            if(entity.getAnnotation(Entity::class.java) == null) {
                logMessage(Diagnostic.Kind.ERROR,
                        "Class ${entity.simpleName} used as entity on database does not have @Entity annotation",
                        entity)
            }

            if(!entity.enclosedElements.any { it.getAnnotation(PrimaryKey::class.java) != null }) {
                logMessage(Diagnostic.Kind.ERROR,
                        "Class ${entity.simpleName} used as entity does not have a field annotated @PrimaryKey")
            }

            val stmt = dbConnection!!.createStatement()
            stmt.use {
                val typeEntitySpec = entity.asEntityTypeSpec()
                val createTableSql = makeCreateTableStatement(typeEntitySpec, DoorDbType.SQLITE)
                try {
                    stmt.execute(createTableSql)
                }catch(sqle: SQLException) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "SQLException creating table for:" +
                            "${entity.simpleName} : ${sqle.message}. SQL was \"$createTableSql\"")
                }

                allKnownEntityNames.add(typeEntitySpec.name!!)
                if(entity.getAnnotation(SyncableEntity::class.java) != null) {
                    val trackerEntitySpec = generateTrackerEntity(entity, processingEnv)
                    stmt.execute(makeCreateTableStatement(trackerEntitySpec, DoorDbType.SQLITE))
                    allKnownEntityNames.add(trackerEntitySpec.name!!)
                }
            }
        }
    }

    protected fun makeCreateTableStatement(entitySpec: TypeSpec, dbType: Int): String {
        var sql = "CREATE TABLE IF NOT EXISTS ${entitySpec.name} ("
        var commaNeeded = false
        for (fieldEl in getEntityFieldElements(entitySpec, true)) {
            sql += """${if(commaNeeded) "," else " "} ${fieldEl.name} """
            val pkAutoGenerate = fieldEl.annotations
                    .firstOrNull { it.className == PrimaryKey::class.asClassName() }
                    ?.members?.findBooleanMemberValue("autoGenerate") ?: false
            if(pkAutoGenerate) {
                when(dbType) {
                    DoorDbType.SQLITE -> sql += " INTEGER "
                    DoorDbType.POSTGRES -> sql += (if(fieldEl.type == LONG) { " BIGSERIAL " } else { " SERIAL " })
                }
            }else {
                sql += " ${fieldEl.type.toSqlType(dbType)} "
            }

            if(fieldEl.annotations.any { it.className == PrimaryKey::class.asClassName()} ) {
                sql += " PRIMARY KEY "
                if(pkAutoGenerate && dbType == DoorDbType.SQLITE)
                    sql += " AUTOINCREMENT "

                sql += " NOT NULL "
            }

            commaNeeded = true
        }
        sql += ")"

        return sql
    }

    protected fun generateCreateIndicesCodeBlock(indexes: Array<IndexMirror>, tableName: String,
                                            execSqlFnName: String): CodeBlock {
        val codeBlock = CodeBlock.builder()
        indexes.forEach {
            val indexName = if(it.name != "") {
                it.name
            }else {
                "index_${tableName}_${it.value.joinToString(separator = "_", postfix = "", prefix = "")}"
            }

            codeBlock.add("$execSqlFnName(%S)\n", """CREATE 
                |${if(it.unique){ "UNIQUE" } else { "" } } INDEX $indexName 
                |ON $tableName (${it.value.joinToString()})""".trimMargin())
        }

        return codeBlock.build()
    }

    protected fun generateSyncTriggersCodeBlock(entityClass: ClassName, execSqlFn: String, dbType: Int): CodeBlock {
        val codeBlock = CodeBlock.builder()
        messager.printMessage(Diagnostic.Kind.NOTE, "AbstractDbProcessor: generateSyncTriggersCodeBlock: ${entityClass.canonicalName}")
        val syncableEntityInfo = SyncableEntityInfo(entityClass, processingEnv)
        when(dbType){
            DoorDbType.SQLITE -> {
                listOf("UPDATE", "INSERT").forEach {op_name ->
                    codeBlock.add("$execSqlFn(%S)\n", """CREATE TRIGGER ${op_name.substring(0, 3)}_${syncableEntityInfo.tableId}
                        |AFTER $op_name ON ${entityClass.simpleName} FOR EACH ROW WHEN
                        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                        |(NEW.${syncableEntityInfo.entityMasterCsnField.name} = 0 
                        |${
                        if(op_name == "UPDATE") {
                            "OR OLD.${syncableEntityInfo.entityMasterCsnField.name} = NEW.${syncableEntityInfo.entityMasterCsnField.name}"
                        }else {
                            ""
                        }}
                        |)
                        |ELSE
                        |(NEW.${syncableEntityInfo.entityLocalCsnField.name} = 0  
                        |${
                        if(op_name == "UPDATE") {
                            "OR OLD.${syncableEntityInfo.entityLocalCsnField.name} = NEW.${syncableEntityInfo.entityLocalCsnField.name}"
                        }else {
                            ""
                        }}
                        |) END)
                        |BEGIN 
                        |UPDATE ${entityClass.simpleName} SET ${syncableEntityInfo.entityLocalCsnField.name} = 
                        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.${syncableEntityInfo.entityLocalCsnField.name} 
                        |ELSE (SELECT MAX(${syncableEntityInfo.entityLocalCsnField.name}) + 1 FROM ${entityClass.simpleName}) END),
                        |${syncableEntityInfo.entityMasterCsnField.name} = 
                        |(SELECT CASE WHEN (SELECT master FROM SyncNode) THEN 
                        |(SELECT MAX(${syncableEntityInfo.entityMasterCsnField.name}) + 1 FROM ${entityClass.simpleName})
                        |ELSE NEW.${syncableEntityInfo.entityMasterCsnField.name} END)
                        |WHERE ${syncableEntityInfo.entityPkField.name} = NEW.${syncableEntityInfo.entityPkField.name}
                        |; END
                    """.trimMargin())
                }

            }

            DoorDbType.POSTGRES -> {
                listOf("m", "l").forEach {
                    codeBlock.add("$execSqlFn(%S)\n",
                        "CREATE SEQUENCE IF NOT EXISTS ${syncableEntityInfo.syncableEntity.simpleName}_${it}csn_seq")
                }

                codeBlock.add("$execSqlFn(%S)\n", """CREATE OR REPLACE FUNCTION 
                    | inccsn_${syncableEntityInfo.tableId}_fn() RETURNS trigger AS $$
                    | BEGIN  
                    | UPDATE ${syncableEntityInfo.syncableEntity.simpleName} SET ${syncableEntityInfo.entityLocalCsnField.name} =
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) THEN NEW.${syncableEntityInfo.entityLocalCsnField.name} 
                    | ELSE NEXTVAL('${syncableEntityInfo.syncableEntity.simpleName}_lcsn_seq') END),
                    | ${syncableEntityInfo.entityMasterCsnField.name} = 
                    | (SELECT CASE WHEN (SELECT master FROM SyncNode) 
                    | THEN NEXTVAL('${syncableEntityInfo.syncableEntity.simpleName}_mcsn_seq') 
                    | ELSE NEW.${syncableEntityInfo.entityMasterCsnField.name} END)
                    | WHERE ${syncableEntityInfo.entityPkField.name} = NEW.${syncableEntityInfo.entityPkField.name};
                    | RETURN null;
                    | END $$
                    | LANGUAGE plpgsql
                """.trimMargin())
                        .add("$execSqlFn(%S)\n", """CREATE TRIGGER inccsn_${syncableEntityInfo.tableId}_trig 
                            |AFTER UPDATE OR INSERT ON ${syncableEntityInfo.syncableEntity.simpleName} 
                            |FOR EACH ROW WHEN (pg_trigger_depth() = 0) 
                            |EXECUTE PROCEDURE inccsn_${syncableEntityInfo.tableId}_fn()
                        """.trimMargin())
            }
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "AbstractDbProcessor: finish generateSyncTriggersCodeBlock: ${entityClass.canonicalName}")

        return codeBlock.build()
    }


    /**
     * Generate a codeblock with the JDBC code required to perform a query and return the given
     * result type
     *
     * @param returnType the return type of the query
     * @param queryVars: map of String (variable name) to the type of parameter. Used to set
     * parameters on the preparedstatement
     * @param querySql The actual query SQL itself (e.g. as per the Query annotation)
     * @param enclosing TypeElement (e.g the DAO) in which it is enclosed, used to resolve parameter types
     * @param method The method that this implementation is being generated for. Used for error reporting purposes
     * @param resultVarName The variable name for the result of the query (this will be as per resultType,
     * with any wrapping (e.g. LiveData) removed.
     */
    //TODO: Check for invalid combos. Cannot have querySql and rawQueryVarName as null. Cannot have rawquery doing update
    fun generateQueryCodeBlock(returnType: TypeName, queryVars: Map<String, TypeName>, querySql: String?,
                               enclosing: TypeElement?, method: ExecutableElement?,
                               resultVarName: String = "_result", rawQueryVarName: String? = null): CodeBlock {
        // The result, with any wrapper (e.g. LiveData or DataSource.Factory) removed
        val resultType = resolveQueryResultType(returnType)

        // The individual entity type e.g. Entity or String etc
        val entityType = resolveEntityFromResultType(resultType)

        val entityTypeElement = if(entityType is ClassName) {
            processingEnv.elementUtils.getTypeElement(entityType.canonicalName)
        } else {
            null
        }

        val entityFieldMap = if(entityTypeElement != null) {
            mapEntityFields(entityTypeEl = entityTypeElement, processingEnv = processingEnv)
        }else {
            null
        }

        val isUpdateOrDelete = querySql != null
                && (querySql.trim().startsWith("update", ignoreCase = true)
                || querySql.trim().startsWith("delete", ignoreCase = true))

        val codeBlock = CodeBlock.builder()

        var preparedStatementSql = querySql
        var execStmtSql = querySql
        if(preparedStatementSql != null) {
            val namedParams = getQueryNamedParameters(querySql!!)
            namedParams.forEach { preparedStatementSql = preparedStatementSql!!.replace(":$it", "?") }
            namedParams.forEach { execStmtSql = execStmtSql!!.replace(":$it",
                    defaultSqlQueryVal(queryVars[it]!!)) }
        }

        if(resultType != UNIT)
            codeBlock.add("var $resultVarName = ${defaultVal(resultType)}\n")

        codeBlock.add("var _conToClose = null as %T?\n", Connection::class)
                .add("var _stmtToClose = null as %T?\n", PreparedStatement::class)
                .add("var _resultSetToClose = null as %T?\n", ResultSet::class)
                .beginControlFlow("try")
                .add("val _con = _db.openConnection()\n")
                .add("_conToClose = _con\n")

        if(rawQueryVarName == null) {
            if(queryVars.any { isListOrArray(it.value.javaToKotlinType()) }) {
                codeBlock.beginControlFlow("val _stmt = if(_db!!.jdbcArraySupported)")
                        .add("_con.prepareStatement(_db.adjustQueryWithSelectInParam(%S))!!\n", preparedStatementSql)
                        .nextControlFlow("else")
                        .add("%T(%S, _con) as %T\n", PreparedStatementArrayProxy::class, preparedStatementSql,
                                PreparedStatement::class)
                        .endControlFlow()
            }else {
                codeBlock.add("val _stmt = _con.prepareStatement(%S)\n", preparedStatementSql)
            }
        }else {
            codeBlock.beginControlFlow("val _stmt = if(!_db!!.jdbcArraySupported && ($rawQueryVarName.values?.asList()?.any { it is List<*> || (it?.javaClass?.isArray ?: false)} ?: false))")
                    .add("%T(_db.adjustQueryWithSelectInParam($rawQueryVarName.getSql()), _con) as %T\n",
                            PreparedStatementArrayProxy::class, PreparedStatement::class)
                    .nextControlFlow("else")
                    .add("_con.prepareStatement(_db.adjustQueryWithSelectInParam($rawQueryVarName.getSql()))\n")
                    .endControlFlow()
        }


        codeBlock.add("_stmtToClose = _stmt\n")


        if(querySql != null) {
            var paramIndex = 1
            val queryVarsNotSubstituted = mutableListOf<String>()
            getQueryNamedParameters(querySql).forEach {
                val paramType = queryVars[it]
                if(paramType == null ) {
                    queryVarsNotSubstituted.add(it)
                }else if(isListOrArray(paramType.javaToKotlinType())) {
                    //val con = null as Connection
                    val arrayTypeName = sqlArrayComponentTypeOf(paramType.javaToKotlinType())
                    codeBlock.add("_stmt.setArray(${paramIndex++}, ")
                            .beginControlFlow("if(_db!!.jdbcArraySupported) ")
                            .add("_con!!.createArrayOf(%S, %L.toTypedArray())\n", arrayTypeName, it)
                            .nextControlFlow("else")
                            .add("%T.createArrayOf(%S, %L.toTypedArray())\n", PreparedStatementArrayProxy::class,
                                    arrayTypeName, it)
                            .endControlFlow()
                            .add(")\n")
                }else {
                    codeBlock.add("_stmt.set${getPreparedStatementSetterGetterTypeName(paramType.javaToKotlinType())}(${paramIndex++}, " +
                            "${it})\n")
                }
            }

            if(queryVarsNotSubstituted.isNotEmpty()) {
                logMessage(Diagnostic.Kind.ERROR,
                        "Parameters in query not found in method signature: ${queryVarsNotSubstituted.joinToString()}",
                        enclosing, method)
                return CodeBlock.builder().build()
            }
        }else {
            codeBlock.add("$rawQueryVarName.bindToPreparedStmt(_stmt, _db, _con)\n")
        }

        var resultSet = null as ResultSet?
        var execStmt = null as Statement?
        try {
            execStmt = dbConnection?.createStatement()

            if(isUpdateOrDelete) {
                /*
                 Run this query now so that we would get an exception if there is something wrong with it.
                 */
                execStmt?.executeUpdate(execStmtSql)
                codeBlock.add("val _numUpdates = _stmt.executeUpdate()\n")
                val stmtSplit = execStmtSql!!.trim().split(Regex("\\s+"), limit = 4)
                val tableName = if(stmtSplit[0].equals("UPDATE", ignoreCase = true)) {
                    stmtSplit[1] // in case it is an update statement, will be the second word (e.g. update tablename)
                }else {
                    stmtSplit[2] // in case it is a delete statement, will be the third word (e.g. delete from tablename)
                }

                /*
                 * If the entity did not exist, then our attempt to run the query would have thrown
                 * an SQLException . When calling handleTableChanged, we want to use the same case
                 * as the entity, so we look it up from the list of known entities to find the correct
                 * case to use.
                 */
                val entityModified = allKnownEntityNames.first {it.equals(tableName,  ignoreCase = true)}

                codeBlock.beginControlFlow("if(_numUpdates > 0)")
                        .add("_db.handleTableChanged(listOf(%S))\n", entityModified)
                        .endControlFlow()

                if(resultType != UNIT) {
                    codeBlock.add("$resultVarName = _numUpdates\n")
                }
            }else {
                codeBlock.add("val _resultSet = _stmt.executeQuery()\n")
                        .add("_resultSetToClose = _resultSet\n")

                val colNames = mutableListOf<String>()
                if(execStmtSql != null) {
                    resultSet = execStmt?.executeQuery(execStmtSql)
                    val metaData = resultSet!!.metaData
                    for(i in 1 .. metaData.columnCount) {
                        colNames.add(metaData.getColumnName(i))
                    }
                }else {
                    colNames.addAll(entityFieldMap!!.fieldMap.map { it.key.substringAfterLast('.') })
                }

                val entityVarName = "_entity"
                val entityInitializerBlock = if(QUERY_SINGULAR_TYPES.contains(entityType)) {
                    CodeBlock.builder().add("${defaultVal(entityType)}").build()
                }else {
                    CodeBlock.builder().add("%T()", entityType).build()
                }

                if(entityType !in QUERY_SINGULAR_TYPES && rawQueryVarName != null) {
                    codeBlock.add("val _resultMetaData = _resultSet.metaData\n")
                            .add("val _columnIndexMap = (1 .. _resultMetaData.columnCount).map { _resultMetaData.getColumnLabel(it) to it }.toMap()\n")
                }


                if(isListOrArray(resultType)) {
                    codeBlock.beginControlFlow("while(_resultSet.next())")
                }else {
                    codeBlock.beginControlFlow("if(_resultSet.next())")
                }

                if(QUERY_SINGULAR_TYPES.contains(entityType)) {
                    codeBlock.add("val $entityVarName = _resultSet.get${getPreparedStatementSetterGetterTypeName(entityType)}(1)\n")
                }else {
                    codeBlock.add("val _entity =")
                            .add(entityInitializerBlock)
                            .add("\n")

                    // Map of the last prop name (e.g. name) to the full property name as it will
                    // be generated (e.g. embedded!!.name)
                    val colNameLastToFullMap = entityFieldMap!!.fieldMap.map { it.key.substringAfterLast('.') to it.key}.toMap()

                    entityFieldMap.embeddedVarsList.forEach {
                        codeBlock.add("$entityVarName${it.first} = %T()\n", it.second.asType())
                    }

                    val missingPropNames = mutableListOf<String>()
                    colNames.forEach {colName ->
                        val fullPropName = colNameLastToFullMap[colName]
                        if(!fullPropName.isNullOrBlank()) {
                            val propType = entityFieldMap.fieldMap[fullPropName]
                            val getterName = "get${getPreparedStatementSetterGetterTypeName(propType!!.asType().asTypeName()) }"

                            if(rawQueryVarName != null) {
                                codeBlock.beginControlFlow("if(_columnIndexMap.containsKey(%S))",
                                        colName)
                            }

                            codeBlock.add("$entityVarName$fullPropName = _resultSet.$getterName(%S)\n", colName)
                            if(rawQueryVarName != null) {
                                codeBlock.endControlFlow()
                            }

                        }else {
                            missingPropNames.add(colName)
                        }
                    }

                    if(missingPropNames.isNotEmpty()) {
                        logMessage(Diagnostic.Kind.ERROR, " Cannot map the following columns " +
                                "from query to properties on return type of element $entityType : " +
                                "$missingPropNames", enclosing, method)
                    }
                }

                if(isListOrArray(resultType)) {
                    codeBlock.add("$resultVarName.add(_entity)\n")
                }else {
                    codeBlock.add("$resultVarName = _entity\n")
                }

                codeBlock.endControlFlow()
            }
        }catch(e: SQLException) {
            logMessage(Diagnostic.Kind.ERROR,
                    "Exception running query SQL '$execStmtSql' : ${e.message}",
                    enclosing = enclosing, element = method,
                    annotation = method?.annotationMirrors?.firstOrNull {it.annotationType.asTypeName() == Query::class.asTypeName()})
        }

        codeBlock.nextControlFlow("catch(_e: %T)", SQLException::class)
                .add("_e.printStackTrace()\n")
                .add("throw %T(_e)\n", RuntimeException::class)
                .nextControlFlow("finally")
                .add("_resultSetToClose?.close()\n")
                .add("_stmtToClose?.close()\n")
                .add("_conToClose?.close()\n")
                .endControlFlow()

        return codeBlock.build()
    }

    /**
     * Generate a JDBC insert code block. Generates an EntityInsertionAdapter, insert SQL,
     * and code that will insert from the given parameters
     *
     * @param parameterSpec - ParameterSpec representing the entity type to insert. This could be
     * any POKO with the Entity annotation, or a list thereof
     * @param returnType - TypeName representing the return value. This can be UNIT for no return type,
     * a long for a singular insert (return auto generated primary key), or a list of longs (return
     * all generated primary keys)
     * @param daoTypeBuilder The TypeBuilder being used to construct the DAO. If not already present,
     * an entity insertion adapter member variable will be added to the typeBuilder.
     * @param upsertMode - if true, the query will be generated as an upsert
     * @param addReturnStmt - if true, a return statement will be added to the codeblock, where the
     * return type will match the given returnType
     */
    fun generateInsertCodeBlock(parameterSpec: ParameterSpec, returnType: TypeName,
                                   entityTypeSpec: TypeSpec,
                                   daoTypeBuilder: TypeSpec.Builder,
                                   upsertMode: Boolean = false,
                                   addReturnStmt: Boolean = true): CodeBlock {
        val codeBlock = CodeBlock.builder()
        val paramType = parameterSpec.type
        val entityClassName = if(paramType is ParameterizedTypeName && paramType.rawType == List::class.asClassName()) {
            val typeArg = paramType.typeArguments[0]
            if(typeArg is WildcardTypeName) {
                typeArg.outTypes[0] as ClassName
            }else {
                typeArg as ClassName
            }
        }else {
            paramType as ClassName
        }


        val entityInserterPropName = "_insertAdapter${entityTypeSpec.name}_${if(upsertMode) "upsert" else ""}"
        if(!daoTypeBuilder.propertySpecs.any { it.name == entityInserterPropName }) {
            val fieldNames = mutableListOf<String>()
            val parameterHolders = mutableListOf<String>()

            val bindCodeBlock = CodeBlock.builder()
            var fieldIndex = 1
            val pkProp = entityTypeSpec.propertySpecs
                    .first { it.annotations.any { it.className == PrimaryKey::class.asClassName()} }

            entityTypeSpec.propertySpecs.forEach { prop ->
                fieldNames.add(prop.name)
                val pkAnnotation = prop.annotations.firstOrNull { it.className == PrimaryKey::class.asClassName() }
                val setterMethodName = getPreparedStatementSetterGetterTypeName(prop.type)
                if(pkAnnotation != null && pkAnnotation.members.findBooleanMemberValue("autoGenerate") ?: false) {
                    parameterHolders.add("\${when(_db.jdbcDbType) { DoorDbType.POSTGRES -> " +
                            "\"COALESCE(?,nextval('${entityTypeSpec.name}_${prop.name}_seq'))\" else -> \"?\"} }")
                    bindCodeBlock.add("when(entity.${prop.name}){ ${defaultVal(prop.type)} " +
                            "-> stmt.setObject(${fieldIndex}, null) " +
                            "else -> stmt.set$setterMethodName(${fieldIndex++}, entity.${prop.name})  }\n")
                }else {
                    parameterHolders.add("?")
                    bindCodeBlock.add("stmt.set$setterMethodName(${fieldIndex++}, entity.${prop.name})\n")
                }
            }

            val statementClause = if(upsertMode) {
                "\${when(_db.jdbcDbType) { DoorDbType.SQLITE -> \"INSERT·OR·REPLACE\" else -> \"INSERT\"} }"
            }else {
                "INSERT"
            }

            val upsertSuffix = if(upsertMode) {
                val nonPkFields = entityTypeSpec.propertySpecs
                        .filter { ! it.annotations.any { it.className == PrimaryKey::class.asClassName() } }
                val nonPkFieldPairs = nonPkFields.map { "${it.name}·=·excluded.${it.name}" }
                val pkField = entityTypeSpec.propertySpecs
                        .firstOrNull { it.annotations.any { it.className == PrimaryKey::class.asClassName()}}
                "\${when(_db.jdbcDbType){ DoorDbType.POSTGRES -> \"·ON·CONFLICT·(${pkField?.name})·" +
                        "DO·UPDATE·SET·${nonPkFieldPairs.joinToString(separator = ",·")}\" " +
                        "else -> \"·\" } } "
            } else {
                ""
            }

            val autoGenerateSuffix = " \${when{ _db.jdbcDbType == DoorDbType.POSTGRES && returnsId -> " +
                    "\"·RETURNING·${pkProp.name}·\"  else -> \"\"} } "

            val sql = """
                $statementClause INTO ${entityTypeSpec.name} (${fieldNames.joinToString()})
                VALUES (${parameterHolders.joinToString()})
                $upsertSuffix
                $autoGenerateSuffix
                """.trimIndent()

            val insertAdapterSpec = TypeSpec.anonymousClassBuilder()
                    .superclass(EntityInsertionAdapter::class.asClassName().parameterizedBy(entityClassName))
                    .addSuperclassConstructorParameter("_db.jdbcDbType")
                    .addFunction(FunSpec.builder("makeSql")
                            .addParameter("returnsId", BOOLEAN)
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode("return \"\"\"%L\"\"\"", sql).build())
                    .addFunction(FunSpec.builder("bindPreparedStmtToEntity")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("stmt", PreparedStatement::class)
                            .addParameter("entity", entityClassName)
                            .addCode(bindCodeBlock.build()).build())

            daoTypeBuilder.addProperty(PropertySpec.builder(entityInserterPropName,
                    EntityInsertionAdapter::class.asClassName().parameterizedBy(entityClassName))
                    .initializer("%L", insertAdapterSpec.build())
                    .build())
        }



        if(returnType != UNIT) {
            codeBlock.add("val _retVal = ")
        }


        val insertMethodName = makeInsertAdapterMethodName(paramType, returnType, processingEnv)
        codeBlock.add("$entityInserterPropName.$insertMethodName(${parameterSpec.name}, _db.openConnection())")

        if(returnType != UNIT) {
            if(isListOrArray(returnType)
                    && returnType is ParameterizedTypeName
                    && returnType.typeArguments[0] == INT) {
                codeBlock.add(".map { it.toInt() }")
            }else if(returnType == INT){
                codeBlock.add(".toInt()")
            }
        }

        codeBlock.add("\n")

        codeBlock.add("_db.handleTableChanged(listOf(%S))\n", entityTypeSpec.name)

        if(addReturnStmt) {
            if(returnType != UNIT) {
                codeBlock.add("return _retVal")
            }

            if(returnType is ParameterizedTypeName
                    && returnType.rawType == ARRAY) {
                codeBlock.add(".toTypedArray()")
            }else if(returnType == LongArray::class.asClassName()) {
                codeBlock.add(".toLongArray()")
            }else if(returnType == IntArray::class.asClassName()) {
                codeBlock.add(".toIntArray()")
            }
        }

        codeBlock.add("\n")

        return codeBlock.build()
    }

    /**
     * Generates a CodeBlock for running an SQL select statement on the KTOR serer and then returning
     * the result as JSON.
     *
     * e.g.
     * get("methodName") {
     *   val paramVal = request.queryParameters['uid']?.toLong()
     *   .. query execution code (as per JDBC)
     *   call.respond(_result)
     * }
     *
     * The method will automatically choose between using get or post, and will use post if there
     * are any parameters which cannot be sent as query parameters (e.g. JSON), or get otherwise.
     *
     * This will handle refactoring the query to remove syncable entities already delivered to the
     * client making the request
     *
     * @param daoMethod A FunSpec representing the DAO method that this CodeBlock is being generated for
     * @param daoTypeEl The DAO element that this is being generated for: optional for error logging purposes
     *
     */
    fun generateKtorRouteSelectCodeBlock(daoMethod: FunSpec, daoTypeEl: TypeElement? = null,
                                         syncHelperDaoVarName: String = "_syncHelper") : CodeBlock {
        val codeBlock = CodeBlock.builder()
        val resultType = resolveQueryResultType(daoMethod.returnType!!)

        daoMethod.parameters.forEach {
            codeBlock.add(generateGetParamFromRequestCodeBlock(it.type,
                    it.name, declareVariableName = it.name, declareVariableType = "val"))
        }

        val queryVarsMap = daoMethod.parameters.map { it.name to it.type}.toMap().toMutableMap()

        var querySql = daoMethod.annotations.first { it.className == Query::class.asClassName() }
                .members.first { it.toString().trim().startsWith("value") || it.toString().trim().startsWith("\"") }.toString()


        if(querySql.endsWith("trimMargin()")) {
            querySql = querySql.removeSuffix(".trimMargin()")
                    .removeSurrounding("\"\"\"").trimMargin()
        }else {
            querySql = querySql.removeSurrounding("\"")
        }

        querySql = StringEscapeUtils.unescapeJava(querySql)




        val componentEntityType = resolveEntityFromResultType(resultType)
        val syncableEntitiesList = if(componentEntityType is ClassName) {
            findSyncableEntities(componentEntityType, processingEnv)
        }else {
            null
        }

        if(syncableEntitiesList != null) {
            codeBlock.add("val clientId = %M.request.%M(%S)?.toInt() ?: 0\n",
                    DbProcessorKtorServer.CALL_MEMBER,
                    MemberName("io.ktor.request","header"),
                    "X-nid")
                    .add("val _reqId = %T().nextInt()\n", Random::class)
                    .add("%M.response.header(%S, _reqId)\n", DbProcessorKtorServer.CALL_MEMBER, "X-reqid")
            queryVarsMap.put("clientId", INT)
            querySql = refactorSyncSelectSql(querySql, componentEntityType as ClassName,
                    processingEnv)
        }


        codeBlock.add(generateQueryCodeBlock(resultType, queryVarsMap, querySql, daoTypeEl, null))
        codeBlock.add(generateReplaceSyncableEntitiesTrackerCodeBlock("_result", resultType,
                processingEnv = processingEnv, syncHelperDaoVarName = syncHelperDaoVarName))

        codeBlock.add(generateRespondCall(resultType, "_result"))

        return codeBlock.build()
    }

    /**
     * Generates a Codeblock that will call the DAO method, and then call.respond with the result
     *
     * e.g.
     * val paramName = request.queryParameters['paramName']?.toLong()
     * val _result = _dao.methodName(paramName)
     * call.respond(_result)
     *
     * @param daoMethod FunSpec representing the method that is being delegated
     */
    fun generateKtorPassToDaoCodeBlock(daoMethod: FunSpec): CodeBlock {
        val codeBlock = CodeBlock.builder()
        val returnType = daoMethod.returnType
        if(returnType != UNIT) {
            codeBlock.add("val _result = ")
        }

        codeBlock.add("_dao.${daoMethod.name}(")
        var paramOutCount = 0
        daoMethod.parameters.forEachIndexed {index, param ->
            val paramTypeName = param.type.javaToKotlinType()
            if(isContinuationParam(paramTypeName))
                return@forEachIndexed

            if(paramOutCount > 0)
                codeBlock.add(",")

            codeBlock.add(generateGetParamFromRequestCodeBlock(paramTypeName, param.name))

            paramOutCount++
        }

        codeBlock.add(")\n")

        codeBlock.add(generateRespondCall(returnType!!, "_result"))

        return codeBlock.build()
    }

    fun generateRepositoryGetSyncableEntitiesFun(daoFunSpec: FunSpec, daoName: String,
                                                 syncHelperDaoVarName: String = "_syncHelper",
                                                 addReturnDaoResult: Boolean  = true): CodeBlock {
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


        codeBlock.beginControlFlow("try")
        codeBlock.add(generateKtorRequestCodeBlockForMethod(
                daoName = daoName,
                dbPathVarName = "_dbPath",
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
                            .add("encodedPath = \"\${encodedPath}\${_dbPath}/%L/%L\"\n", daoName,
                                    "_update${SyncableEntityInfo(it, processingEnv).tracker.simpleName}Received")
                            .endControlFlow()
                            .add("%M(%S, _requestId)\n",
                                    MemberName("io.ktor.client.request", "parameter"),
                                    "reqId")
                            .endControlFlow()
                            .build()
                },
                resultType = resultType, processingEnv = processingEnv,
                syncHelperDaoVarName = syncHelperDaoVarName))

        codeBlock.nextControlFlow("catch(e: Exception)")
                .add("e.printStackTrace()\n")
                .endControlFlow()
        if(KModifier.SUSPEND !in daoFunSpec.modifiers) {
            codeBlock.endControlFlow()
        }

        if(addReturnDaoResult) {
            codeBlock.add("return _dao.${daoFunSpec.name}(")
                    .add(daoFunSpec.parameters.filter { !isContinuationParam(it.type)}.joinToString { it.name })
                    .add(")\n")
        }


        return codeBlock.build()
    }


    fun logMessage(kind: Diagnostic.Kind, message: String, enclosing: TypeElement? = null,
                   element: Element? = null, annotation: AnnotationMirror? = null) {
        val messageStr = "DoorDb: ${enclosing?.qualifiedName}#${element?.simpleName} $message "
        if(annotation != null && element != null) {
            messager?.printMessage(kind, messageStr, element, annotation)
        }else if(element != null) {
            messager?.printMessage(kind, messageStr, element)
        }else {
            messager?.printMessage(kind, messageStr)
        }
    }

    /**
     * Write the given file spec to directories specified in the annotation processor argument. Paths
     * should be separated by the path separator character (platform dependent - e.g. : on Unix, ; on Windows)
     */
    protected fun writeFileSpecToOutputDirs(fileSpec: FileSpec, argName: String, useFilerAsDefault: Boolean = true) {
        (processingEnv.options[argName]?.split(File.pathSeparator) ?: listOf(processingEnv.options["kapt.kotlin.generated"]!!)).forEach {
            fileSpec.writeTo(File(it))
        }
    }
}