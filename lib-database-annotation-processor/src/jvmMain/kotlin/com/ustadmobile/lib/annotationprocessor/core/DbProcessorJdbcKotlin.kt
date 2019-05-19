package com.ustadmobile.lib.annotationprocessor.core

import android.arch.persistence.room.ColumnInfo
import androidx.room.*
import com.squareup.kotlinpoet.*
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorJdbcKotlin.Companion.OPTION_OUTPUT_DIR
import java.io.File
import java.sql.Connection
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.*
import javax.lang.model.type.*
import com.ustadmobile.door.DoorDbType
import com.ustadmobile.door.DoorDatabase
import com.ustadmobile.door.EntityInsertionAdapter
import java.sql.Statement
import javax.sql.DataSource
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.sql.PreparedStatement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import kotlin.reflect.jvm.internal.impl.name.FqName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap




fun isList(type: TypeMirror, processingEnv: ProcessingEnvironment): Boolean =
        type.kind == TypeKind.DECLARED && (processingEnv.typeUtils.asElement(type) as TypeElement).qualifiedName.toString() == "java.util.List"


fun entityTypeFromFirstParam(method: ExecutableElement, enclosing: DeclaredType, processingEnv: ProcessingEnvironment) : TypeMirror {
    val methodResolved = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType
    val firstParamType = methodResolved.parameterTypes[0]
    if(isList(firstParamType, processingEnv)) {
        val listDt = firstParamType as DeclaredType
        return listDt.typeArguments[0]
    }else if(firstParamType.kind == TypeKind.ARRAY) {
        return (firstParamType as ArrayType).componentType
    }else {
        return firstParamType
    }
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


fun overrideAndConvertToKotlin(method: ExecutableElement, enclosing: DeclaredType, processingEnv: ProcessingEnvironment): FunSpec.Builder {

    val funSpec = FunSpec.builder(method.simpleName.toString())
            .addModifiers(KModifier.OVERRIDE)
    val resolvedExecutableType = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType
    for(i in 0 until method.parameters.size) {
        funSpec.addParameter(method.parameters[i].simpleName.toString(),
                resolvedExecutableType.parameterTypes[i].asTypeName().javaToKotlinType())
    }

    funSpec.returns(resolvedExecutableType.returnType.asTypeName().javaToKotlinType())
    return funSpec
}

fun makeInsertAdapterMethodName(paramType: TypeMirror, returnType: TypeMirror, processingEnv: ProcessingEnvironment): String {
    var methodName = "insert"
    if(isList(paramType, processingEnv)) {
        methodName += "List"
        if(returnType.kind != TypeKind.VOID)
            methodName += "AndReturnIds"
    }else {
        if(returnType.kind != TypeKind.VOID) {
            methodName += "AndReturnId"
        }
    }

    return methodName
}

private fun getPreparedStatementSetterGetterTypeName(variableType: TypeMirror, processingEnv: ProcessingEnvironment): String? {
    if (variableType.kind == TypeKind.BYTE) {
        return "Byte"
    } else if (variableType.kind == TypeKind.INT) {
        return "Int"
    } else if (variableType.kind == TypeKind.LONG) {
        return "Long"
    } else if (variableType.kind == TypeKind.FLOAT) {
        return "Float"
    } else if (variableType.kind == TypeKind.DOUBLE) {
        return "Double"
    } else if (variableType.kind == TypeKind.BOOLEAN) {
        return "Boolean"
    } else if (variableType.kind == TypeKind.DECLARED) {
        val className = (processingEnv.getTypeUtils().asElement(variableType) as TypeElement)
                .qualifiedName.toString()
        when (className) {
            "java.sql.Array" -> return "Array"
            "java.lang.String" -> return "String"
            "java.lang.Integer" -> return "Int"
            "java.lang.Long" -> return "Long"
            "java.lang.Float" -> return "Float"
            "java.lang.Double" -> return "Double"
            "java.lang.Boolean" -> return "Boolean"
        }
    }

    return null
}


@SupportedAnnotationTypes("androidx.room.Database")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(OPTION_OUTPUT_DIR)
class DbProcessorJdbcKotlin: AbstractProcessor() {

    private var messager: Messager? = null

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        messager = p0?.messager
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val dbs = roundEnv!!.getElementsAnnotatedWith(Database::class.java)
        val outputArg = processingEnv.options[OPTION_OUTPUT_DIR]
        val outputDir = if(outputArg == "filer") processingEnv.options["kapt.kotlin.generated"] else outputArg

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

        for(daoSubEl in daoTypeElement.enclosedElements) {
            if(daoSubEl.kind != ElementKind.METHOD)
                continue

            val daoMethod = daoSubEl as ExecutableElement
            if(daoMethod.getAnnotation(Insert::class.java) != null) {
                daoImpl.addFunction(generateInsertFun(daoTypeElement, daoMethod, daoImpl))
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
                .addCode("var _stmt = null as %T\n", Statement::class)
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
        val insertFun = overrideAndConvertToKotlin(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                daoMethod) as ExecutableType


        val entityType = entityTypeFromFirstParam(daoMethod, daoTypeElement.asType() as DeclaredType,
                processingEnv)

        val entityTypeEl = processingEnv.typeUtils.asElement(entityType) as TypeElement

        val upsertMode = daoMethod.getAnnotation(Insert::class.java).onConflict == OnConflictStrategy.REPLACE
        val entityInserterPropName = "_insertAdapter${entityTypeEl.simpleName}_upsert$upsertMode"
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
                    bindCodeBlock.add("stmt.set${getPreparedStatementSetterGetterTypeName(subEl.asType(),
                            processingEnv)}(_fieldIndex++, entity.${subEl.simpleName})\n")
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

        val insertMethodName = makeInsertAdapterMethodName(daoMethodResolved.parameterTypes[0],
                daoMethodResolved.returnType, processingEnv)
        insertFun.addCode("$entityInserterPropName.$insertMethodName(${daoMethod.parameters[0].simpleName}, _db.openConnection())\n")
        return insertFun.build()
    }




    companion object {

        const val OPTION_OUTPUT_DIR = "door_jdbc_kt_out"

        const val SUFFIX_JDBC_KT = "JdbcKt"

    }
}