package com.ustadmobile.lib.annotationprocessor.core

import android.arch.persistence.room.ColumnInfo
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorJdbcKotlin.Companion.OPTION_OUTPUT_DIR
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.*
import javax.sql.DataSource
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.*
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.statement.select.Select
import net.sf.jsqlparser.util.TablesNamesFinder
import org.jetbrains.annotations.Nullable
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import org.sqlite.SQLiteDataSource
import java.lang.RuntimeException
import java.sql.*
import javax.lang.model.util.SimpleTypeVisitor7
import javax.tools.Diagnostic

val QUERY_SINGULAR_TYPES = listOf(INT, LONG, SHORT, BYTE, BOOLEAN, FLOAT, DOUBLE,
        String::class.asTypeName(), String::class.asTypeName().copy(nullable = true))


fun isList(type: TypeMirror, processingEnv: ProcessingEnvironment): Boolean =
        type.kind == TypeKind.DECLARED && (processingEnv.typeUtils.asElement(type) as TypeElement).qualifiedName.toString() == "java.util.List"



fun entityTypeFromFirstParam(method: ExecutableElement, enclosing: DeclaredType, processingEnv: ProcessingEnvironment) : TypeMirror {
    val methodResolved = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType
    if(methodResolved.parameterTypes.isEmpty()) {
        return processingEnv.typeUtils.nullType
    }

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
internal fun TypeName.javaToKotlinType(): TypeName = if (this is ParameterizedTypeName) {
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
                                    processingEnv: ProcessingEnvironment, forceNullableReturn: Boolean = false,
                                    forceNullableParameterTypeArgs: Boolean = false): FunSpec.Builder {

    val funSpec = FunSpec.builder(method.simpleName.toString())
            .addModifiers(KModifier.OVERRIDE)
    val resolvedExecutableType = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType

    var suspendedReturnType = null as TypeName?
    var suspendedParamEl = null as VariableElement?
    for(i in 0 until method.parameters.size) {
        val resolvedTypeName = resolvedExecutableType.parameterTypes[i].asTypeName().javaToKotlinType()

        if(isContinuationParam(resolvedTypeName)) {
            suspendedParamEl= method.parameters[i]
            suspendedReturnType = resolveReturnTypeIfSuspended(resolvedExecutableType)
            funSpec.addModifiers(KModifier.SUSPEND)
        }else {
            funSpec.addParameter(method.parameters[i].simpleName.toString(),
                    resolvedTypeName.copy(nullable = (method.parameters[i].getAnnotation(Nullable::class.java) != null)))
        }
    }

    if(suspendedReturnType != null && suspendedReturnType != UNIT) {
        funSpec.returns(suspendedReturnType.copy(nullable = forceNullableReturn
                || suspendedParamEl?.getAnnotation(Nullable::class.java) != null))
    }else if(suspendedReturnType == null) {
        var returnType = resolvedExecutableType.returnType.asTypeName().javaToKotlinType()
                .copy(nullable = forceNullableReturn || method.getAnnotation(Nullable::class.java) != null)
        if(forceNullableParameterTypeArgs && returnType is ParameterizedTypeName) {
            returnType = returnType.rawType.parameterizedBy(*returnType.typeArguments.map { it.copy(nullable = true)}.toTypedArray())
        }

        funSpec.returns(returnType)
    }

    return funSpec
}

fun isContinuationParam(paramTypeName: TypeName) = paramTypeName is ParameterizedTypeName &&
        paramTypeName.rawType.canonicalName == "kotlin.coroutines.Continuation"

/**
 * Figures out the return type of a method. This will also figure out the return type of a suspended method
 */
fun resolveReturnTypeIfSuspended(method: ExecutableType) : TypeName {
    val continuationParam = method.parameterTypes.firstOrNull { isContinuationParam(it.asTypeName()) }
    return if(continuationParam != null) {
        //The continuation parameter is always the last parameter, and has one type argument
        val contReturnType = (method.parameterTypes.last() as DeclaredType).typeArguments.first().extendsBoundOrSelf().asTypeName()
        removeTypeProjection(contReturnType)
        //Open classes can result in <out T> being generated instead of just <T>. Therefor we want to remove the wildcard
    }else {
        method.returnType.asTypeName().javaToKotlinType()
    }
}

/**
 * Remove <out T>
 */
fun removeTypeProjection(typeName: TypeName) =
    if(typeName is ParameterizedTypeName && typeName.typeArguments[0] is WildcardTypeName) {
        typeName.rawType.parameterizedBy((typeName.typeArguments[0] as WildcardTypeName).outTypes[0]).javaToKotlinType()
    }else {
        typeName.javaToKotlinType()
    }


/**
 * If the return type is LiveData, Factory, etc. then unwrap that into the result type.
 */
fun resolveQueryResultType(returnTypeName: TypeName)  =
        if(returnTypeName is ParameterizedTypeName
                && returnTypeName.rawType == DoorLiveData::class.asClassName()) {
            returnTypeName.typeArguments[0]
        }else if(returnTypeName is ParameterizedTypeName
                && returnTypeName.rawType == androidx.paging.DataSource.Factory::class.asClassName()) {
            List::class.asClassName().parameterizedBy(returnTypeName.typeArguments[1])
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

fun getPreparedStatementSetterGetterTypeName(typeName: TypeName): String? {
    val kotlinType = typeName.javaToKotlinType()
    when(kotlinType) {
        INT -> return "Int"
        BYTE -> return "Byte"
        LONG -> return "Long"
        FLOAT -> return "Float"
        DOUBLE -> return "Double"
        BOOLEAN -> return "Boolean"
        String::class.asTypeName() -> return "String"
        else -> {
            if(isListOrArray(kotlinType)) {
                return "Array"
            }else {
                return "UNKNOWN"
            }
        }
    }
}

/**
 * For SQL with named parameters (e.g. "SELECT * FROM Table WHERE uid = :paramName") return a
 * list of all named parameters.
 *
 * @param querySql SQL that may contain named parameters
 * @return String list of named parameters (e.g. "paramName"). Empty if no named parameters are present.
 */
fun getQueryNamedParameters(querySql: String): List<String> {
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
        BOOLEAN -> codeBlock.add("false")
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

fun isDataSourceFactory(typeName: TypeName) = typeName is ParameterizedTypeName
        && typeName.rawType == androidx.paging.DataSource.Factory::class.asTypeName()


fun isLiveData(typeName: TypeName) = (typeName is ParameterizedTypeName
        && typeName.rawType == DoorLiveData::class.asClassName())

val SQL_COMPONENT_TYPE_MAP = mapOf(LONG to "BIGINT",
        INT to "INTEGER",
        SHORT to "SMALLINT",
        BOOLEAN to "BOOLEAN",
        FLOAT to "FLOAT",
        DOUBLE to "DOUBLE",
        String::class.asClassName() to "TEXT")

fun sqlArrayComponentTypeOf(typeName: TypeName): String {
    if(typeName is ParameterizedTypeName) {
        return SQL_COMPONENT_TYPE_MAP.get(typeName.typeArguments[0])!!
    }

    return "UNKNOWN"
}

//Limitation: this does not currently support interface inheritence
data class MethodToImplement(val methodName: String, val paramTypes: List<TypeMirror>)

fun methodsToImplement(typeElement: TypeElement, enclosing: DeclaredType, processingEnv: ProcessingEnvironment) :List<Element> {
    return ancestorsToList(typeElement, processingEnv).flatMap {
        it.enclosedElements.filter {
            it.kind ==  ElementKind.METHOD && Modifier.ABSTRACT in it.modifiers //abstract methods in this class
        } + it.interfaces.flatMap {
            processingEnv.typeUtils.asElement(it).enclosedElements.filter { it.kind == ElementKind.METHOD } //methods from the interface
        }
    }.filter {
        !isMethodImplemented(it as ExecutableElement, typeElement, processingEnv)
    }.distinctBy {
        val signatureParamTypes = (processingEnv.typeUtils.asMemberOf(enclosing, it) as ExecutableType)
                .parameterTypes.filter { ! isContinuationParam(it.asTypeName()) }
        MethodToImplement(it.simpleName.toString(), signatureParamTypes)
    }
}

fun isMethodImplemented(method: ExecutableElement, enclosingClass: TypeElement, processingEnv: ProcessingEnvironment): Boolean {
    val enclosingClassType = enclosingClass.asType() as DeclaredType
    val methodResolved = processingEnv.typeUtils.asMemberOf(enclosingClassType,
            method) as ExecutableType
    return ancestorsToList(enclosingClass, processingEnv).any {
        it.enclosedElements.any {
            it is ExecutableElement
                    && !(Modifier.ABSTRACT in it.modifiers)
                    && it.simpleName == method.simpleName
                    && processingEnv.typeUtils.isSubsignature(methodResolved,
                        processingEnv.typeUtils.asMemberOf(enclosingClassType, it) as ExecutableType)
        }
    }
}

// As per
// https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/room/compiler/src/main/kotlin/androidx/room/ext/element_ext.kt
// converts ? in Set< ? extends Foo> to Foo
fun TypeMirror.extendsBound(): TypeMirror? {
    return this.accept(object : SimpleTypeVisitor7<TypeMirror?, Void?>() {
        override fun visitWildcard(type: WildcardType, ignored: Void?): TypeMirror? {
            return type.extendsBound ?: type.superBound
        }
    }, null)
}
/**
 * If the type mirror is in form of ? extends Foo, it returns Foo; otherwise, returns the TypeMirror
 * itself.
 */
fun TypeMirror.extendsBoundOrSelf(): TypeMirror {
    return extendsBound() ?: this
}

fun fieldsOnEntity(entityType: TypeElement) = entityType.enclosedElements.filter {
    it.kind  == ElementKind.FIELD && it.simpleName.toString() != "Companion"
            && !it.modifiers.contains(Modifier.STATIC)
}

/**
 * Determine if the result type is nullable. Any single result entity object or String result can be
 * null (e.g. no such object was found by the query). Primitives cannot be null as they will be 0/false.
 * Lists and arrays (parameterized types) cannot be null: no results will provide an non-null empty
 * list/array.
 */
fun isNullableResultType(typeName: TypeName) = typeName != UNIT
        && !PRIMITIVE.contains(typeName)
        && !(typeName is ParameterizedTypeName)


val PRIMITIVE = listOf(INT, LONG, BOOLEAN, SHORT, BYTE, FLOAT, DOUBLE)


@SupportedAnnotationTypes("androidx.room.Database")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(OPTION_OUTPUT_DIR)
class DbProcessorJdbcKotlin: AbstractDbProcessor() {


    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)

        val dbs = roundEnv.getElementsAnnotatedWith(Database::class.java)
        val outputArg = processingEnv.options[OPTION_OUTPUT_DIR]
        val outputDir = if(outputArg == null || outputArg == "filer") processingEnv.options["kapt.kotlin.generated"] else outputArg
        messager?.printMessage(Diagnostic.Kind.NOTE, "DbProcessorJdbcKotlin: output to ${File(outputDir).absolutePath}")

        for(dbTypeEl in dbs) {
            val dbFileSpec = generateDbImplClass(dbTypeEl as TypeElement)
            dbFileSpec.writeTo(File(outputDir))
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

        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv).forEach {daoSubEl ->
            if(daoSubEl.kind != ElementKind.METHOD)
                return@forEach

            val daoMethod = daoSubEl as ExecutableElement
            if(daoMethod.getAnnotation(Insert::class.java) != null) {
                daoImpl.addFunction(generateInsertFun(daoTypeElement, daoMethod, daoImpl))
            }else if(daoMethod.getAnnotation(Query::class.java) != null) {
                daoImpl.addFunction(generateQueryFun(daoTypeElement, daoMethod, daoImpl))
            }else if(daoMethod.getAnnotation(Update::class.java) != null) {
                daoImpl.addFunction(generateUpdateFun(daoTypeElement, daoMethod, daoImpl))
            }else if(daoMethod.getAnnotation(Delete::class.java) != null) {
                daoImpl.addFunction(generateDeleteFun(daoTypeElement, daoMethod))
            }else {
                messager?.printMessage(Diagnostic.Kind.ERROR,
                        "${makeLogPrefix(daoTypeElement, daoMethod)}: Abstract method on DAO not annotated with Query, Update, Delete, or Insert",
                        daoMethod)
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

            if(subEl.simpleName.startsWith("get")) {
                //must be overriden using a val
                val propName = subEl.simpleName.substring(3, 4).toLowerCase() + subEl.simpleName.substring(4)
                val getterFunSpec = FunSpec.getterBuilder().addStatement("return _${daoTypeEl.simpleName}").build()
                dbImplType.addProperty(PropertySpec.builder(propName,
                        methodEl.returnType.asTypeName(), KModifier.OVERRIDE)
                        .getter(getterFunSpec).build())
            }else {
                dbImplType.addFunction(FunSpec.overriding(methodEl)
                        .addStatement("return _${daoTypeEl.simpleName}").build())
            }

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



    fun generateInsertFun(daoTypeElement: TypeElement, daoMethod: ExecutableElement, daoTypeBuilder: TypeSpec.Builder): FunSpec {
        val insertFun = overrideAndConvertToKotlinTypes(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod) as ExecutableType


        val entityType = entityTypeFromFirstParam(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        if(entityType == processingEnv.typeUtils.nullType) {
            logMessage(Diagnostic.Kind.ERROR, "Insert function first parameter must be an entity object",
                    daoTypeElement, daoMethod)
            return insertFun.build()
        }

        val entityTypeEl = processingEnv.typeUtils.asElement(entityType) as TypeElement

        if(entityTypeEl.getAnnotation(Entity::class.java) == null) {
            logMessage(Diagnostic.Kind.ERROR, "Insert method entity type must be annotated @Entity",
                    daoTypeElement, daoMethod)
            return insertFun.build()
        }

        val resolvedReturnType = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()

        val upsertMode = daoMethod.getAnnotation(Insert::class.java).onConflict == OnConflictStrategy.REPLACE
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

        if(resolvedReturnType != UNIT) {
            insertFun.addCode("val _retVal = ")
        }


        val insertMethodName = makeInsertAdapterMethodName(daoMethodResolved.parameterTypes[0],
                resolvedReturnType, processingEnv)
        insertFun.addCode("$entityInserterPropName.$insertMethodName(${daoMethod.parameters[0].simpleName}, _db.openConnection())")

        if(resolvedReturnType != UNIT) {
            if(isListOrArray(resolvedReturnType)
                    && resolvedReturnType is ParameterizedTypeName
                    && resolvedReturnType.typeArguments[0] == INT) {
                insertFun.addCode(".map { it.toInt() }")
            }else if(resolvedReturnType == INT){
                insertFun.addCode(".toInt()")
            }
        }

        insertFun.addCode("\n")

        insertFun.addCode("_db.handleTableChanged(listOf(%S))\n", entityTypeEl.simpleName)

        if(resolvedReturnType != UNIT) {
            insertFun.addCode("return _retVal")
        }

        if(resolvedReturnType is ParameterizedTypeName
                && resolvedReturnType.rawType == ARRAY) {
            insertFun.addCode(".toTypedArray()")
        }else if(resolvedReturnType == LongArray::class.asClassName()) {
            insertFun.addCode(".toLongArray()")
        }else if(resolvedReturnType == IntArray::class.asClassName()) {
            insertFun.addCode(".toIntArray()")
        }

        insertFun.addCode("\n")

        return insertFun.build()
    }

    fun generateQueryFun(daoTypeElement: TypeElement, daoMethod: ExecutableElement, daoTypeBuilder: TypeSpec.Builder) : FunSpec {
        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod) as ExecutableType

        // The return type of the method - e.g. List<Entity>, LiveData<List<Entity>>, String, etc.
        val returnTypeResolved = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()

        //The type of result with any wrapper (e.g. LiveData) removed e..g List<Entity>, Entity, String, etc.
        val resultType = resolveQueryResultType(returnTypeResolved)

        val funSpec = overrideAndConvertToKotlinTypes(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv,
                forceNullableReturn = isNullableResultType(returnTypeResolved),
                forceNullableParameterTypeArgs = isLiveData(returnTypeResolved)
                        && isNullableResultType((returnTypeResolved as ParameterizedTypeName).typeArguments[0]))

        val querySql = daoMethod.getAnnotation(Query::class.java).value

        if(!querySql.trim().startsWith("UPDATE", ignoreCase = true)
                && !querySql.trim().startsWith("DELETE", ignoreCase = true)
                && resultType == UNIT) {
            logMessage(Diagnostic.Kind.ERROR, "Query method running SELECT must have a return type")
            return funSpec.build()
        }

        val paramTypesResolved = daoMethodResolved.parameterTypes


        //Perhaps this could be replaced with a bit of mapIndexed + filters
        val queryVarsMap = mutableMapOf<String, TypeName>()
        for(i in 0 until daoMethod.parameters.size) {
            if (!isContinuationParam(paramTypesResolved[i].asTypeName())) {
                queryVarsMap[daoMethod.parameters[i].simpleName.toString()] = paramTypesResolved[i].asTypeName()
            }
        }


        if(isDataSourceFactory(returnTypeResolved)) {
            funSpec.addCode("val _result = %T<%T, %T>()\n",
                    DoorDataSourceJdbc.Factory::class, INT,
                    (returnTypeResolved as ParameterizedTypeName).typeArguments[1])
        }else if(isLiveData(returnTypeResolved)) {
            val tablesToWatch = mutableListOf<String>()
            try {
                val select = CCJSqlParserUtil.parse(querySql) as Select
                val tablesNamesFinder = TablesNamesFinder()
                tablesToWatch.addAll(tablesNamesFinder.getTableList(select))
            }catch(e: Exception) {
                messager?.printMessage(Diagnostic.Kind.WARNING,
                        "Could not parse SQL to determine livedata tables to watch")
            }

            val liveDataCodeBlock = CodeBlock.builder()
                    .beginControlFlow("val _result = %T<%T>(_db, listOf(%L)) ",
                            DoorLiveDataJdbcImpl::class.asClassName(),
                            resultType.copy(nullable = isNullableResultType(resultType)),
                            tablesToWatch.map {"\"$it\""}.joinToString())
                    .add(generateQueryCodeBlock(returnTypeResolved, queryVarsMap, querySql,
                            daoTypeElement, daoMethod, resultVarName = "_liveResult"))
                    .add("_liveResult")

            if(resultType is ParameterizedTypeName && resultType.rawType == List::class.asClassName())
                liveDataCodeBlock.add(".toList()")

            liveDataCodeBlock.add("\n")
                    .endControlFlow()

            funSpec.addCode(liveDataCodeBlock.build())
        }else {
            funSpec.addCode(generateQueryCodeBlock(returnTypeResolved, queryVarsMap, querySql,
                    daoTypeElement, daoMethod))
        }

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

        //The parameter type - could be singular (e.g. Entity), could be list/array (e.g. List<Entity>)
        val paramType = daoMethodResolved.parameterTypes[0].asTypeName().javaToKotlinType()

        val entityType = entityTypeFromFirstParam(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val entityTypeEl = processingEnv.typeUtils.asElement(entityType) as TypeElement

        val resolvedReturnType = resolveReturnTypeIfSuspended(daoMethodResolved)

        val codeBlock = CodeBlock.builder()

        val pkEl = entityTypeEl.enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null }
        val nonPkFields = fieldsOnEntity(entityTypeEl).filter { it.kind == ElementKind.FIELD && it.getAnnotation(PrimaryKey::class.java) == null }
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
        if(isListOrArray(paramType)) {
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
            codeBlock.add("_result += ")

        codeBlock.add("_stmt.executeUpdate()\n")

        if(isListOrArray(paramType)) {
            codeBlock.endControlFlow()
                .add("_con.commit()\n")
        }

        codeBlock.nextControlFlow("catch(_e: %T)", SQLException::class)
                .add("_e.printStackTrace()\n")
                .add("throw %T(_e)\n", RuntimeException::class)
                .nextControlFlow("finally")
                .add("_stmt?.close()\n")
                .add("_con?.close()\n")
                .endControlFlow()
                .add("_db.handleTableChanged(listOf(%S))\n", entityTypeEl.simpleName)

        if(resolvedReturnType != UNIT)
            codeBlock.add("return _result\n")

        updateFun.addCode(codeBlock.build())
        return updateFun.build()
    }


    fun generateDeleteFun(daoTypeElement: TypeElement, daoMethod: ExecutableElement): FunSpec {
        val deleteFun = overrideAndConvertToKotlinTypes(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod) as ExecutableType

        //The parameter type - could be singular (e.g. Entity), could be list/array (e.g. List<Entity>)
        val paramType = daoMethodResolved.parameterTypes[0].asTypeName().javaToKotlinType()

        val entityType = entityTypeFromFirstParam(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val entityTypeEl = processingEnv.typeUtils.asElement(entityType) as TypeElement

        val resolvedReturnType = resolveReturnTypeIfSuspended(daoMethodResolved)

        val codeBlock = CodeBlock.builder()

        val pkEl = entityTypeEl.enclosedElements.first { it.getAnnotation(PrimaryKey::class.java) != null }

        val stmtSql = "DELETE FROM ${entityTypeEl.simpleName} WHERE ${pkEl.simpleName} = ?"

        codeBlock.add("var _con = null as %T?\n", Connection::class)
                .add("var _stmt = null as %T?\n", PreparedStatement::class)
                .add("var _numChanges = 0\n")
                .beginControlFlow("try")
                .add("_con = _db.openConnection()\n")
                .add("_stmt = _con.prepareStatement(%S)\n", stmtSql)



        var entityVarName = daoMethod.parameters[0].simpleName.toString()
        if(isListOrArray(paramType)) {
            codeBlock.add("_con.autoCommit = false\n")
                    .beginControlFlow("for(_entity in ${daoMethod.parameters[0].simpleName})")
            entityVarName = "_entity"
        }

        codeBlock.add("_stmt.set${getPreparedStatementSetterGetterTypeName(pkEl.asType().asTypeName())}(1, $entityVarName.${pkEl.simpleName})\n")
        codeBlock.add("_numChanges += _stmt.executeUpdate()\n")

        if(isListOrArray(paramType)) {
            codeBlock.endControlFlow()
                .add("_con.commit()\n")
                .add("_con.autoCommit = true\n")
        }

        codeBlock.beginControlFlow("if(_numChanges > 0)")
                .add("_db.handleTableChanged(listOf(%S))\n", entityTypeEl.simpleName)
                .endControlFlow()

        codeBlock.nextControlFlow("catch(_e: %T)", SQLException::class)
                .add("_e.printStackTrace()\n")
                .add("throw %T(_e)\n", RuntimeException::class)
                .nextControlFlow("finally")
                .add("_con?.close()\n")
                .add("_stmt?.close()\n")
                .endControlFlow()


        if(resolvedReturnType != UNIT)
            codeBlock.add("return _numChanges")

        return deleteFun.addCode(codeBlock.build()).build()
    }

    fun makeLogPrefix(enclosing: TypeElement, method: ExecutableElement) = "DoorDb: ${enclosing.qualifiedName}. ${method.simpleName} "

    companion object {

        const val OPTION_OUTPUT_DIR = "door_jdbc_kt_out"

        const val SUFFIX_JDBC_KT = "JdbcKt"

    }
}