package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Database
import androidx.room.PrimaryKey
import androidx.room.Query
import com.google.gson.Gson
import com.squareup.kotlinpoet.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import com.ustadmobile.door.DoorDatabase
import com.google.gson.reflect.TypeToken
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.annotation.EntityWithAttachment
import com.ustadmobile.door.annotation.LocalChangeSeqNum
import com.ustadmobile.door.annotation.MasterChangeSeqNum
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_KTOR_OUTPUT
import java.util.*
import javax.lang.model.element.ElementKind

/**
 * Generates a codeblock that will get a parameter from a request and add it to the codeblock
 * with the correct type. This can be with or without a variable declaration.
 *
 * e.g. request.queryParameters['uid']?.toLong() :? 0
 *
 */
fun generateGetParamFromRequestCodeBlock(typeName: TypeName, paramName: String,
                                         declareVariableName: String? = null,
                                         declareVariableType: String = "val",
                                         gsonVarName: String = "_gson",
                                         multipartHelperVarName: String? = null): CodeBlock {
    val codeBlock = CodeBlock.builder()
    if(declareVariableName != null) {
        codeBlock.add("%L %L =", declareVariableType, declareVariableName)
    }

    val precedingCodeblock = CodeBlock.builder()

    if(isQueryParam(typeName)) {
        if(typeName in QUERY_SINGULAR_TYPES) {
            codeBlock.add("%M.request.queryParameters[%S]", DbProcessorKtorServer.CALL_MEMBER, paramName)
            if(typeName == String::class.asTypeName()) {
                codeBlock.add(" ?: \"\"")
            }else {
                codeBlock.add("?.to${(typeName as ClassName).simpleName}() ?: ${defaultVal(typeName)}")
            }
        }else {
            codeBlock.add("%M.request.queryParameters.getAll(%S)", DbProcessorKtorServer.CALL_MEMBER,
                    paramName)
            val parameterizedTypeName = typeName as ParameterizedTypeName
            if(parameterizedTypeName.typeArguments[0] != String::class.asClassName()) {
                codeBlock.add("?.map { it.to${(parameterizedTypeName.typeArguments[0] as ClassName).simpleName}() }")
            }
            codeBlock.add(" ?: listOf()\n")
        }
    }else {
        val getJsonStrCodeBlock = if(multipartHelperVarName != null) {
            CodeBlock.of("$multipartHelperVarName.receiveJsonStr()")
        }else {
            CodeBlock.of("%M.%M<String>()", DbProcessorKtorServer.CALL_MEMBER,
                    MemberName("io.ktor.request", "receiveOrNull"))
        }
        codeBlock.add("$gsonVarName.fromJson(")
                .add(getJsonStrCodeBlock)
                .add(", object: %T() {}.type)",
                    TypeToken::class.asClassName().parameterizedBy(removeTypeProjection(typeName)))
    }

    if(declareVariableName != null){
        codeBlock.add("\n")
    }

    return precedingCodeblock.add(codeBlock.build()).build()
}

/**
 * Generate the code required for sending a response back with a KTOR HTTP call.
 *
 * e.g.
 *
 * when varName is not nullable:
 * call.respond(_varName)
 *
 * when varName is nullable:
 * if(_varName != null) {
 *   call.respond(varName)
 * }else {
 *   call.sendResponse(HttpResponse.NO_CONTENT, "")
 * }
 *
 * when return type is Unit:
 * call.sendResponse(HttpResponse.NO_CONTENT, "")
 *
 */
fun generateRespondCall(returnType: TypeName, varName: String): CodeBlock{
    val codeBlock = CodeBlock.builder()
    when{
        returnType == UNIT -> codeBlock.add("%M.%M(%T.NoContent, \"\")\n", DbProcessorKtorServer.CALL_MEMBER,
                DbProcessorKtorServer.RESPOND_MEMBER, HttpStatusCode::class)

        !isNullableResultType(returnType) -> codeBlock.add("%M.%M($varName)\n",
                DbProcessorKtorServer.CALL_MEMBER,
                DbProcessorKtorServer.RESPOND_MEMBER)

        else -> codeBlock.beginControlFlow("if($varName != null)")
                .add("%M.%M($varName!!)\n", DbProcessorKtorServer.CALL_MEMBER, DbProcessorKtorServer.RESPOND_MEMBER)
                .nextControlFlow("else")
                .add("%M.%M(%T.NoContent, \"\")\n", DbProcessorKtorServer.CALL_MEMBER,
                        DbProcessorKtorServer.RESPOND_MEMBER, HttpStatusCode::class)
                .endControlFlow()
    }

    return codeBlock.build()
}

