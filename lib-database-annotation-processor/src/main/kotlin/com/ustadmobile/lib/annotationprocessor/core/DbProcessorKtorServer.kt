package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Query
import com.squareup.kotlinpoet.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import java.io.File
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import java.util.*
import com.ustadmobile.door.DoorDatabase

/**
 * Generates a codeblock that will get a parameter from a request and add it to the codeblock
 * with the correct type. This can be with or without a variable declaration.
 *
 * e.g. request.queryParameters['uid']?.toLong() :? 0
 *
 */
fun generateGetParamFromRequestCodeBlock(typeName: TypeName, paramName: String,
                                         declareVariableName: String? = null,
                                         declareVariableType: String = "val"): CodeBlock {
    val codeBlock = CodeBlock.builder()

    if(declareVariableName != null){
        codeBlock.add("%L %L =", declareVariableType, declareVariableName)
    }

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
        codeBlock.add("%M.%M<%T>()", DbProcessorKtorServer.CALL_MEMBER,
                MemberName("io.ktor.request", "receive"),
                removeTypeProjection(typeName))
    }

    if(declareVariableName != null){
        codeBlock.add("\n")
    }

    return codeBlock.build()
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

class DbProcessorKtorServer: AbstractDbProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        setupDb(roundEnv)

        val daos = roundEnv.getElementsAnnotatedWith(Dao::class.java)
        val outputArg = processingEnv.options[OPTION_KTOR_OUTPUT]
        val outputDir = if(outputArg == null || outputArg == "filer") processingEnv.options["kapt.kotlin.generated"] else outputArg

        daos.forEach { generateDaoImplClass(it as TypeElement).writeTo(File(outputDir!!)) }


        return true
    }

    fun generateDaoImplClass(daoTypeElement: TypeElement): FileSpec {
        val daoImplFile = FileSpec.builder(pkgNameOfElement(daoTypeElement, processingEnv),
                "${daoTypeElement.simpleName}_${SUFFIX_KTOR_ROUTE}")
        daoImplFile.addImport("com.ustadmobile.door", "DoorDbType")
        daoImplFile.addImport("io.ktor.response", "header")

        val daoRouteFn = FunSpec.builder("${daoTypeElement.simpleName}Route")
                .receiver(Route::class)
                .addParameter("_dao", daoTypeElement.asType().asTypeName())
                .addParameter("_db", DoorDatabase::class)

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

            if(daoSubEl.getAnnotation(Query::class.java) != null) {
                codeBlock.add(generateSelectCodeBlock(daoMethodResolved, daoMethodEl,
                        daoTypeElement))
            }else {
                val funSpec = FunSpec.builder(daoMethodEl.simpleName.toString())
                        .returns(daoMethodResolved.returnType.asTypeName().javaToKotlinType())
                daoMethodEl.parameters.forEachIndexed { index, paramEl ->
                    funSpec.addParameter(paramEl.simpleName.toString(),
                            daoMethodResolved.parameterTypes[index].asTypeName())
                }


                codeBlock.add(generatePassToDaoCodeBlock(funSpec.build()))
            }

            codeBlock.endControlFlow()
        }

        syncableEntitiesOnDao(daoTypeElement.asClassName(), processingEnv).forEach {
            val entitySyncTracker = getEntitySyncTracker(
                    processingEnv.elementUtils.getTypeElement(it.canonicalName), processingEnv)
            val entitySyncTrackerEl = processingEnv.typeUtils.asElement(entitySyncTracker)
            val updateTrackerReceivedFunName = "_update${entitySyncTrackerEl.simpleName}Received"
            codeBlock.beginControlFlow("%M(%S)", GET_MEMBER, updateTrackerReceivedFunName)
                    .add("val _clientId = %M.request.%M(%S)?.toInt() ?: 0\n",
                        CALL_MEMBER,
                        MemberName("io.ktor.request","header"),
                        "X-nid")
                    .add(generateGetParamFromRequestCodeBlock(INT, "reqId", "_requestId"))
                    //TODO: Add the clientId to this query (to prevent other clients interfering)
                    .add("_syncHelper.$updateTrackerReceivedFunName(true, _requestId)\n")
                    .add("%M.%M(%T.NoContent, \"\")\n", CALL_MEMBER, RESPOND_MEMBER,
                            HttpStatusCode::class)
                    .endControlFlow()
        }

        codeBlock.endControlFlow()
        daoRouteFn.addCode(codeBlock.build())
        daoImplFile.addFunction(daoRouteFn.build())

        return daoImplFile.build()
    }

    fun generateSelectCodeBlock(daoMethodResolved: ExecutableType, daoMethodEl: ExecutableElement,
                                daoTypeEl: TypeElement) : CodeBlock {
        val codeBlock = CodeBlock.builder()
        val resultType = resolveQueryResultType(resolveReturnTypeIfSuspended(daoMethodResolved))

        daoMethodResolved.parameterTypes.map { it.asTypeName() }.filter { !isContinuationParam(it) }
                .forEachIndexed { index, paramType ->

            val paramName = daoMethodEl.parameters[index].simpleName.toString()
            codeBlock.add(generateGetParamFromRequestCodeBlock(paramType,
                    paramName, declareVariableName = paramName, declareVariableType = "val"))
        }




        val queryVarsMap = daoMethodResolved.parameterTypes.mapIndexed { index, typeMirror ->
            daoMethodEl.parameters[index].simpleName.toString() to typeMirror.asTypeName().javaToKotlinType()
        }.filter {
            !isContinuationParam(it.second)
        }.toMap().toMutableMap()

        var querySql = daoMethodEl.getAnnotation(Query::class.java).value
        val componentEntityType = resolveEntityFromResultType(resultType)
        val syncableEntitiesList = if(componentEntityType is ClassName) {
            findSyncableEntities(componentEntityType, processingEnv)
        }else {
            null
        }

        if(syncableEntitiesList != null) {
            codeBlock.add("val clientId = %M.request.%M(%S)?.toInt() ?: 0\n",
                    CALL_MEMBER,
                    MemberName("io.ktor.request","header"),
                    "X-nid")
                    .add("val _reqId = %T().nextInt()\n", Random::class)
                    .add("%M.response.header(%S, _reqId)\n", CALL_MEMBER, "X-reqid")
            queryVarsMap.put("clientId", INT)
            querySql = refactorSyncSelectSql(querySql, componentEntityType as ClassName,
                    processingEnv)
        }


        codeBlock.add(generateQueryCodeBlock(resultType, queryVarsMap, querySql, daoTypeEl, daoMethodEl))
        codeBlock.add(generateReplaceSyncableEntitiesTrackerCodeBlock("_result", resultType,
                processingEnv = processingEnv))

        codeBlock.add(generateRespondCall(resultType, "_result"))

        return codeBlock.build()
    }

    /**
     * Generates a Codeblock that will call the DAO method, and then call.respond with the result
     */
    fun generatePassToDaoCodeBlock(daoMethod: FunSpec): CodeBlock {
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