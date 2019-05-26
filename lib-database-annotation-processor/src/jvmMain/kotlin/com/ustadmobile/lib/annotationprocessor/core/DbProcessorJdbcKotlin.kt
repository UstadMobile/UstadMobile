package com.ustadmobile.lib.annotationprocessor.core

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorJdbcKotlin.Companion.OPTION_OUTPUT_DIR
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.*
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.EntityInsertionAdapter
import javax.sql.DataSource
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import org.sqlite.SQLiteDataSource
import java.sql.*
import javax.tools.Diagnostic
import kotlin.coroutines.Continuation

val QUERY_SINGULAR_TYPES = listOf(INT, LONG, SHORT, BYTE, BOOLEAN, FLOAT, DOUBLE,
        String::class.asTypeName())


fun isList(type: TypeMirror, processingEnv: ProcessingEnvironment): Boolean =
        type.kind == TypeKind.DECLARED && (processingEnv.typeUtils.asElement(type) as TypeElement).qualifiedName.toString() == "java.util.List"



fun entityTypeFromFirstParam(method: ExecutableElement, enclosing: DeclaredType, processingEnv: ProcessingEnvironment) : TypeMirror {
    val methodResolved = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType
    val firstParamType = methodResolved.parameterTypes[0]
    if(isList(firstParamType, processingEnv)) {
        val firstType = (firstParamType as DeclaredType).typeArguments[0]
        return if(firstType is WildcardType) {
            firstType.extendsBound
        }else {
            firstType
        }
    }else if(firstParamType.kind == TypeKind.ARRAY) {
        return (firstParamType as ArrayType).componentType
    }else {
        return firstParamType
    }
}

/**
 * Given an input result type (e.g. Entity, Entity[], List<Entity>, String, int, etc), figure out
 * what the actual entity type is
 */
fun resolveEntityFromResultType(type: TypeName) =
        if(type is ParameterizedTypeName && type.rawType.canonicalName == "kotlin.collections.List") {
            type.typeArguments[0]
        }else {
            type
        }



fun pkgNameOfElement(element: Element, processingEnv: ProcessingEnvironment) =
        processingEnv.elementUtils.getPackageOf(element).qualifiedName.toString()

fun entityTypesOnDb(dbType: TypeElement, processingEnv: ProcessingEnvironment): MutableList<TypeElement> {
    val entityTypeElements = mutableListOf<TypeElement>()
    for (annotationMirror in dbType.getAnnotationMirrors()) {
        val annotationTypeEl = processingEnv.typeUtils
                .asElement(annotationMirror.getAnnotationType()) as TypeElement
        if (annotationTypeEl.qualifiedName.toString() != "androidx.room.Database")
            continue

        val annotationEntryMap = dbType.getAnnotationMirrors().get(0).getElementValues()
        for (entry in annotationEntryMap.entries) {
            val key = entry.key.getSimpleName().toString()
            val value = entry.value.getValue()
            if (key == "entities") {
                val typeMirrors = value as List<AnnotationValue>
                for (entityValue in typeMirrors) {
                    entityTypeElements.add(processingEnv.typeUtils
                            .asElement(entityValue.value as TypeMirror) as TypeElement)
                }
            }
        }
    }


    return entityTypeElements
}

/**
 * Returns a list of the entity fields of a particular object. If getAutoIncLast is true, then
 * any autoincrement primary key will always be returned at the end of the list, e.g. so that a
 * preparedstatement insert with or without an autoincrement id can share the same code to set
 * all other parameters.
 *
 * @param entityTypeElement The TypeElement representing the entity, from which we wish to get
 * the field names
 * @param getAutoIncLast if true, then always return any field that is auto increment at the very end
 * @return List of VariableElement representing the entity fields that are persisted
 */
fun getEntityFieldElements(entityTypeElement: TypeElement,
                           getAutoIncLast: Boolean): List<VariableElement> {
    val entityFieldsList = mutableListOf<VariableElement>()
    var pkAutoIncField: VariableElement? = null
    for (subElement in entityTypeElement.enclosedElements) {
        if (subElement.kind != ElementKind.FIELD || subElement.modifiers.contains(Modifier.STATIC))
            continue

        if (getAutoIncLast
                && subElement.getAnnotation(PrimaryKey::class.java) != null
                && subElement.getAnnotation(PrimaryKey::class.java).autoGenerate) {
            pkAutoIncField = subElement as VariableElement
        } else {
            entityFieldsList.add(subElement as VariableElement)
        }
    }

    if (pkAutoIncField != null)
        entityFieldsList.add(pkAutoIncField)

    return entityFieldsList
}

