package com.ustadmobile.lib.annotationprocessor.core

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
import com.ustadmobile.door.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.annotation.*
import java.util.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

fun isUpdateDeleteOrInsertMethod(methodEl: Element)
        = listOf(Update::class.java, Delete::class.java, Insert::class.java).any { methodEl.getAnnotation(it) != null }

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
        val syncableEntityTypeEl = processingEnv.elementUtils.getTypeElement(it.canonicalName)
        val entityPkField = syncableEntityTypeEl.enclosedElements
                .first { it.getAnnotation(PrimaryKey::class.java) != null }
        val entitySyncTracker = getEntitySyncTracker(syncableEntityTypeEl, processingEnv)
        val entitySyncTrackerEl = processingEnv.typeUtils.asElement(entitySyncTracker) as TypeElement
        val entityMasterCsnField = syncableEntityTypeEl.enclosedElements
                .first { it.getAnnotation(MasterChangeSeqNum::class.java) != null}
        val entitySyncTrackerPkField = entitySyncTrackerEl.enclosedElements
                .first {it.getAnnotation(TrackerEntityPrimaryKey::class.java) != null}
        val entitySyncTrackCsnField = entitySyncTrackerEl.enclosedElements
                .first { it.getAnnotation(TrackerChangeSeqNum::class.java) != null }
        val entitySyncTrackerDestField = entitySyncTrackerEl.enclosedElements
                .first {it.getAnnotation(TrackDestId::class.java) != null}


        """( ${entityMasterCsnField.simpleName} > COALESCE((SELECT 
            |${entitySyncTrackCsnField.simpleName} FROM ${entitySyncTrackerEl.simpleName}  
            |WHERE ${entitySyncTrackerPkField.simpleName} = ${resultComponentClassName.simpleName}.${entityPkField.simpleName} 
            |AND ${entitySyncTrackerDestField.simpleName} = :$clientIdParamName), 0))
        """.trimMargin()
    }
    newSql += whereClauses.joinToString(prefix = "(", postfix = ")", separator = " OR ")

    return newSql
}

abstract class AbstractDbProcessor: AbstractProcessor() {

    protected lateinit var messager: Messager

    protected var dbConnection: Connection? = null