internal fun generateUpdateTrackerReceivedCodeBlock(trackerClassName: ClassName, syncHelperVarName: String = "_syncHelper") =
    CodeBlock.builder()
            .beginControlFlow("%M(%S)", DbProcessorKtorServer.GET_MEMBER, "_update${trackerClassName.simpleName}Received")
            .add("val _clientId = %M.request.%M(%S)?.toInt() ?: 0\n",
                    DbProcessorKtorServer.CALL_MEMBER,
                    MemberName("io.ktor.request","header"),
                    "X-nid")
            .add(generateGetParamFromRequestCodeBlock(INT, "reqId", "_requestId"))
            //TODO: Add the clientId to this query (to prevent other clients interfering)
            .add("$syncHelperVarName._update${trackerClassName.simpleName}Received(true, _requestId)\n")
            .add("%M.%M(%T.NoContent, \"\")\n", DbProcessorKtorServer.CALL_MEMBER, DbProcessorKtorServer.RESPOND_MEMBER,
                    HttpStatusCode::class)
            .endControlFlow()
            .build()

internal fun generateGetAttachmentDataCodeBlock(entityTypeEl: TypeElement, attachmentsDirVarName: String = "_attachmentsDir"): CodeBlock {
    val entityPkField = entityTypeEl.enclosedElements
            .first { it.getAnnotation(PrimaryKey::class.java) != null }
    return CodeBlock.builder()
            .beginControlFlow("%M(%S)", DbProcessorKtorServer.GET_MEMBER,
                    "_get${entityTypeEl.simpleName}AttachmentData")
            .add(generateGetParamFromRequestCodeBlock(entityPkField.asType().asTypeName(),
                    "_pk", "_pk"))
            .add("val _file = %T(\"\$${attachmentsDirVarName}/${entityTypeEl.simpleName}/\$_pk\")\n",
                    File::class)
            .add("%M.%M(_file)\n", DbProcessorKtorServer.CALL_MEMBER, MemberName("io.ktor.response",
                    "respondFile"))
            .endControlFlow()
            .build()
}

/**
 * This annotation processor generates a KTOR Route for each DAO, and a KTOR Route for each DAO
 * with subroutes for each DAO that is part of the database.
 *
 * Each Syncable KTOR DAO will have the following additional interfaces and classes generated:
 *
 * - DaoName_KtorHelper : This is an interface with a method for each syncable select query. These
 *  methods use refactored SQL that avoids sending a client entities it was already sent. The
 *  generated method will have a clientId parameter (to filter by), and offset and limit parameters
 *  (if the DAO itself returns a DataSource.Factory). The return type will always be the plain entity
 *  itself or a list thereof (not LiveData or DataSource.Factory).
 *
 * - DaoName_KtorHelperLocal: Abstract class implementing DaoName_KtorHelper using the local change
 *  sequence number as the basis for filtering entities. A JDBC implementation of this DAO will also
 *  be generated.
 *
 * - DaoName_KtorHelperMaster: Abstract class implementing DaoName_KtorHelper using the master change
 *  sequence number as the basis for filtering entities. A JDBC implementation of this DAO will also
 *  be generated.
 *
 */