fun getFieldSqlType(fieldEl: VariableElement, processingEnv: ProcessingEnvironment, dbType: Int = 0): String {
    when(fieldEl.asType().kind){
        TypeKind.BOOLEAN -> return "BOOL"
        TypeKind.INT -> return "INTEGER"
        TypeKind.LONG -> return "BIGINT"
        TypeKind.FLOAT -> return "FLOAT"
        TypeKind.DECLARED -> {
            val fieldClassName = (processingEnv.typeUtils.asElement(fieldEl.asType()) as TypeElement).qualifiedName.toString()
            return if (fieldClassName == "java.lang.String") "TEXT" else "UNKNOWN"
        }
    }

    return "UNKNOWN"
}

//As per https://github.com/square/kotlinpoet/issues/236
private fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
    (rawType.javaToKotlinType() as ClassName).parameterizedBy(
            *typeArguments.map { it.javaToKotlinType() }.toTypedArray()
    )
} else {
    val className = JavaToKotlinClassMap.INSTANCE
            .mapJavaToKotlin(FqName(toString()))?.asSingleFqName()?.asString()
    if (className == null) this
    else ClassName.bestGuess(className)
}


fun overrideAndConvertToKotlinTypes(method: ExecutableElement, enclosing: DeclaredType,
                                    processingEnv: ProcessingEnvironment, forceNullableReturn: Boolean = false): FunSpec.Builder {

    val funSpec = FunSpec.builder(method.simpleName.toString())
            .addModifiers(KModifier.OVERRIDE)
    val resolvedExecutableType = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType

    var suspendedReturnType = null as TypeName?
    var suspendedParamEl = null as VariableElement?
    for(i in 0 until method.parameters.size) {
        val resolvedTypeName = resolvedExecutableType.parameterTypes[i].asTypeName().javaToKotlinType()

        if(isContinuationParam(resolvedTypeName)) {
            suspendedParamEl= method.parameters[i]
            suspendedReturnType = suspendedReturnTypeFromContinuationParam(resolvedTypeName)
            funSpec.addModifiers(KModifier.SUSPEND)
        }else {
            funSpec.addParameter(method.parameters[i].simpleName.toString(), resolvedTypeName)
        }
    }

    if(suspendedReturnType != null && suspendedReturnType != UNIT) {
        funSpec.returns(suspendedReturnType.copy(nullable = forceNullableReturn
                || suspendedParamEl?.getAnnotation(Nullable::class.java) != null))
    }else if(suspendedReturnType == null) {
        funSpec.returns(resolvedExecutableType.returnType.asTypeName().javaToKotlinType()
                .copy(nullable = forceNullableReturn || method.getAnnotation(Nullable::class.java) != null))
    }

    return funSpec
}

fun isContinuationParam(paramTypeName: TypeName) = paramTypeName is ParameterizedTypeName &&
        paramTypeName.rawType.canonicalName == "kotlin.coroutines.Continuation"

fun suspendedReturnTypeFromContinuationParam(continuationParam: TypeName) =
        (((continuationParam as ParameterizedTypeName).typeArguments[0] as WildcardTypeName).inTypes[0] as ClassName).javaToKotlinType()

/**
 * Figures out the return type of a method. This will also figure out the return type of a suspended method
 */
fun resolveReturnTypeIfSuspended(method: ExecutableType) : TypeName {
    val continuationParam = method.parameterTypes.firstOrNull { isContinuationParam(it.asTypeName()) }
    return if(continuationParam != null) {
        suspendedReturnTypeFromContinuationParam(continuationParam.asTypeName())
    }else {
        method.returnType.asTypeName()
    }
}

/**
 * If the return type is LiveData, Factory, etc. then unwrap that into the result type.
 */
fun resolveQueryResultType(returnTypeName: TypeName)  =
        if(returnTypeName is ParameterizedTypeName && returnTypeName.rawType.canonicalName == "androidx.lifecycle.LiveData") {
            returnTypeName.typeArguments[0]
        }else {
            returnTypeName
        }

