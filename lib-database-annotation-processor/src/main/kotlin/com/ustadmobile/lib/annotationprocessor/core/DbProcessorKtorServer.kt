package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import com.squareup.kotlinpoet.*
import io.ktor.http.HttpStatusCode
import io.ktor.routing.Route
import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

fun isQueryParam(typeName: TypeName) =
    if(QUERY_SINGULAR_TYPES.contains(typeName)) {
        true
    }else {
        typeName is ParameterizedTypeName && typeName.typeArguments[0] in QUERY_SINGULAR_TYPES
    }

class DbProcessorKtorServer: AbstractProcessor() {

    private var messager: Messager? = null

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
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

            codeBlock.add(generatePassToDaoCodeBlock(daoMethodResolved, daoMethodEl))

            codeBlock.endControlFlow()

        }
        codeBlock.endControlFlow()
        daoRouteFn.addCode(codeBlock.build())
        daoImplFile.addFunction(daoRouteFn.build())

        return daoImplFile.build()
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
            val paramTypeName = el.asType().asTypeName().javaToKotlinType()
            if(isContinuationParam(paramTypeName))
                return@forEachIndexed

            if(paramOutCount > 0)
                codeBlock.add(",")

            if(isQueryParam(paramTypeName)) {
                if(paramTypeName in QUERY_SINGULAR_TYPES) {
                    codeBlock.add("%M.request.queryParameters[%S]", CALL_MEMBER, el.simpleName)
                    if(paramTypeName == String::class.asTypeName()) {
                        codeBlock.add(" ?: \"\"")
                    }else {
                        codeBlock.add("?.to${(paramTypeName as ClassName).simpleName}() ?: ${defaultVal(paramTypeName)}")
                    }
                }else {
                    codeBlock.add("%M.request.queryParameters.getAll(%S)", CALL_MEMBER,
                            el.simpleName)
                    val parameterizedTypeName = paramTypeName as ParameterizedTypeName
                    if(parameterizedTypeName.typeArguments[0] != String::class.asClassName()) {
                        codeBlock.add("?.map { it.to${(parameterizedTypeName.typeArguments[0] as ClassName).simpleName}() }")
                    }
                    codeBlock.add(" ?: listOf()\n")
                }
            }else {
                codeBlock.add("%M.%M<%T>()", CALL_MEMBER,
                        MemberName("io.ktor.request", "receive"),
                        removeTypeProjection(daoMethodResolved.parameterTypes[index].asTypeName()))
            }

            paramOutCount++
        }

        codeBlock.add(")\n")

        when{
            returnType == UNIT -> codeBlock.add("%M.%M(%T.NoContent, \"\")\n", CALL_MEMBER,
                    RESPOND_MEMBER, HttpStatusCode::class)

            !isNullableResultType(returnType) -> codeBlock.add("%M.%M(_result)\n", CALL_MEMBER,
                    RESPOND_MEMBER)

            else -> codeBlock.beginControlFlow("if(_result != null)")
                    .add("%M.%M(_result)\n", CALL_MEMBER, RESPOND_MEMBER)
                    .nextControlFlow("else")
                    .add("%M.%M(%T.NoContent, \"\")\n", CALL_MEMBER,
                            RESPOND_MEMBER, HttpStatusCode::class)
                    .endControlFlow()
        }

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