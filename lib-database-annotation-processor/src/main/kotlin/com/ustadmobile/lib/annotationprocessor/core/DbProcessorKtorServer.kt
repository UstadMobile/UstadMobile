package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Database
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
                                         gsonVarName: String = "_gson"): CodeBlock {
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
        codeBlock.add("$gsonVarName.fromJson(%M.%M<String>(), object: %T() {}.type)",
                    DbProcessorKtorServer.CALL_MEMBER,
                    MemberName("io.ktor.request", "receiveOrNull"),
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

class DbProcessorKtorServer: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)

        daos.forEach {
            writeFileSpecToOutputDirs(generateDaoImplClass(it as TypeElement),
                    AnnotationProcessorWrapper.OPTION_KTOR_OUTPUT)
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
            codeBlock.add("%M(_syncHelperDao, _db, _gson)\n",
                    MemberName(dbTypeClassName.packageName,
                            "${dbTypeClassName.simpleName}${DbProcessorSync.SUFFIX_SYNCDAO_ABSTRACT}_$SUFFIX_KTOR_ROUTE"))
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

            codeBlock.add("%M(_db.$daoFromDbGetter, _db, _gson", MemberName(daoTypeClassName.packageName,
                    "${daoTypeEl.simpleName}_$SUFFIX_KTOR_ROUTE"))
            if(syncableEntitiesOnDao(daoTypeClassName, processingEnv).isNotEmpty()) {
                codeBlock.add(", _syncHelperDao")
            }
            codeBlock.add(")\n")
        }
        codeBlock.endControlFlow()

        dbRouteFn.addCode(codeBlock.build())
        dbRouteFileSpec.addFunction(dbRouteFn.build())

        return dbRouteFileSpec.build()


    }

    fun generateDaoImplClass(daoTypeElement: TypeElement): FileSpec {
        val daoImplFile = FileSpec.builder(pkgNameOfElement(daoTypeElement, processingEnv),
                "${daoTypeElement.simpleName}_${SUFFIX_KTOR_ROUTE}")
        daoImplFile.addImport("com.ustadmobile.door", "DoorDbType")
        daoImplFile.addImport("io.ktor.response", "header")

        val daoRouteFn = FunSpec.builder("${daoTypeElement.simpleName}_$SUFFIX_KTOR_ROUTE")
                .receiver(Route::class)
                .addParameter("_dao", daoTypeElement.asType().asTypeName())
                .addParameter("_db", DoorDatabase::class)
                .addParameter("_gson", Gson::class)

        if(syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv).isNotEmpty()) {
            daoRouteFn.addParameter("_syncHelper",
                    ClassName(pkgNameOfElement(daoTypeElement, processingEnv),
                            "${daoTypeElement.simpleName}_SyncHelper"))
        }

        val codeBlock = CodeBlock.builder()

        codeBlock.beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                "${daoTypeElement.simpleName}")

        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv).forEach { daoSubEl ->
            val daoMethodEl = daoSubEl as ExecutableElement
            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType

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
            if(queryAnnotation != null) {
                funSpec.addAnnotation(AnnotationSpec.builder(Query::class.asClassName())
                        .addMember(CodeBlock.of("%S", queryAnnotation.value)).build())
                codeBlock.add(generateKtorRouteSelectCodeBlock(funSpec.build(),
                        daoTypeElement))
            }else {
                codeBlock.add(generateKtorPassToDaoCodeBlock(funSpec.build()))
            }

            codeBlock.endControlFlow()
        }

        syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv).forEach {
            val syncableEntityinfo = SyncableEntityInfo(it, processingEnv)
            codeBlock.add(generateUpdateTrackerReceivedCodeBlock(syncableEntityinfo.tracker))
        }

        codeBlock.endControlFlow()
        daoRouteFn.addCode(codeBlock.build())
        daoImplFile.addFunction(daoRouteFn.build())

        return daoImplFile.build()
    }



    companion object {

        const val OPTION_KTOR_OUTPUT = "door_ktor_server_out"

        const val SUFFIX_KTOR_ROUTE = "KtorRoute"

        val GET_MEMBER = MemberName("io.ktor.routing", "get")

        val POST_MEMBER = MemberName("io.ktor.routing", "post")

        val CALL_MEMBER = MemberName("io.ktor.application", "call")

        val RESPOND_MEMBER = MemberName("io.ktor.response", "respond")

        val RESPONSE_HEADER = MemberName("io.ktor.response", "header")
    }
}