fun makeInsertAdapterMethodName(paramType: TypeMirror, returnType: TypeName, processingEnv: ProcessingEnvironment): String {
    var methodName = "insert"
    if(isList(paramType, processingEnv)) {
        methodName += "List"
        if(returnType != UNIT)
            methodName += "AndReturnIds"
    }else {
        if(returnType != UNIT) {
            methodName += "AndReturnId"
        }
    }

    return methodName
}

private fun getPreparedStatementSetterGetterTypeName(typeName: TypeName): String? {
    when(typeName.javaToKotlinType()) {
        INT -> return "Int"
        BYTE -> return "Byte"
        LONG -> return "Long"
        FLOAT -> return "Float"
        DOUBLE -> return "Double"
        BOOLEAN -> return "Boolean"
        String::class.asTypeName() -> return "String"
        else -> return "UNKNOWN"
    }
}

/**
 * For SQL with named parameters (e.g. "SELECT * FROM Table WHERE uid = :paramName") return a
 * list of all named parameters.
 *
 * @param querySql SQL that may contain named parameters
 * @return String list of named parameters (e.g. "paramName"). Empty if no named parameters are present.
 */
private fun getQueryNamedParameters(querySql: String): List<String> {
    val namedParams = mutableListOf<String>()
    var insideQuote = false
    var insideDoubleQuote = false
    var lastC: Char = 0.toChar()
    var startNamedParam = -1
    for (i in 0 until querySql.length) {
        val c = querySql[i]
        if (c == '\'' && lastC != '\\')
            insideQuote = !insideQuote
        if (c == '\"' && lastC != '\\')
            insideDoubleQuote = !insideDoubleQuote

        if (!insideQuote && !insideDoubleQuote) {
            if (c == ':') {
                startNamedParam = i
            } else if (!(Character.isLetterOrDigit(c) || c == '_') && startNamedParam != -1) {
                //process the parameter
                namedParams.add(querySql.substring(startNamedParam + 1, i))
                startNamedParam = -1
            } else if (i == querySql.length - 1 && startNamedParam != -1) {
                namedParams.add(querySql.substring(startNamedParam + 1, i + 1))
                startNamedParam = -1
            }
        }


        lastC = c
    }

    return namedParams
}

/**
 * Generate a map of all the fields that can be set on the given entity
 */
data class EntityFieldMap(val fieldMap: Map<String, Element>, val embeddedVarsList: List<Pair<String, Element>>)
fun mapEntityFields(entityTypeEl: TypeElement, prefix: String = "",
                           fieldMap: MutableMap<String, Element> = mutableMapOf(),
                           embeddedVarsList: MutableList<Pair<String, Element>> = mutableListOf(),
                           processingEnv: ProcessingEnvironment): EntityFieldMap {

    ancestorsToList(entityTypeEl, processingEnv).forEach {
        val listParted = it.enclosedElements.filter { it.kind == ElementKind.FIELD }.partition { it.getAnnotation(Embedded::class.java) == null }
        listParted.first.forEach { fieldMap["$prefix.${it.simpleName}"] = it}
        listParted.second.forEach {
            embeddedVarsList.add(Pair("$prefix.${it.simpleName}", it))
            mapEntityFields(processingEnv.typeUtils.asElement(it.asType()) as TypeElement,
                    "$prefix.${it.simpleName}!!", fieldMap, embeddedVarsList, processingEnv)
        }
    }

    return EntityFieldMap(fieldMap, embeddedVarsList)
}

/**
 *
 */
private fun ancestorsToList(child: TypeElement, processingEnv: ProcessingEnvironment): List<TypeElement> {
    val entityAncestors = mutableListOf<TypeElement>()

    var nextEntity = child as TypeElement?

    do {
        entityAncestors.add(nextEntity!!)
        val nextElement = processingEnv.typeUtils.asElement(nextEntity.superclass)
        nextEntity = if(nextElement is TypeElement) { nextElement } else { null }
    }while(nextEntity != null)

    return entityAncestors
}

fun defaultVal(typeName: TypeName) : CodeBlock {
    val codeBlock = CodeBlock.builder()
    val kotlinType = typeName.javaToKotlinType()
    when(kotlinType) {
        INT -> codeBlock.add("0")
        LONG -> codeBlock.add("0L")
        BYTE -> codeBlock.add("0.toByte()")
        String::class.asTypeName() -> codeBlock.add("null as String?")
        else -> {
            if(kotlinType is ParameterizedTypeName && kotlinType.rawType == List::class.asClassName()) {
                codeBlock.add("mutableListOf<%T>()", kotlinType.typeArguments[0])
            }else {
                codeBlock.add("null as %T?", typeName)
            }
        }
    }

    return codeBlock.build()
}

