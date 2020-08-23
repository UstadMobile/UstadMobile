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
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_KTOR_OUTPUT
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.SERVER_TYPE_KTOR
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.SERVER_TYPE_NANOHTTPD
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.router.RouterNanoHTTPD
import java.util.*
import javax.lang.model.element.ElementKind
import com.ustadmobile.door.DoorConstants
import com.ustadmobile.door.DoorDaoProvider
import com.ustadmobile.door.NanoHttpdCall
import com.ustadmobile.door.annotation.MinSyncVersion
import com.ustadmobile.door.ext.DoorTag
import com.ustadmobile.lib.annotationprocessor.core.AnnotationProcessorWrapper.Companion.OPTION_ANDROID_OUTPUT
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.CODEBLOCK_KTOR_NO_CONTENT_RESPOND
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.CODEBLOCK_NANOHTTPD_NO_CONTENT_RESPONSE
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.DI_INSTANCE_MEMBER
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.DI_INSTANCE_TYPETOKEN_MEMBER
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.DI_ON_MEMBER
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.NANOHTTPD_URIRESOURCE_FUNPARAMS
import com.ustadmobile.lib.annotationprocessor.core.DbProcessorKtorServer.Companion.SUFFIX_KTOR_HELPER
import io.ktor.application.ApplicationCallPipeline
import org.kodein.di.DI
import javax.annotation.processing.ProcessingEnvironment

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
                                         multipartHelperVarName: String? = null,
                                         serverType: Int = SERVER_TYPE_KTOR): CodeBlock {
    val codeBlock = CodeBlock.builder()
    if(declareVariableName != null) {
        codeBlock.add("%L %L =", declareVariableType, declareVariableName)
    }

    val precedingCodeblock = CodeBlock.builder()

    if(isQueryParam(typeName)) {
        if(typeName in QUERY_SINGULAR_TYPES) {
            if(serverType == SERVER_TYPE_KTOR) {
                codeBlock.add("%M.request.queryParameters[%S]", DbProcessorKtorServer.CALL_MEMBER, paramName)
            }else {
                codeBlock.add("_session.parameters.get(%S)?.get(0)", paramName)
            }
            if(typeName == String::class.asTypeName()) {
                codeBlock.add(" ?: \"\"")
            }else {
                codeBlock.add("?.to${(typeName as ClassName).simpleName}() ?: ${defaultVal(typeName)}")
            }
        }else {
            if(serverType == SERVER_TYPE_KTOR) {
                codeBlock.add("%M.request.queryParameters.getAll(%S)", DbProcessorKtorServer.CALL_MEMBER,
                        paramName)
            }else {
                codeBlock.add("_session.parameters[%S]", paramName)
            }

            val parameterizedTypeName = typeName as ParameterizedTypeName
            if(parameterizedTypeName.typeArguments[0] != String::class.asClassName()) {
                codeBlock.add("·?.map·{·it.to${(parameterizedTypeName.typeArguments[0] as ClassName).simpleName}()·}")
            }
            codeBlock.add("·?:·listOf()\n")
        }
    }else {
        val getJsonStrCodeBlock = if(multipartHelperVarName != null) {
            CodeBlock.of("$multipartHelperVarName.receiveJsonStr()")
        }else if(serverType == SERVER_TYPE_KTOR){
            CodeBlock.of("%M.%M<String>()", DbProcessorKtorServer.CALL_MEMBER,
                    MemberName("io.ktor.request", "receiveOrNull"))
        }else {
            CodeBlock.of("mutableMapOf<String,String>().also{_session.parseBody(it)}.get(%S)",
                    "postData")
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

fun CodeBlock.Builder.addNanoHttpdResponse(varName: String, addNonNullOperator: Boolean = false,
                                           applyToResponseCodeBlock: CodeBlock? = null)
        = add("return %T.newFixedLengthResponse(%T.Status.OK, %T.MIME_TYPE_JSON, _gson.toJson($varName${if(addNonNullOperator) "!!" else ""}))\n",
            NanoHTTPD::class, NanoHTTPD.Response::class, DoorConstants::class)
        .takeIf { applyToResponseCodeBlock != null }
            ?.beginControlFlow(".apply ")
            ?.add(applyToResponseCodeBlock!!)
            ?.endControlFlow()

fun CodeBlock.Builder.addKtorResponse(varName: String, addNonNullOperator: Boolean = false)
        = add("%M.%M($varName)\n", DbProcessorKtorServer.CALL_MEMBER,
            DbProcessorKtorServer.RESPOND_MEMBER)

/**
 * This extension function is designed to wrapup a NanoHTTPD UriResponder. It will generate an
 * implementation of the get and post function that will call the real function according to the uri.
 * It will also add override functions implementing the delete, put and other function
 *
 * @param nanoHttpdGetFns A list of function names that are get requests
 * @param nanoHttpdPostFns A list of function names that are post requests
 * @param initParamsList A list of the parameters being used in the UriResponder methods (e.g. the
 * real dao,
 */
fun TypeSpec.Builder.addNanoHttpdUriResponderFuns(nanoHttpdGetFns: List<String>, nanoHttpdPostFns: List<String>,
                                                  initParamsList: List<ParameterSpec>, daoTypeClassName: ClassName,
                                                  isSyncable: Boolean): TypeSpec.Builder {

    listOf(nanoHttpdGetFns to "get", nanoHttpdPostFns to "post").forEach {fnType ->
        val uriResponderFnSpec = FunSpec.builder(fnType.second)
                .returns(NanoHTTPD.Response::class)
                .addModifiers(KModifier.OVERRIDE)
                .addParameters(NANOHTTPD_URIRESOURCE_FUNPARAMS)

        val fnCodeBlock = CodeBlock.builder()
                .add("val _fnName = _session.uri.%M('/')\n", MemberName("kotlin.text", "substringAfterLast"))
                .add("val _di = _uriResource.initParameter(0, %T::class.java)\n", DI::class)
                .add("val _daoProvider = _uriResource.initParameter(1, %T) as %T\n",
                    DoorDaoProvider::class,
                        DoorDaoProvider::class.asClassName().parameterizedBy(
                                DoorDatabase::class.asClassName(), daoTypeClassName))
                .add("val _typeToken = _uriResource.initParameter(2, %T::class) as %T<%T>\n",
                    org.kodein.type.TypeToken::class, org.kodein.type.TypeToken::class, DoorDatabase::class)
                .add("val _call = %T(_uriResource, _urlParams, _session)\n", NanoHttpdCall::class)
                .add("val _db: %T by _di.%M(_call).%M(_typeToken, tag = %T.TAG_DB)\n", DoorDatabase::class,
                        DI_ON_MEMBER, DI_INSTANCE_TYPETOKEN_MEMBER, DoorTag::class)
                .add("val _dao = _daoProvider.getDao(_db)\n")
                .apply { takeIf { isSyncable }
                        ?.add("val _syncHelper = (_uriResource.initParameter(3, %T::class) as %T).getDao(_db)\n",
                                DoorDaoProvider::class, DoorDaoProvider::class.asClassName().parameterizedBy(
                                DoorDatabase::class.asClassName(), ClassName(daoTypeClassName.packageName,
                                "${daoTypeClassName.simpleName}_SyncHelper")))
                        ?.add("val _ktorHelperDao = (uriResource.initParameter(4, %T::class as %T).getDao(_db)\n",
                                DoorDaoProvider::class, DoorDaoProvider::class.asClassName().parameterizedBy(
                                DoorDatabase::class.asClassName(), ClassName(daoTypeClassName.packageName,
                                "${daoTypeClassName.simpleName}$SUFFIX_KTOR_HELPER")))
                }

//        initParamsList.forEachIndexed { index, parameterSpec ->
//            fnCodeBlock.add("val ${parameterSpec.name} = _uriResource.initParameter($index, %T::class.java)\n",
//                    parameterSpec.type)
//        }


        fnCodeBlock.add("val _clientDbVersion = _session.headers.get(%T.HEADER_DBVERSION)?.toInt() ?: 0\n",
                    DoorConstants::class)
        //TODO: re-enable this
//                .beginControlFlow("if(_clientDbVersion < _minSyncVersion)")
//                .add("return %T.newFixedLengthResponse(%T.Response.Status.BAD_REQUEST, %T.MIME_TYPE_PLAIN, %S)",
//                        NanoHTTPD::class, NanoHTTPD::class, DoorConstants::class,
//                        "Client db version does not meet minimum \$_minSyncVersion")
//                .endControlFlow()

        val notFoundCodeBlock = CodeBlock.of("%T.newFixedLengthResponse(%T.Status.NOT_FOUND, " +
                "%T.MIME_TYPE_PLAIN, %S)", NanoHTTPD::class, NanoHTTPD.Response::class,
                DoorConstants::class, "")
        if(fnType.first.isEmpty()) {
            fnCodeBlock.add("return ").add(notFoundCodeBlock).add("\n")
        }else {
            fnCodeBlock.beginControlFlow("return when(_fnName)")

            fnType.first.forEach {fnName ->
                fnCodeBlock.add("%S -> ${fnName}(_uriResource, _urlParams, _session, ${initParamsList.map{it.name}.joinToString()})\n",
                        fnName)
            }
            fnCodeBlock.add("else -> ").add(notFoundCodeBlock).add("\n")
            fnCodeBlock.endControlFlow()
        }

        this.addFunction(uriResponderFnSpec
                .addCode(fnCodeBlock.build())
                .build())
    }

    val returnMethodNotSupportedException = CodeBlock.of("return %T.newFixedLengthResponse(" +
            "%T.Status.METHOD_NOT_ALLOWED, %T.MIME_TYPE_PLAIN, %S)", NanoHTTPD::class,
            NanoHTTPD.Response::class, DoorConstants::class, "")
    listOf("put", "delete").forEach {fnName ->
        this.addFunction(FunSpec.builder(fnName)
                .addParameters(NANOHTTPD_URIRESOURCE_FUNPARAMS)
                .addModifiers(KModifier.OVERRIDE)
                .returns(NanoHTTPD.Response::class)
                .addCode(returnMethodNotSupportedException)
                .build())
    }
    this.addFunction(FunSpec.builder("other")
            .addParameter("methodName", String::class)
            .addParameters(NANOHTTPD_URIRESOURCE_FUNPARAMS)
            .addModifiers(KModifier.OVERRIDE)
            .returns(NanoHTTPD.Response::class)
            .addCode(returnMethodNotSupportedException)
            .build())

    return this
}

fun FunSpec.Builder.addParametersForHttpDb(dbTypeElement: TypeElement, isPrimaryDefaultVal: Boolean): FunSpec.Builder {
    addParameter(ParameterSpec.builder("_isPrimary", BOOLEAN)
                    .defaultValue(isPrimaryDefaultVal.toString())
                    .build())
    return this
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
 * @param ktorBeforeRespondCodeBlock This codeblock will be added on the KTOR response type before
 * the call.respond line (e.g. to set headers)
 * @param nanoHttpdApplyCodeBlock This codeblock will be added to an also block after returning the
 * nanohttpd response (e.g. to set headers)
 *
 */
fun generateRespondCall(returnType: TypeName, varName: String, serverType: Int = SERVER_TYPE_KTOR,
                        ktorBeforeRespondCodeBlock: CodeBlock? = null,
                        nanoHttpdApplyCodeBlock: CodeBlock? = null): CodeBlock{
    val codeBlock = CodeBlock.builder()
    if(ktorBeforeRespondCodeBlock != null && serverType == SERVER_TYPE_KTOR)
        codeBlock.add(ktorBeforeRespondCodeBlock)

    when{
        returnType == UNIT && serverType == SERVER_TYPE_KTOR->
            codeBlock.add(CODEBLOCK_KTOR_NO_CONTENT_RESPOND)

        returnType == UNIT && serverType == SERVER_TYPE_NANOHTTPD ->
            codeBlock.add(CODEBLOCK_NANOHTTPD_NO_CONTENT_RESPONSE)

        !isNullableResultType(returnType) && serverType == SERVER_TYPE_KTOR->
            codeBlock.addKtorResponse(varName)

        !isNullableResultType(returnType) && serverType == SERVER_TYPE_NANOHTTPD ->
            codeBlock.addNanoHttpdResponse(varName, applyToResponseCodeBlock = nanoHttpdApplyCodeBlock)


        else -> codeBlock.beginControlFlow("if($varName != null)")
                .apply {
                    takeIf { serverType == SERVER_TYPE_KTOR }?.addKtorResponse(varName,
                            addNonNullOperator = true)
                    takeIf { serverType == SERVER_TYPE_NANOHTTPD }?.addNanoHttpdResponse(varName,
                            addNonNullOperator = true, applyToResponseCodeBlock = nanoHttpdApplyCodeBlock)
                }
                .nextControlFlow("else")
                .apply {
                    takeIf { serverType == SERVER_TYPE_KTOR }
                            ?.add(CODEBLOCK_KTOR_NO_CONTENT_RESPOND)
                    takeIf { serverType == SERVER_TYPE_NANOHTTPD }
                            ?.add(CODEBLOCK_NANOHTTPD_NO_CONTENT_RESPONSE)
                }
                .endControlFlow()
    }

    return codeBlock.build()
}


fun CodeBlock.Builder.addRequestDi(diVarName: String = "_di", dbVarName: String = "_db",
    serverType: Int = SERVER_TYPE_KTOR) : CodeBlock.Builder {
    return add("val ${diVarName} = %M()\n", MemberName("org.kodein.di.ktor", "di"))
            .add("val ${dbVarName}: %T by _di.%M(call).%M(tag = %T.TAG_DB)\n",
                    TypeVariableName.invoke("T"), DbProcessorKtorServer.DI_ON_MEMBER, DbProcessorKtorServer.DI_INSTANCE_MEMBER,
                    DoorTag::class)
}

internal fun generateUpdateTrackerReceivedCodeBlock(trackerClassName: ClassName, syncHelperVarName: String = "_syncHelper",
                                                    syncHelperFnName: String = "_syncHelperFn",
                                                    dbVarName: String = "_db",
                                                    serverType: Int = SERVER_TYPE_KTOR) =
    CodeBlock.builder()
            .addGetClientIdHeader("_clientId", serverType)
            .add(generateGetParamFromRequestCodeBlock(INT, "reqId", "_requestId",
                    serverType = serverType))
            .addRequestDi(serverType = serverType, dbVarName = dbVarName)
            .apply { takeIf { serverType == SERVER_TYPE_KTOR }?.add("val $syncHelperVarName = $syncHelperFnName($dbVarName)\n") }
            //TODO: Add the clientId to this query (to prevent other clients interfering)
            .add("$syncHelperVarName._update${trackerClassName.simpleName}Received(true, _requestId)\n")
            .apply { takeIf { serverType == SERVER_TYPE_KTOR }?.add(CODEBLOCK_KTOR_NO_CONTENT_RESPOND) }
            .apply { takeIf { serverType == SERVER_TYPE_NANOHTTPD }?.add(CODEBLOCK_NANOHTTPD_NO_CONTENT_RESPONSE) }
            .build()

internal fun generateUpdateTrackerReceivedKtorRoute(trackerClassName: ClassName,
                                                    syncHelperFnName: String = "_syncHelperFn",
                                                    dbVarName: String = "_db",
                                                    syncHelperVarName: String = "_syncHelper")=
    CodeBlock.builder().beginControlFlow("%M(%S)",
                DbProcessorKtorServer.GET_MEMBER, "_update${trackerClassName.simpleName}Received")
            .add(generateUpdateTrackerReceivedCodeBlock(trackerClassName, syncHelperVarName,
                    serverType = SERVER_TYPE_KTOR, syncHelperFnName = syncHelperFnName))
            .endControlFlow()
            .build()

/**
 * Generates a function for NanoHTTPD to implement the update syncable tracker received. This function
 * will have the NanoHTTPD standard parameters (uriresource, urlparams, and session) but will not
 * have parameters for parameters that are retrieved from the uriResource itself (e.g. the db,
 * dao, synchelper etc).
 */
internal fun generateUpdateTrackerReceivedNanoHttpdFun(trackerClassName: ClassName, syncHelperVarName: String = "_syncHelper") =
    FunSpec.builder("_update${trackerClassName.simpleName}Received")
            .addCode(generateUpdateTrackerReceivedCodeBlock(trackerClassName, syncHelperVarName,
                        serverType = SERVER_TYPE_NANOHTTPD))
            .addParameters(NANOHTTPD_URIRESOURCE_FUNPARAMS)
            .returns(NanoHTTPD.Response::class)


internal fun generateGetAttachmentDataCodeBlock(entityTypeEl: TypeElement, attachmentsDirVarName: String = "_attachmentsDir"): CodeBlock {
    val entityPkField = entityTypeEl.enclosedElements
            .first { it.getAnnotation(PrimaryKey::class.java) != null }
    return CodeBlock.builder()
            .beginControlFlow("%M(%S)", DbProcessorKtorServer.GET_MEMBER,
                    "_get${entityTypeEl.simpleName}AttachmentData")
            .addRequestDi()
            .add("val $attachmentsDirVarName : String by _di.%M(call).%M(tag = %T.TAG_ATTACHMENT_DIR)\n",
                DI_ON_MEMBER, DI_INSTANCE_MEMBER, DoorTag::class)
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
 * Gives a list of all DAOS on the given database. Returns a list of Pairs where the first element
 * is the TypeElement of the databse and the second is a string representing the correct way to
 * access this DAO (e.g. .name if it's a property, or .name() if it's a function)
 */
internal fun daosOnDatabase(dbTypeElement: TypeElement, processingEnv: ProcessingEnvironment): List<Pair<TypeElement, String>> {
    return methodsToImplement(dbTypeElement, dbTypeElement.asType() as DeclaredType, processingEnv)
            .filter{ it.kind == ElementKind.METHOD }
            .map {it as ExecutableElement }
            .filter{ processingEnv.typeUtils.asElement(it.returnType) != null}
            .map {
                var daoFromDbGetter = ""
                val daoTypeEl = processingEnv.typeUtils.asElement(it.returnType) as TypeElement

                if (it.simpleName.toString().startsWith("get")) {
                    daoFromDbGetter += it.simpleName.substring(3, 4).toLowerCase(Locale.ROOT) + it.simpleName.substring(4)
                } else {
                    daoFromDbGetter += "${it.simpleName}()"
                }

                Pair(daoTypeEl, daoFromDbGetter)
            }
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
            daoSpecs.writeAllSpecs(writeImpl = true, writeKtor = true,
                    outputSpecArgName = OPTION_KTOR_OUTPUT, useFilerAsDefault = false)
            daoSpecs.writeAllSpecs(writeNanoHttpd = true,
                    outputSpecArgName = OPTION_ANDROID_OUTPUT, useFilerAsDefault = false)
        }

        roundEnv.getElementsAnnotatedWith(Database::class.java).forEach {
            writeFileSpecToOutputDirs(generateDbRoute(it as TypeElement),
                    AnnotationProcessorWrapper.OPTION_KTOR_OUTPUT)
            writeFileSpecToOutputDirs(generateNanoHttpdDbMapper(it as TypeElement,
                    getHelpersFromDb = true), OPTION_ANDROID_OUTPUT, false)
        }

        return true
    }

    fun generateNanoHttpdDbMapper(dbTypeElement: TypeElement, getHelpersFromDb: Boolean = true): FileSpec {
        val dbTypeClassName = dbTypeElement.asClassName()
        val dbMapperFileSpec = FileSpec.builder(dbTypeClassName.packageName,
                "${dbTypeClassName.simpleName}$SUFFIX_NANOHTTPD_ADDURIMAPPING")
                .addImport("com.ustadmobile.door", "DoorDbType")

        val dbMapperFn = FunSpec.builder("${dbTypeClassName.simpleName}$SUFFIX_NANOHTTPD_ADDURIMAPPING")
                .addParametersForHttpDb(dbTypeElement, false)
                .addParameter("_mappingPrefix", String::class)
                .addParameter("_di", DI::class)
                .receiver(RouterNanoHTTPD::class)
        val codeBlock = CodeBlock.builder()
                .add("val _typeToken : %T = %M()\n",
                    org.kodein.type.TypeToken::class.asClassName().parameterizedBy(dbTypeClassName),
                    MemberName("org.kodein.type", "erased"))

        val isSyncableDb = isSyncableDb(dbTypeElement, processingEnv)

        val syncDaoClassName = ClassName(dbTypeClassName.packageName,
                "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}")



        if(isSyncableDb) {
            val syncDaoProviderType = DoorDaoProvider::class.asTypeName().parameterizedBy(
                    dbTypeClassName, syncDaoClassName)
            codeBlock.takeIf { !getHelpersFromDb }?.add("val _syncDaoProvider = %T(){ %T(it) }\n",
                    syncDaoProviderType, ClassName(dbTypeClassName.packageName,
                            "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_IMPL}"))
            codeBlock.takeIf { getHelpersFromDb }?.add("val _syncDaoProvider = %T() { it._syncDao() }\n",
                syncDaoProviderType)
        }

        daosOnDatabase(dbTypeElement, processingEnv).forEach {
            val daoTypeEl = it.first
            val daoFromDbGetter = it.second
            val daoTypeClassName = daoTypeEl.asClassName()
            val responderUriClassName = ClassName(daoTypeClassName.packageName,
                    "${daoTypeClassName.simpleName}$SUFFIX_NANOHTTPD_URIRESPONDER")

            val minClientDbVersion = dbTypeElement.getAnnotation(MinSyncVersion::class.java)?.value ?: -1
            codeBlock.add("addRoute(\"\$_mappingPrefix/${daoTypeClassName.simpleName}/(.)+\",\n " +
                    "%T::class.java, _di,\n %T(){ it.$daoFromDbGetter }, _typeToken",
                    responderUriClassName, DoorDaoProvider::class.asTypeName().parameterizedBy(
                        dbTypeClassName, daoTypeClassName))
            if(syncableEntitiesOnDao(daoTypeEl.asClassName(), processingEnv).isNotEmpty()) {
                val ktorHelperBaseName = "${daoTypeClassName.simpleName}$SUFFIX_KTOR_HELPER"
                val ktorHelperClassName = ClassName(daoTypeClassName.packageName, ktorHelperBaseName)
                codeBlock.takeIf { getHelpersFromDb }
                        ?.add("\n,_syncDaoProvider,\n%T() { if(isPrimary){it._${ktorHelperBaseName}Master()} else {it._${ktorHelperBaseName}Local()} }",
                            DoorDaoProvider::class.asTypeName().parameterizedBy(dbTypeClassName, ktorHelperClassName))
            }

            codeBlock.add(")\n")
        }

        dbMapperFn.addCode(codeBlock.build())
        dbMapperFileSpec.addFunction(dbMapperFn.build())

        return dbMapperFileSpec.build()
    }

    fun generateDbRoute(dbTypeElement: TypeElement): FileSpec {
        val dbTypeClassName = dbTypeElement.asClassName()
        val dbRouteFileSpec = FileSpec.builder(dbTypeElement.asClassName().packageName,
                "${dbTypeElement.simpleName}${SUFFIX_KTOR_ROUTE}")
            .addImport("com.ustadmobile.door", "DoorDbType")
            .addImport("io.ktor.response", "header")

        val dbRouteFn = FunSpec.builder("${dbTypeElement.simpleName}${SUFFIX_KTOR_ROUTE}")
                .receiver(Route::class)
                .addParametersForHttpDb(dbTypeElement, true)

        val codeBlock = CodeBlock.builder()

        val isSyncableDb = isSyncableDb(dbTypeElement, processingEnv)

        val syncDaoImplClassName = ClassName(dbTypeClassName.packageName,
                "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_IMPL}")

        codeBlock.beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                dbTypeClassName.simpleName)

        val minSyncVersionAnnotation = dbTypeElement.getAnnotation(MinSyncVersion::class.java)
        if(minSyncVersionAnnotation != null) {
            codeBlock.beginControlFlow("this.intercept(%T.Features)", ApplicationCallPipeline::class)
                    .add("val _clientVersion = this.context.request.headers[%T.HEADER_DBVERSION]?.toInt() ?: 0\n",
                            DoorConstants::class)
                    .beginControlFlow("if(_clientVersion < ${minSyncVersionAnnotation.value})")
                    .add("context.request.call.%M(%T.BadRequest, %S)\n", RESPOND_MEMBER,
                            HttpStatusCode::class,
                            "Door DB Version does not meet minimum required: ${minSyncVersionAnnotation.value}")
                    .add("return@intercept finish()\n")
                    .endControlFlow()
                    .endControlFlow()
        }


        if(isSyncableDb) {
            val syncDaoBaseName = "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}"
            val helperClasses = listOf(SUFFIX_KTOR_HELPER_MASTER, SUFFIX_KTOR_HELPER_LOCAL)
                    .map {
                        ClassName(dbTypeClassName.packageName,
                                "${syncDaoBaseName}$it${DbProcessorJdbcKotlin.SUFFIX_JDBC_KT}")
                    }
            codeBlock.add("%M<%T>({ %T(it) }, ",
                    MemberName(dbTypeClassName.packageName, "${syncDaoBaseName}$SUFFIX_KTOR_ROUTE"),
                    dbTypeElement, syncDaoImplClassName)
                    .add("{ if(_isPrimary)·{·%T(it)·}·else·{·%T(it)·} }", helperClasses[0],
                            helperClasses[1])
                    .add(")\n")
        }


        daosOnDatabase(dbTypeElement, processingEnv).forEach {
            val daoTypeEl = it.first
            val daoFromDbGetter = it.second
            val daoTypeClassName = daoTypeEl.asClassName()

            codeBlock.add("%M<%T>({it.$daoFromDbGetter }",
                    MemberName(daoTypeClassName.packageName, "${daoTypeEl.simpleName}$SUFFIX_KTOR_ROUTE"),
                    dbTypeElement)
            if(syncableEntitiesOnDao(daoTypeClassName, processingEnv).isNotEmpty()) {
                val helperClasses = listOf(SUFFIX_KTOR_HELPER_MASTER, SUFFIX_KTOR_HELPER_LOCAL)
                        .map {
                            ClassName(daoTypeClassName.packageName,
                        "${daoTypeClassName.simpleName}$it${DbProcessorJdbcKotlin.SUFFIX_JDBC_KT}")
                        }
                codeBlock.add(", { %T(it) }", syncDaoImplClassName)
                        .add(",{ if(_isPrimary)·{·%T(it)·}·else·{·%T(it)·} }", helperClasses[0],
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

        specs.ktorRoute.fileSpec.addImport("com.ustadmobile.door", "DoorDbType")
                .addImport("io.ktor.response", "header")

        val ktorDaoParams = mutableListOf(
                ParameterSpec.builder("_daoFn",
                        LambdaTypeName.get(parameters = *arrayOf(TypeVariableName("T")),
                        returnType = daoTypeElement.asType().asTypeName()))
                            .addModifiers(KModifier.CROSSINLINE)
                            .build())
        val nanoHttpdDaoParams = mutableListOf(
                ParameterSpec.builder("_dao", daoTypeElement.asClassName()).build())


        val daoRouteFn = FunSpec.builder("${daoTypeElement.simpleName}$SUFFIX_KTOR_ROUTE")
                .addTypeVariable(TypeVariableName.invoke("T", DoorDatabase::class).copy(reified = true))
                .addModifiers(KModifier.INLINE)
                .receiver(Route::class)

        val ktorHelperClassName = ClassName(daoTypeElement.asClassName().packageName,
                "${daoTypeElement.simpleName}$SUFFIX_KTOR_HELPER")

        val daoHasSyncableEntities = syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv)
                .isNotEmpty()

        if(daoHasSyncableEntities) {
            val syncHelperClassName = ClassName(pkgNameOfElement(daoTypeElement, processingEnv),
                    "${daoTypeElement.simpleName}_SyncHelper")
            ktorDaoParams += ParameterSpec.builder("_syncHelperFn",
                    LambdaTypeName.get(parameters = *arrayOf(TypeVariableName("T")),
                            returnType = syncHelperClassName))
                    .addModifiers(KModifier.CROSSINLINE)
                    .build()
            ktorDaoParams += ParameterSpec.builder("_ktorHelperDaoFn",
                    LambdaTypeName.get(parameters = *arrayOf(TypeVariableName("T")),
                        returnType = ktorHelperClassName))
                    .addModifiers(KModifier.CROSSINLINE)
                    .build()

            nanoHttpdDaoParams += ParameterSpec.builder("_syncHelper",
                    syncHelperClassName).build()
            nanoHttpdDaoParams += ParameterSpec.builder("_ktorHelperDao",
                    ktorHelperClassName).build()
        }

        daoRouteFn.addParameters(ktorDaoParams)

        val ktorCodeBlock = CodeBlock.builder()
            .beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                "${daoTypeElement.simpleName}")

        val nanoHttpdGetFns = mutableListOf<String>()
        val nanoHttpdPostFns = mutableListOf<String>()

        val implementedMethodNames = mutableSetOf<String>()
        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv,
                includeImplementedMethods = true).forEach { daoSubEl ->
            if(daoSubEl.simpleName.toString() in implementedMethodNames) {
                return@forEach
            }

            implementedMethodNames += daoSubEl.simpleName.toString()
            val daoMethodEl = daoSubEl as ExecutableElement
            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType
            val resolvedReturnTypeName = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()
            val resultType = resolveQueryResultType(resolvedReturnTypeName)
            val componentEntityType = resolveEntityFromResultType(resultType)
            val nanoHttpdFun = FunSpec.builder(daoSubEl.simpleName.toString())
                    .returns(NanoHTTPD.Response::class)
                    .addParameter("_uriResource", RouterNanoHTTPD.UriResource::class)
                    .addParameter("_urlParams",
                            Map::class.parameterizedBy(String::class, String::class))
                    .addParameter("_session", NanoHTTPD.IHTTPSession::class)
                    .addParameters(nanoHttpdDaoParams)
            val nanoHttpdCodeBlock = CodeBlock.builder()


            val numNonQueryParams = getHttpBodyParams(daoMethodEl, daoMethodResolved).size

            val memberFn = if(numNonQueryParams == 1){
                nanoHttpdPostFns.add(daoSubEl.simpleName.toString())
                POST_MEMBER
            }else {
                nanoHttpdGetFns.add(daoSubEl.simpleName.toString())
                GET_MEMBER
            }


            ktorCodeBlock.beginControlFlow("%M(%S)", memberFn, daoSubEl.simpleName)
                    .addRequestDi()
                    .add("val _dao = _daoFn(_db)\n")
                    .add("val _gson: %T by _di.%M()\n", Gson::class, DI_INSTANCE_MEMBER)

            val funSpec = FunSpec.builder(daoMethodEl.simpleName.toString())
                    .returns(resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType())
            funSpec.takeIf {daoMethodResolved.parameterTypes.any { isContinuationParam(it.asTypeName()) }}
                    ?.addModifiers(KModifier.SUSPEND)

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
                ktorCodeBlock
                        .add("val _ktorHelperDao = _ktorHelperDaoFn(_db)\n")
                        .add("val _syncHelper = _syncHelperFn(_db)\n")
                        .add(generateKtorRouteSelectCodeBlock(funSpecBuilt,
                        daoTypeElement, serverType = SERVER_TYPE_KTOR))
                nanoHttpdCodeBlock.add(generateKtorRouteSelectCodeBlock(funSpecBuilt,
                        daoTypeElement, serverType = SERVER_TYPE_NANOHTTPD))

                val isDataSourceFactory = resolvedReturnTypeName is ParameterizedTypeName
                        && resolvedReturnTypeName.rawType == DataSource.Factory::class.asClassName()

                val helperParamList = funSpecBuilt.parameters.toMutableList()
                if(isDataSourceFactory) {
                    helperParamList += ParameterSpec.builder(PARAM_NAME_OFFSET, INT).build()
                    helperParamList += ParameterSpec.builder(PARAM_NAME_LIMIT, INT).build()
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
                val funSpecBuilt = funSpec.build()
                ktorCodeBlock.add(generateHttpServerPassToDaoCodeBlock(funSpecBuilt,
                        serverType = SERVER_TYPE_KTOR))
                nanoHttpdCodeBlock.add(generateHttpServerPassToDaoCodeBlock(funSpecBuilt,
                        serverType = SERVER_TYPE_NANOHTTPD))
            }

            ktorCodeBlock.endControlFlow()
            specs.nanoHttpdResponder.typeSpec.addFunction(nanoHttpdFun
                    .addCode(nanoHttpdCodeBlock.build())
                    .build())
        }

        syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv).forEach {
            val syncableEntityinfo = SyncableEntityInfo(it, processingEnv)
            ktorCodeBlock.add(generateUpdateTrackerReceivedKtorRoute(syncableEntityinfo.tracker))
            specs.nanoHttpdResponder.typeSpec.addFunction(generateUpdateTrackerReceivedNanoHttpdFun(
                    syncableEntityinfo.tracker)
                    .addParameters(nanoHttpdDaoParams)
                    .build()
                    .also { nanoHttpdGetFns.add(it.name) }
            )
        }

        queryResultTypesWithAnnotationOnDao(daoTypeElement.asClassName(), EntityWithAttachment::class.java,
                processingEnv).forEach {
            val entityTypeEl = processingEnv.elementUtils.getTypeElement(it.canonicalName) as TypeElement
            ktorCodeBlock.add(generateGetAttachmentDataCodeBlock(entityTypeEl))
        }



        specs.nanoHttpdResponder.typeSpec.addNanoHttpdUriResponderFuns(nanoHttpdGetFns,
                nanoHttpdPostFns, nanoHttpdDaoParams, daoTypeElement.asClassName(),
                isSyncable = daoHasSyncableEntities)

        ktorCodeBlock.endControlFlow()
        daoRouteFn.addCode(ktorCodeBlock.build())
        specs.ktorRoute.fileSpec.addFunction(daoRouteFn.build())

        return specs.toBuiltFileSpecs()
    }



    companion object {

        const val SUFFIX_KTOR_ROUTE = "_KtorRoute"

        const val SUFFIX_KTOR_HELPER = "_KtorHelper"

        const val SUFFIX_KTOR_HELPER_MASTER = "_KtorHelperMaster"

        const val SUFFIX_KTOR_HELPER_LOCAL = "_KtorHelperLocal"

        const val SUFFIX_NANOHTTPD_URIRESPONDER = "_UriResponder"

        const val SUFFIX_NANOHTTPD_ADDURIMAPPING = "_AddUriMapping"

        val GET_MEMBER = MemberName("io.ktor.routing", "get")

        val POST_MEMBER = MemberName("io.ktor.routing", "post")

        val CALL_MEMBER = MemberName("io.ktor.application", "call")

        val RESPOND_MEMBER = MemberName("io.ktor.response", "respond")

        val RESPONSE_HEADER = MemberName("io.ktor.response", "header")

        val DI_ON_MEMBER = MemberName("org.kodein.di", "on")

        val DI_INSTANCE_MEMBER = MemberName("org.kodein.di", "instance")

        val DI_INSTANCE_TYPETOKEN_MEMBER = MemberName("org.kodein.di", "Instance")

        const val SERVER_TYPE_KTOR = 1

        const val SERVER_TYPE_NANOHTTPD = 2

        internal val CODEBLOCK_NANOHTTPD_NO_CONTENT_RESPONSE = CodeBlock.of(
                "return %T.newFixedLengthResponse(%T.Status.NO_CONTENT, %T.MIME_TYPE_PLAIN, %S)\n",
                NanoHTTPD::class, NanoHTTPD.Response::class, DoorConstants::class, "")

        internal val CODEBLOCK_KTOR_NO_CONTENT_RESPOND = CodeBlock.of("%M.%M(%T.NoContent, %S)\n",
                CALL_MEMBER, RESPOND_MEMBER, HttpStatusCode::class, "")

        internal val NANOHTTPD_URIRESOURCE_FUNPARAMS = listOf<ParameterSpec>(
                ParameterSpec.builder("_uriResource", RouterNanoHTTPD.UriResource::class).build(),
                ParameterSpec.builder("_urlParams",
                        Map::class.parameterizedBy(String::class, String::class)).build(),
                ParameterSpec.builder("_session", NanoHTTPD.IHTTPSession::class).build())

    }
}