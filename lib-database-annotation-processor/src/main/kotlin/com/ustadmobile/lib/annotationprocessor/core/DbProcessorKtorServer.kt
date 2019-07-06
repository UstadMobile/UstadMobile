package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Query
import com.squareup.kotlinpoet.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import java.io.File
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import com.ustadmobile.door.*

fun isQueryParam(typeName: TypeName) =
    if(QUERY_SINGULAR_TYPES.contains(typeName)) {
        true
    }else {
        typeName is ParameterizedTypeName && typeName.typeArguments[0] in QUERY_SINGULAR_TYPES
    }

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
                .add("%M.%M($varName)\n", DbProcessorKtorServer.CALL_MEMBER, DbProcessorKtorServer.RESPOND_MEMBER)
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

        val daoRouteFn = FunSpec.builder("${daoTypeElement.simpleName}Route")
                .receiver(Route::class)
                .addParameter("_dao", daoTypeElement.asType().asTypeName())
                .addParameter("_db", DoorDatabase::class)
        val codeBlock = CodeBlock.builder()

        codeBlock.beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                "${daoTypeElement.simpleName}")
        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv).forEach { daoSubEl ->
            val daoMethodEl = daoSubEl as ExecutableElement
            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType

            val numNonQueryParams =  daoMethodEl.parameters
                    .map { it.asType().asTypeName().javaToKotlinType() }
                    .count { !isContinuationParam(it) && !isQueryParam(it) }

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
                codeBlock.add(generatePassToDaoCodeBlock(daoMethodResolved, daoMethodEl))
            }

            codeBlock.endControlFlow()

        }
        codeBlock.endControlFlow()
        daoRouteFn.addCode(codeBlock.build())
        daoImplFile.addFunction(daoRouteFn.build())

        return daoImplFile.build()
    }

    fun generateSelectCodeBlock(daoMethodResolved: ExecutableType, daoMethodEl: ExecutableElement,
                                daoTypeEl: TypeElement) : CodeBlock {
        val codeBlock = CodeBlock.builder()
        val returnType = resolveQueryResultType(resolveReturnTypeIfSuspended(daoMethodResolved))

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
        }.toMap()

        val querySql = daoMethodEl.getAnnotation(Query::class.java).value

        codeBlock.add(generateQueryCodeBlock(returnType, queryVarsMap, querySql, daoTypeEl, daoMethodEl))
        codeBlock.add(generateRespondCall(returnType, "_result!!"))

        return codeBlock.build()
    }

    /**
     * Generates a Codeblock that will call the DAO method, and then call.respond with the result
     */
    fun generatePassToDaoCodeBlock(daoMethodResolved: ExecutableType, daoMethodEl: ExecutableElement): CodeBlock {
        val codeBlock = CodeBlock.builder()
        val returnType = resolveReturnTypeIfSuspended(daoMethodResolved)
        if(returnType != UNIT) {
            codeBlock.add("val _result = ")
        }

        codeBlock.add("_dao.${daoMethodEl.simpleName}(")
        var paramOutCount = 0
        daoMethodEl.parameters.forEachIndexed {index, el ->
            val paramTypeName = daoMethodResolved.parameterTypes[index].asTypeName().javaToKotlinType()
            if(isContinuationParam(paramTypeName))
                return@forEachIndexed

            if(paramOutCount > 0)
                codeBlock.add(",")

            codeBlock.add(generateGetParamFromRequestCodeBlock(paramTypeName, el.simpleName.toString()))

            paramOutCount++
        }

        codeBlock.add(")\n")

        codeBlock.add(generateRespondCall(returnType, "_result"))

        return codeBlock.build()
    }

    companion object {

        const val OPTION_KTOR_OUTPUT = "door_ktor_server_out"

        const val SUFFIX_KTOR_ROUTE = "KtorRoute"

        val GET_MEMBER = MemberName("io.ktor.routing", "get")

        val POST_MEMBER = MemberName("io.ktor.routing", "post")

        val CALL_MEMBER = MemberName("io.ktor.application", "call")

        val RESPOND_MEMBER = MemberName("io.ktor.response", "respond")
    }
}