class DbProcessorKtorServer: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        daos.forEach {
            val daoSpecs =  generateDaoImplClasses(it as TypeElement)
            daoSpecs.writeAllSpecs(writeImpl = true, outputSpecArgName = OPTION_KTOR_OUTPUT,
                    useFilerAsDefault = false)
        }

        roundEnv.getElementsAnnotatedWith(Database::class.java).forEach {
            writeFileSpecToOutputDirs(generateDbRoute(it as TypeElement),
                    AnnotationProcessorWrapper.OPTION_KTOR_OUTPUT)
        }

        return true
    }

    fun generateDbRoute(dbTypeElement: TypeElement): FileSpec {
        val dbTypeClassName = dbTypeElement.asClassName()
        val dbRouteFileSpec = FileSpec.builder(dbTypeElement.asClassName().packageName,
                "${dbTypeElement.simpleName}_${SUFFIX_KTOR_ROUTE}")
            .addImport("com.ustadmobile.door", "DoorDbType")
            .addImport("io.ktor.response", "header")

        val dbRouteFn = FunSpec.builder("${dbTypeElement.simpleName}_${SUFFIX_KTOR_ROUTE}")
                .receiver(Route::class)
                .addParameter("_db", dbTypeElement.asClassName())
                .addParameter("_gson", Gson::class)
                .addParameter("_attachmentsDir", String::class)
                .addParameter(ParameterSpec.builder("_isMaster", BOOLEAN)
                        .defaultValue("true")
                        .build())

        val codeBlock = CodeBlock.builder()
                .add("val _gson = %T()\n", Gson::class)

        val isSyncableDb = isSyncableDb(dbTypeElement, processingEnv)

        if(isSyncableDb){
            codeBlock.add("val _syncHelperDao = %T(_db)\n",
                    ClassName(dbTypeClassName.packageName,
                            "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_IMPL}"))
        }

        codeBlock.beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                dbTypeClassName.simpleName)

        if(isSyncableDb) {
            val syncDaoBaseName = "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}"
            val helperClasses = listOf(SUFFIX_KTOR_HELPER_MASTER, SUFFIX_KTOR_HELPER_LOCAL)
                    .map {
                        ClassName(dbTypeClassName.packageName,
                                "${syncDaoBaseName}$it${DbProcessorJdbcKotlin.SUFFIX_JDBC_KT}")
                    }
            codeBlock.add("%M(_syncHelperDao, _db, _gson, _attachmentsDir",
                    MemberName(dbTypeClassName.packageName,
                            "${syncDaoBaseName}_$SUFFIX_KTOR_ROUTE"))
                    .add(",if(_isMaster)·{·%T(_db)·}·else·{·%T(_db)·}", helperClasses[0],
                            helperClasses[1])
                    .add(")\n")
        }

        methodsToImplement(dbTypeElement, dbTypeElement.asType() as DeclaredType, processingEnv)
        .filter{ it.kind == ElementKind.METHOD }.map {it as ExecutableElement }.forEach {
            var daoFromDbGetter = ""
            val daoTypeEl = processingEnv.typeUtils.asElement(it.returnType) as TypeElement?
            if(daoTypeEl == null) {
                return@forEach
            }
            val daoTypeClassName = daoTypeEl.asClassName()

            if(it.simpleName.toString().startsWith("get")) {
                daoFromDbGetter += it.simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + it.simpleName.substring(4)
            }else {
                daoFromDbGetter += "${it.simpleName}()"
            }

            codeBlock.add("%M(_db.$daoFromDbGetter, _db, _gson, _attachmentsDir", MemberName(daoTypeClassName.packageName,
                    "${daoTypeEl.simpleName}_$SUFFIX_KTOR_ROUTE"))
            if(syncableEntitiesOnDao(daoTypeClassName, processingEnv).isNotEmpty()) {
                val helperClasses = listOf(SUFFIX_KTOR_HELPER_MASTER, SUFFIX_KTOR_HELPER_LOCAL)
                        .map {
                            ClassName(daoTypeClassName.packageName,
                        "${daoTypeClassName.simpleName}$it${DbProcessorJdbcKotlin.SUFFIX_JDBC_KT}")
                        }
                codeBlock.add(", _syncHelperDao")
                        .add(",if(_isMaster)·{·%T(_db)·}·else·{·%T(_db)·}", helperClasses[0],
                                helperClasses[1])
            }
            codeBlock.add(")\n")
        }
        codeBlock.endControlFlow()

        dbRouteFn.addCode(codeBlock.build())
        dbRouteFileSpec.addFunction(dbRouteFn.build())

        return dbRouteFileSpec.build()


    }



    fun generateDaoImplClasses(daoTypeElement: TypeElement): KtorDaoFileSpecs {
        val specs = KtorDaoSpecs(daoTypeElement.asClassName().packageName,
                daoTypeElement.asClassName().simpleName)

        specs.route.fileSpec.addImport("com.ustadmobile.door", "DoorDbType")
                .addImport("io.ktor.response", "header")


        val daoRouteFn = FunSpec.builder("${daoTypeElement.simpleName}_$SUFFIX_KTOR_ROUTE")
                .receiver(Route::class)
                .addParameter("_dao", daoTypeElement.asType().asTypeName())
                .addParameter("_db", DoorDatabase::class)
                .addParameter("_gson", Gson::class)
                .addParameter("_attachmentsDir", String::class)

        if(syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv).isNotEmpty()) {
            daoRouteFn.addParameter("_syncHelper",
                    ClassName(pkgNameOfElement(daoTypeElement, processingEnv),
                            "${daoTypeElement.simpleName}_SyncHelper"))
                    .addParameter("_ktorHelperDao",
                            ClassName(daoTypeElement.asClassName().packageName,
                                    "${daoTypeElement.simpleName}$SUFFIX_KTOR_HELPER"))
        }

        val codeBlock = CodeBlock.builder()

        codeBlock.beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                "${daoTypeElement.simpleName}")

        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv,
                includeImplementedMethods = true).forEach { daoSubEl ->
            val daoMethodEl = daoSubEl as ExecutableElement
            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType
            val resolvedReturnTypeName = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()
            val resultType = resolveQueryResultType(resolvedReturnTypeName)
            val componentEntityType = resolveEntityFromResultType(resultType)

            val numNonQueryParams = getHttpBodyParams(daoMethodEl, daoMethodResolved).size

            val memberFn = if(numNonQueryParams == 1){
                POST_MEMBER
            }else {
                GET_MEMBER
            }


            codeBlock.beginControlFlow("%M(%S)", memberFn, daoSubEl.simpleName)

            val funSpec = FunSpec.builder(daoMethodEl.simpleName.toString())
                    .returns(resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType())
            daoMethodEl.parameters
                    .filter { !isContinuationParam(it.asType().asTypeName()) }
                    .forEachIndexed { index, paramEl ->
                funSpec.addParameter(paramEl.simpleName.toString(),
                        daoMethodResolved.parameterTypes[index].asTypeName().javaToKotlinType())
            }

            val queryAnnotation = daoSubEl.getAnnotation(Query::class.java)
            if(queryAnnotation != null
                    && componentEntityType is ClassName
                    && findSyncableEntities(componentEntityType, processingEnv).isNotEmpty()) {
                //Generate a codeblock to handle syncable result types
                funSpec.addAnnotation(AnnotationSpec.builder(Query::class.asClassName())
                        .addMember(CodeBlock.of("%S", queryAnnotation.value)).build())
                val funSpecBuilt = funSpec.build()
                codeBlock.add(generateKtorRouteSelectCodeBlock(funSpecBuilt,
                        daoTypeElement))

                val isDataSourceFactory = resolvedReturnTypeName is ParameterizedTypeName
                        && resolvedReturnTypeName.rawType == DataSource.Factory::class.asClassName()

                val helperParamList = funSpecBuilt.parameters.toMutableList()
                if(isDataSourceFactory) {
                    helperParamList += ParameterSpec.builder("_offset", INT).build()
                    helperParamList += ParameterSpec.builder("_limit", INT).build()
                }
                helperParamList += ParameterSpec.builder("clientId", INT).build()

                val querySql = daoSubEl.getAnnotation(Query::class.java).value
                val helperFunSpec = FunSpec.builder(funSpecBuilt.name)
                        .addParameters(helperParamList)
                        .returns(resultType.copy(nullable = isNullableResultType(resultType)))
                        .build()
                specs.addHelperQueryFun(helperFunSpec, querySql, componentEntityType,
                        isDataSourceFactory)
            }else {
                codeBlock.add(generateKtorPassToDaoCodeBlock(funSpec.build()))
            }

            codeBlock.endControlFlow()
        }

        syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv).forEach {
            val syncableEntityinfo = SyncableEntityInfo(it, processingEnv)
            codeBlock.add(generateUpdateTrackerReceivedCodeBlock(syncableEntityinfo.tracker))
        }
        queryResultTypesWithAnnotationOnDao(daoTypeElement.asClassName(), EntityWithAttachment::class.java,
                processingEnv).forEach {
            val entityTypeEl = processingEnv.elementUtils.getTypeElement(it.canonicalName) as TypeElement
            codeBlock.add(generateGetAttachmentDataCodeBlock(entityTypeEl))
        }


        codeBlock.endControlFlow()
        daoRouteFn.addCode(codeBlock.build())
        specs.route.fileSpec.addFunction(daoRouteFn.build())

        return specs.toBuiltFileSpecs()
    }



    companion object {

        const val SUFFIX_KTOR_ROUTE = "KtorRoute"

        const val SUFFIX_KTOR_HELPER = "_KtorHelper"

        const val SUFFIX_KTOR_HELPER_MASTER = "_KtorHelperMaster"

        const val SUFFIX_KTOR_HELPER_LOCAL = "_KtorHelperLocal"

        val GET_MEMBER = MemberName("io.ktor.routing", "get")

        val POST_MEMBER = MemberName("io.ktor.routing", "post")

        val CALL_MEMBER = MemberName("io.ktor.application", "call")

        val RESPOND_MEMBER = MemberName("io.ktor.response", "respond")

        val RESPONSE_HEADER = MemberName("io.ktor.response", "header")
    }
}