fun isListOrArray(typeName: TypeName) = (typeName is ClassName && typeName.canonicalName =="kotlin.Array")
        || (typeName is ParameterizedTypeName && typeName.rawType == List::class.asClassName())


val PRIMITIVE = listOf(INT, LONG, BOOLEAN, SHORT, BYTE, FLOAT, DOUBLE)


@SupportedAnnotationTypes("androidx.room.Database")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(OPTION_OUTPUT_DIR)
class DbProcessorJdbcKotlin: AbstractProcessor() {

    private var messager: Messager? = null

    private var dbConnection: Connection? = null

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        messager = p0?.messager
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val dbs = roundEnv!!.getElementsAnnotatedWith(Database::class.java)
        val outputArg = processingEnv.options[OPTION_OUTPUT_DIR]
        val outputDir = if(outputArg == "filer") processingEnv.options["kapt.kotlin.generated"] else outputArg

        val dataSource = SQLiteDataSource()
        val dbTmpFile = File.createTempFile("dbprocessorkt", ".db")
        dataSource.url = "jdbc:sqlite:${dbTmpFile.absolutePath}"
        messager!!.printMessage(Diagnostic.Kind.NOTE, "Annotation processor db tmp file: ${dbTmpFile.absolutePath}")

        for(dbTypeEl in dbs) {
            val dbFileSpec = generateDbImplClass(dbTypeEl as TypeElement)
            dbFileSpec.writeTo(File(outputDir))
        }

        dbConnection = dataSource.connection
        dbs.flatMap { entityTypesOnDb(it as TypeElement, processingEnv) }.forEach {
            val stmt = dbConnection!!.createStatement()
            stmt.execute(makeCreateTableStatement(it, DoorDbType.SQLITE))
        }

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        for(daoElement in daos) {
            val daoTypeEl = daoElement as TypeElement
            val daoFileSpec = generateDaoImplClass(daoTypeEl)
            daoFileSpec.writeTo(File(outputDir))
        }

