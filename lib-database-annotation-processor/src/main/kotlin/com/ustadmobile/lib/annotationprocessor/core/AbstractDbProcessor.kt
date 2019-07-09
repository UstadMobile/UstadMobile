package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import com.squareup.kotlinpoet.*
import java.lang.RuntimeException
import java.sql.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import com.ustadmobile.door.*
import org.sqlite.SQLiteDataSource
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element

abstract class AbstractDbProcessor: AbstractProcessor() {

    protected var messager: Messager? = null

    protected var dbConnection: Connection? = null

    protected val allKnownEntities = mutableListOf<TypeElement>()

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        messager = p0?.messager
    }

    /**
     * Run create
     */
    internal fun setupDb(roundEnv: RoundEnvironment) {
        val dbs = roundEnv!!.getElementsAnnotatedWith(Database::class.java)
        val dataSource = SQLiteDataSource()
        val dbTmpFile = File.createTempFile("dbprocessorkt", ".db")
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
                               enclosing: TypeElement, method: ExecutableElement,
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
            namedParams.forEach { execStmtSql = execStmtSql!!.replace(":$it", "null") }
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
            codeBlock.add("val _stmt = _con.prepareStatement($rawQueryVarName.getSql())\n")
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
            codeBlock.add("$rawQueryVarName.bindToPreparedStmt(_stmt)\n")
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
                            codeBlock.add("$entityVarName$fullPropName = _resultSet.$getterName(%S)\n", colName)
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
                    annotation = method.annotationMirrors.firstOrNull {it.annotationType.asTypeName() == Query::class.asTypeName()})
        }

        codeBlock.nextControlFlow("catch(_e: %T)", SQLException::class)
                .add("_e.printStackTrace()\n")
                .add("throw %T(_e)\n", RuntimeException::class)
                .nextControlFlow("finally")
                .add("_stmtToClose?.close()\n")
                .add("_conToClose?.close()\n")
                .endControlFlow()

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