    protected val allKnownEntities = mutableListOf<TypeElement>()

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
        dbs.flatMap { entityTypesOnDb(it as TypeElement, processingEnv) }.forEach {
            if(it.getAnnotation(Entity::class.java) == null) {
                logMessage(Diagnostic.Kind.ERROR,
                        "Class used as entity on database does not have @Entity annotation",
                        it)
            }

            if(!it.enclosedElements.any { it.getAnnotation(PrimaryKey::class.java) != null }) {
                logMessage(Diagnostic.Kind.ERROR,
                        "Class used as entity does not have a field annotated @PrimaryKey")
            }

            val stmt = dbConnection!!.createStatement()
            stmt.execute(makeCreateTableStatement(it, DoorDbType.SQLITE))
            allKnownEntities.add(it)
        }
    }

    protected fun makeCreateTableStatement(entitySpec: TypeElement, dbType: Int): String {
        var sql = "CREATE TABLE IF NOT EXISTS ${entitySpec.simpleName} ("
        var commaNeeded = false
        for (fieldEl in getEntityFieldElements(entitySpec, true)) {
            sql += """${if(commaNeeded) "," else " "} ${fieldEl.simpleName} """
            val pkAnnotation = fieldEl.getAnnotation(PrimaryKey::class.java)
            if(pkAnnotation != null && pkAnnotation.autoGenerate) {
                when(dbType) {
                    DoorDbType.SQLITE -> sql += " INTEGER "
                    DoorDbType.POSTGRES -> sql += " SERIAL "
                }
            }else {
                sql += " ${getFieldSqlType(fieldEl, processingEnv)} "
            }

            if(pkAnnotation != null) {
                sql += " PRIMARY KEY "
                if(pkAnnotation.autoGenerate && dbType == DoorDbType.SQLITE)
                    sql += " AUTOINCREMENT "

                sql += " NOT NULL "
            }

            commaNeeded = true
        }
        sql += ")"

        return sql
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
                val entityModified = allKnownEntities.first {it.simpleName.toString().equals(tableName, ignoreCase = true)}

                codeBlock.beginControlFlow("if(_numUpdates > 0)")
                        .add("_db.handleTableChanged(listOf(%S))\n", entityModified.simpleName.toString())
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
            logMessage(Diagnostic.Kind.ERROR, "Exception running query SQL '$execStmtSql' : ${e.message}",
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

        val entityTypeEl = processingEnv.elementUtils.getTypeElement(entityClassName.canonicalName)

        val entityInserterPropName = "_insertAdapter${entityTypeEl.simpleName}_${if(upsertMode) "upsert" else ""}"
        if(!daoTypeBuilder.propertySpecs.any { it.name == entityInserterPropName }) {
            val fieldNames = mutableListOf<String>()
            val parameterHolders = mutableListOf<String>()

            val bindCodeBlock = CodeBlock.builder()
            var fieldIndex = 1
            fieldsOnEntity(entityTypeEl).forEach {subEl ->
                fieldNames.add(subEl.simpleName.toString())
                val pkAnnotation = subEl.getAnnotation(PrimaryKey::class.java)
                val setterMethodName = getPreparedStatementSetterGetterTypeName(subEl.asType().asTypeName())
                if(pkAnnotation != null && pkAnnotation.autoGenerate) {
                    parameterHolders.add("\${when(_db.jdbcDbType) { DoorDbType.POSTGRES -> \"COALESCE(?,nextval('${entityTypeEl.simpleName}'))\" else -> \"?\"} }")
                    bindCodeBlock.add("when(entity.${subEl.simpleName}){ ${defaultVal(subEl.asType().asTypeName())} " +
                            "-> stmt.setObject(${fieldIndex}, null) " +
                            "else -> stmt.set$setterMethodName(${fieldIndex++}, entity.${subEl.simpleName})  }\n")
                }else {
                    parameterHolders.add("?")
                    bindCodeBlock.add("stmt.set$setterMethodName(${fieldIndex++}, entity.${subEl.simpleName})\n")
                }
            }

            val statementClause = if(upsertMode) {
                "\${when(_db.jdbcDbType) { DoorDbType.SQLITE -> \"INSERT·OR·REPLACE\" else -> \"INSERT\"} }"
            }else {
                "INSERT"
            }

            val upsertSuffix = if(upsertMode) {
                val nonPkFields = entityTypeEl.enclosedElements.filter { it.kind == ElementKind.FIELD && it.getAnnotation(PrimaryKey::class.java) == null }
                val nonPkFieldPairs = nonPkFields.map { "${it.simpleName}·=·excluded.${it.simpleName}" }
                val pkField = entityTypeEl.enclosedElements.firstOrNull { it.getAnnotation(PrimaryKey::class.java) != null }
                "\${when(_db.jdbcDbType){ DoorDbType.POSTGRES -> \"·ON·CONFLICT·(${pkField?.simpleName})·" +
                        "DO·UPDATE·SET·${nonPkFieldPairs.joinToString(separator = ",·")}\" " +
                        "else -> \"·\" } } "
            } else {
                ""
            }

            val sql = """
                $statementClause INTO ${entityTypeEl.simpleName} (${fieldNames.joinToString()})
                VALUES (${parameterHolders.joinToString()})
                $upsertSuffix
                """.trimIndent()

            val insertAdapterSpec = TypeSpec.anonymousClassBuilder()
                    .superclass(EntityInsertionAdapter::class.asClassName().parameterizedBy(entityClassName))
                    .addSuperclassConstructorParameter("_db.jdbcDbType")
                    .addFunction(FunSpec.builder("makeSql")
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

        codeBlock.add("_db.handleTableChanged(listOf(%S))\n", entityTypeEl.simpleName)

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
}