        return true
    }


    fun generateDaoImplClass(daoTypeElement: TypeElement): FileSpec {
        val daoImplFile = FileSpec.builder(pkgNameOfElement(daoTypeElement, processingEnv),
                "${daoTypeElement.simpleName}_$SUFFIX_JDBC_KT")
        daoImplFile.addImport("com.ustadmobile.door", "DoorDbType")
        val daoImpl = TypeSpec.classBuilder("${daoTypeElement.simpleName}_$SUFFIX_JDBC_KT")
                .primaryConstructor(FunSpec.constructorBuilder().addParameter("_db",
                        DoorDatabase::class).build())
                .addProperty(PropertySpec.builder("_db", DoorDatabase::class).initializer("_db").build())
                .superclass(daoTypeElement.asClassName())

        for(daoSubEl in daoTypeElement.enclosedElements) {
            if(daoSubEl.kind != ElementKind.METHOD)
                continue

            val daoMethod = daoSubEl as ExecutableElement
            if(daoMethod.getAnnotation(Insert::class.java) != null) {
                daoImpl.addFunction(generateInsertFun(daoTypeElement, daoMethod, daoImpl))
            }else if(daoMethod.getAnnotation(Query::class.java) != null) {
                daoImpl.addFunction(generateQueryFun(daoTypeElement, daoMethod, daoImpl))
            }else if(daoMethod.getAnnotation(Update::class.java) != null) {
                daoImpl.addFunction(generateUpdateFun(daoTypeElement, daoMethod, daoImpl))
            }
        }



        daoImplFile.addType(daoImpl.build())
        return daoImplFile.build()
    }


    fun generateDbImplClass(dbTypeElement: TypeElement): FileSpec {
        val dbImplFile = FileSpec.builder(pkgNameOfElement(dbTypeElement, processingEnv),
                "${dbTypeElement.simpleName}_$SUFFIX_JDBC_KT")


        val dbImplType = TypeSpec.classBuilder("${dbTypeElement.simpleName}_$SUFFIX_JDBC_KT")
                .superclass(dbTypeElement.asClassName())
                .addFunction(FunSpec.constructorBuilder()
                        .addParameter("dataSource", DataSource::class)
                        .addCode("this.dataSource = dataSource\n")
                        .addCode("setupFromDataSource()\n").build())
        dbImplType.addFunction(generateCreateTablesFun(dbTypeElement))
        dbImplType.addFunction(generateClearAllTablesFun(dbTypeElement))

        for(subEl in dbTypeElement.enclosedElements) {
            if(subEl.kind != ElementKind.METHOD)
                continue

            val methodEl = subEl as ExecutableElement
            val daoTypeEl = processingEnv.typeUtils.asElement(methodEl.returnType)
            if(!methodEl.modifiers.contains(Modifier.ABSTRACT))
                continue

            val daoImplClassName = ClassName(pkgNameOfElement(daoTypeEl, processingEnv),
                    "${daoTypeEl.simpleName}_$SUFFIX_JDBC_KT")

            dbImplType.addProperty(PropertySpec.builder("_${daoTypeEl.simpleName}",
                    daoImplClassName).delegate("lazy { %T(this) }", daoImplClassName).build())

            dbImplType.addFunction(FunSpec.overriding(methodEl)
                    .addStatement("return _${daoTypeEl.simpleName}").build())
        }


        dbImplFile.addType(dbImplType.build())

        return dbImplFile.build()
    }

    fun generateCreateTablesFun(dbTypeElement: TypeElement): FunSpec {
        val createTablesFunSpec = FunSpec.builder("createAllTables")
                .addModifiers(KModifier.OVERRIDE)
        val initDbVersion = dbTypeElement.getAnnotation(Database::class.java).version
        val codeBlock = CodeBlock.builder()
        codeBlock.add("var _con = null as %T?\n", Connection::class)
                .add("var _stmt = null as %T?\n", Statement::class)
                .beginControlFlow("try")
                .add("_con = openConnection()!!\n")
                .add("_stmt = _con.createStatement()!!\n")
                .beginControlFlow("when(jdbcDbType)")

        for(dbProductType in DoorDbType.SUPPORTED_TYPES) {
            codeBlock.beginControlFlow("$dbProductType -> ")
                    .add("// - create for this $dbProductType \n")

            codeBlock.add("_stmt.executeUpdate(\"CREATE·TABLE·IF·NOT·EXISTS·${DoorDatabase.DBINFO_TABLENAME}" +
                    "·(dbVersion·int·primary·key,·dbHash·varchar(255))\")\n")
            codeBlock.add("_stmt.executeUpdate(\"INSERT·INTO·${DoorDatabase.DBINFO_TABLENAME}·" +
                    "VALUES·($initDbVersion,·'')\")\n")
            val dbEntityTypes = entityTypesOnDb(dbTypeElement, processingEnv)
            for(entityType in dbEntityTypes) {
                codeBlock.add("_stmt.executeUpdate(%S)\n", makeCreateTableStatement(entityType,
                        dbProductType))

                for(field in getEntityFieldElements(entityType, false)) {
                    if(field.getAnnotation(ColumnInfo::class.java)?.index == true) {
                        codeBlock.add("_stmt.executeUpdate(%S)\n",
                                "CREATE INDEX index_${entityType.simpleName}_${field.simpleName} ON ${entityType.simpleName} (${field.simpleName})")
                    }
                }
            }

            codeBlock.endControlFlow()
        }

        codeBlock.endControlFlow() //end when
                .nextControlFlow("finally")
                .add("_stmt?.close()\n")
                .add("_con?.close()\n")
                .endControlFlow()
        return createTablesFunSpec.addCode(codeBlock.build()).build()
    }

    fun generateClearAllTablesFun(dbTypeElement: TypeElement): FunSpec {
        val dropFunSpec = FunSpec.builder("clearAllTables")
                .addModifiers(KModifier.OVERRIDE)
                .addCode("var _con = null as %T?\n", Connection::class)
                .addCode("var _stmt = null as %T?\n", Statement::class)
                .beginControlFlow("try")
                .addCode("_con = openConnection()\n")
                .addCode("_stmt = _con!!.createStatement()\n")
        for(entityType in entityTypesOnDb(dbTypeElement, processingEnv)) {
            dropFunSpec.addCode("_stmt!!.executeUpdate(%S)\n", "DELETE FROM ${entityType.simpleName}")
        }
        dropFunSpec.nextControlFlow("finally")
                .addCode("_stmt?.close()\n")
                .addCode("_con?.close()\n")
                .endControlFlow()

        return dropFunSpec.build()
    }


    private fun makeCreateTableStatement(entitySpec: TypeElement, dbType: Int): String {
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


    fun generateInsertFun(daoTypeElement: TypeElement, daoMethod: ExecutableElement, daoTypeBuilder: TypeSpec.Builder): FunSpec {
        val insertFun = overrideAndConvertToKotlinTypes(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod) as ExecutableType


        val entityType = entityTypeFromFirstParam(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val entityTypeEl = processingEnv.typeUtils.asElement(entityType) as TypeElement

        val upsertMode = daoMethod.getAnnotation(Insert::class.java).onConflict == OnConflictStrategy.REPLACE
        val entityInserterPropName = "_insertAdapter${entityTypeEl.simpleName}_${if(upsertMode) "upsert" else ""}"
        if(!daoTypeBuilder.propertySpecs.any { it.name == entityInserterPropName }) {
            val fieldNames = mutableListOf<String>()
            val parameterHolders = mutableListOf<String>()

            val bindCodeBlock = CodeBlock.builder().add("var _fieldIndex = 1\n")

            for(subEl in entityTypeEl.enclosedElements) {
                if(subEl.kind != ElementKind.FIELD)
                    continue

                fieldNames.add(subEl.simpleName.toString())
                val pkAnnotation = subEl.getAnnotation(PrimaryKey::class.java)
                if(pkAnnotation != null && pkAnnotation.autoGenerate) {
                    parameterHolders.add("\${when(_db.jdbcDbType) { DoorDbType.POSTGRES -> \"COALESCE(?,nextval('${entityTypeEl.simpleName}'))\" else -> \"?\"} }")
                    bindCodeBlock.add("when(entity.${subEl.simpleName}){ 0L -> stmt.setObject(_fieldIndex++, null) else -> stmt.setLong(_fieldIndex++, entity.${subEl.simpleName})  }\n")
                }else {
                    parameterHolders.add("?")
                    bindCodeBlock.add("stmt.set${getPreparedStatementSetterGetterTypeName(subEl.asType().asTypeName())}" +
                        "(_fieldIndex++, entity.${subEl.simpleName})\n")
                }
            }

            val upsertSuffix = if(upsertMode) {
                val nonPkFields = entityTypeEl.enclosedElements.filter { it.kind == ElementKind.FIELD && it.getAnnotation(PrimaryKey::class.java) == null }
                val nonPkFieldPairs = nonPkFields.map { "${it.simpleName}·=·excluded.${it.simpleName}" }
                val pkField = entityTypeEl.enclosedElements.firstOrNull { it.getAnnotation(PrimaryKey::class.java) != null }
                "\${when(_db.jdbcDbType){ DoorDbType.POSTGRES -> \"·ON·CONFLICT·(${pkField?.simpleName})·" +
                        "DO·UPDATE·SET·${nonPkFieldPairs.joinToString(separator = ",·")}\" " +
                        "else -> \"·ON CONFLICT REPLACE\" } } "
            } else {
                ""
            }

            val sql = """
                INSERT INTO ${entityTypeEl.simpleName} (${fieldNames.joinToString()})
                VALUES (${parameterHolders.joinToString()})
                $upsertSuffix
                """.trimIndent()

            val insertAdapterSpec = TypeSpec.anonymousClassBuilder()
                    .superclass(EntityInsertionAdapter::class.asClassName().parameterizedBy(entityType.asTypeName()))
                    .addSuperclassConstructorParameter("_db.jdbcDbType")
                    .addFunction(FunSpec.builder("makeSql")
                            .addModifiers(KModifier.OVERRIDE)
                            .addCode("return \"\"\"%L\"\"\"", sql).build())
                    .addFunction(FunSpec.builder("bindPreparedStmtToEntity")
                            .addModifiers(KModifier.OVERRIDE)
                            .addParameter("stmt", PreparedStatement::class)
                            .addParameter("entity", entityType.asTypeName())
                            .addCode(bindCodeBlock.build()).build())

            daoTypeBuilder.addProperty(PropertySpec.builder(entityInserterPropName,
                    EntityInsertionAdapter::class.asClassName().parameterizedBy(entityType.asTypeName()))
                    .initializer("%L", insertAdapterSpec.build())
                    .build())
        }


        val returnType = daoMethodResolved.returnType

        if(returnType.kind != TypeKind.VOID) {
            insertFun.addCode("return ")
        }

        val resolvedReturnType = resolveReturnTypeIfSuspended(daoMethodResolved)
        val insertMethodName = makeInsertAdapterMethodName(daoMethodResolved.parameterTypes[0],
                resolvedReturnType, processingEnv)
        insertFun.addCode("$entityInserterPropName.$insertMethodName(${daoMethod.parameters[0].simpleName}, _db.openConnection())\n")
        return insertFun.build()
    }

    fun generateQueryFun(daoTypeElement: TypeElement, daoMethod: ExecutableElement, daoTypeBuilder: TypeSpec.Builder) : FunSpec {
        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod)  as ExecutableType

        // The return type of the method - e.g. List<Entity>, LiveData<List<Entity>>, String, etc.
        val returnTypeResolved = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType().javaToKotlinType()

        val resultType = resolveQueryResultType(returnTypeResolved)

        val funSpec = overrideAndConvertToKotlinTypes(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv, forceNullableReturn = !PRIMITIVE.contains(resultType) && !isListOrArray(resultType))

        val querySql = daoMethod.getAnnotation(Query::class.java).value

        val paramTypesResolved = daoMethodResolved.parameterTypes


        //TODO: This could be replaced with a bit of mapIndexed + filters
        val queryVarsMap = mutableMapOf<String, TypeName>()
        for(i in 0 until daoMethod.parameters.size) {
            if (!isContinuationParam(paramTypesResolved[i].asTypeName())) {
                queryVarsMap[daoMethod.parameters[i].simpleName.toString()] = paramTypesResolved[i].asTypeName()
            }
        }


        funSpec.addCode(generateQueryCodeBlock(returnTypeResolved, queryVarsMap, querySql,
                daoTypeElement, daoMethod))

        if(returnTypeResolved != UNIT){
            funSpec.addCode("return _result\n")
        }

        return funSpec.build()
    }

    fun generateUpdateFun(daoTypeElement: TypeElement, daoMethod: ExecutableElement, daoTypeBuilder: TypeSpec.Builder) : FunSpec {
        val updateFun = overrideAndConvertToKotlinTypes(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod) as ExecutableType

        val entityType = entityTypeFromFirstParam(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val entityTypeEl = processingEnv.typeUtils.asElement(entityType) as TypeElement

        val resolvedReturnType = resolveReturnTypeIfSuspended(daoMethodResolved)

        val codeBlock = CodeBlock.builder()

        val pkEl = entityTypeEl.enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null }
        val nonPkFields = entityTypeEl.enclosedElements.filter { it.kind == ElementKind.FIELD && it.getAnnotation(PrimaryKey::class.java) == null }
        val sqlSetPart = nonPkFields.map { "${it.simpleName} = ?" }.joinToString()
        val sqlStmt  = "UPDATE ${entityTypeEl.simpleName} SET $sqlSetPart WHERE ${pkEl.simpleName} = ?"


        if(resolvedReturnType != UNIT)
            codeBlock.add("var _result = ${defaultVal(resolvedReturnType)}\n")

        codeBlock.add("var _con = null as %T?\n", Connection::class)
                .add("var _stmt = null as %T?\n", Statement::class)
                .beginControlFlow("try")
                .add("_con = _db.openConnection()!!\n")
                .add("_stmt = _con.prepareStatement(%S)!!\n", sqlStmt)

        var entityVarName = daoMethod.parameters[0].simpleName.toString()
        if(isListOrArray(entityType.asTypeName())) {
            codeBlock.add("_con.autoCommit = false\n")
                    .beginControlFlow("for(_entity in ${daoMethod.parameters[0].simpleName})")
            entityVarName = "_entity"
        }

        var fieldIndex = 1
        val fieldSetFn = { it : Element ->
            codeBlock.add("_stmt.set${getPreparedStatementSetterGetterTypeName(it.asType().asTypeName())}(${fieldIndex++}, $entityVarName.${it.simpleName})\n")
            Unit
        }
        nonPkFields.forEach(fieldSetFn)
        fieldSetFn(pkEl)

        if(resolvedReturnType != UNIT)
            codeBlock.add("result += ")

        codeBlock.add("_stmt.executeUpdate()\n")

        if(isListOrArray(entityType.asTypeName())) {
            codeBlock.add("_con.commit()\n")
                    .endControlFlow()
        }

        codeBlock.nextControlFlow("finally")
                .add("_stmt?.close()\n")
                .add("_con?.close()\n")
                .endControlFlow()

        if(resolvedReturnType != UNIT)
            codeBlock.add("return _result\n")

        updateFun.addCode(codeBlock.build())
        return updateFun.build()
    }


    fun generateQueryCodeBlock(returnType: TypeName, queryVars: Map<String, TypeName>, querySql: String,
                               enclosing: TypeElement, method: ExecutableElement): CodeBlock {
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
            mapEntityFields(entityTypeEl = entityTypeElement as TypeElement, processingEnv = processingEnv)
        }else {
            null
        }


        val codeBlock = CodeBlock.builder()

        val resultVarName = "_result"

        val namedParams = getQueryNamedParameters(querySql)

        var preparedStatementSql = querySql
        namedParams.forEach { preparedStatementSql = preparedStatementSql.replace(":$it", "?") }

        if(resultType != UNIT)
            codeBlock.add("var $resultVarName = ${defaultVal(resultType)}\n")

        codeBlock.add("var _con = null as %T?\n", Connection::class)
                .add("var _stmt = null as %T?\n", PreparedStatement::class)
                .add("var _resultSet = null as %T?\n", ResultSet::class)
                .beginControlFlow("try")
                .add("_con = _db.openConnection()\n")
                .add("_stmt = _con.prepareStatement(%S)\n", preparedStatementSql)


        var paramIndex = 1
        queryVars.forEach {
            codeBlock.add("_stmt.set${getPreparedStatementSetterGetterTypeName(it.value)}(${paramIndex++}, " +
                    "${it.key})\n")
        }

        codeBlock.add("_resultSet = _stmt.executeQuery()\n")

        var execStmtSql = querySql
        namedParams.forEach { execStmtSql = execStmtSql.replace(":$it", "null") }

        var resultSet = null as ResultSet?
        var execStmt = null as Statement?
        try {
            execStmt = dbConnection?.createStatement()
            resultSet = execStmt?.executeQuery(execStmtSql)
            val metaData = resultSet!!.metaData
            val colNames = mutableListOf<String>()
            for(i in 1 .. metaData.columnCount) {
                colNames.add(metaData.getColumnName(i))
            }

            var entityVarName = ""
            if(isListOrArray(returnType)) {
                codeBlock.beginControlFlow("while(_resultSet.next())")
                        .add("val _entity = %T()\n", entityType)
                entityVarName = "_entity"
            }else {
                codeBlock.beginControlFlow("if(_resultSet.next())")
                        .add("$resultVarName = %T()\n", entityType)
                entityVarName = resultVarName
            }

            if(QUERY_SINGULAR_TYPES.contains(entityType)) {
                codeBlock.add("$entityVarName = _resultSet.get${getPreparedStatementSetterGetterTypeName(entityType)}(1)\n")
            }else {
                // Map of the last prop name (e.g. name) to the full property name as it will
                // be generated (e.g. embedded!!.name)
                val colNameLastToFullMap = entityFieldMap!!.fieldMap.map { it.key.substringAfterLast('.') to it.key}.toMap()

                entityFieldMap.embeddedVarsList.forEach {
                    codeBlock.add("$entityVarName${it.first} = %T()\n", it.second.asType())
                }

                colNames.forEach {colName ->
                    val fullPropName = colNameLastToFullMap[colName]
                    val propType = entityFieldMap.fieldMap[fullPropName]
                    val getterName = "get${getPreparedStatementSetterGetterTypeName(propType!!.asType().asTypeName()) }"
                    codeBlock.add("$entityVarName$fullPropName = _resultSet.$getterName(%S)\n", colName)
                }
            }

            if(isListOrArray(returnType)) {
                codeBlock.add("$resultVarName.add(_entity)\n")
            }

            codeBlock.endControlFlow()

        }catch(e: SQLException) {
            messager!!.printMessage(Diagnostic.Kind.ERROR, "${makeLogPrefix(enclosing, method)} " +
                    "Exception running query SQL '$execStmtSql' : ${e.message}")
        }

        codeBlock.nextControlFlow("finally")
                .add("_stmt?.close()\n")
                .add("_con?.close()\n")
                .endControlFlow()

        return codeBlock.build()
    }

    fun makeLogPrefix(enclosing: TypeElement, method: ExecutableElement) = "DoorDb: ${enclosing.qualifiedName}. ${method.simpleName} "

    companion object {

        const val OPTION_OUTPUT_DIR = "door_jdbc_kt_out"

        const val SUFFIX_JDBC_KT = "JdbcKt"

    }
}