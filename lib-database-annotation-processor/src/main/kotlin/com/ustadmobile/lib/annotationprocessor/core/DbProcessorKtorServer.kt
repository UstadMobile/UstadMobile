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

        val daoRouteFn = FunSpec.builder("${daoTypeElement.simpleName}")
                .receiver(Route::class)
                .addParameter("_dao", daoTypeElement.asType().asTypeName())
        val codeBlock = CodeBlock.builder()

        val getMember = MemberName("io.ktor.routing", "get")
        val postMember = MemberName("io.ktor.routing", "post")
        val callMember = MemberName("io.ktor.application", "call")
        val respondMember = MemberName("io.ktor.response", "respond")

        codeBlock.beginControlFlow("%M(%S)", MemberName("io.ktor.routing", "route"),
                daoTypeElement.simpleName.toString())
        methodsToImplement(daoTypeElement, daoTypeElement.asType() as DeclaredType, processingEnv).forEach { daoSubEl ->
            val daoMethodEl = daoSubEl as ExecutableElement
            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoTypeElement.asType() as DeclaredType,
                    daoMethodEl) as ExecutableType

            val numNonQueryParams =  daoMethodEl.parameters
                    .map { it.asType().asTypeName().javaToKotlinType() }
                    .count { !isContinuationParam(it) && !isQueryParam(it) }

            val memberFn = if(numNonQueryParams == 1){
                postMember
            }else {
                getMember
            }


            codeBlock.beginControlFlow("%M(%S)", memberFn, daoSubEl.simpleName)

            val returnType = resolveReturnTypeIfSuspended(daoMethodResolved)
            if(returnType != UNIT) {
                codeBlock.add("val _result = ")
            }

            codeBlock.add("_dao.${daoSubEl.simpleName}(")
            var paramOutCount = 0
            daoSubEl.parameters.forEachIndexed {index, el ->
                val paramTypeName = el.asType().asTypeName().javaToKotlinType()
                if(isContinuationParam(paramTypeName))
                    return@forEachIndexed

                if(paramOutCount > 0)
                    codeBlock.add(",")

                if(isQueryParam(paramTypeName)) {
                    if(paramTypeName in QUERY_SINGULAR_TYPES) {
                        codeBlock.add("%M.request.queryParameters[%S]", callMember, el.simpleName)
                        if(paramTypeName == String::class.asTypeName()) {
                            codeBlock.add(" ?: \"\"")
                        }else {
                            codeBlock.add("?.to${(paramTypeName as ClassName).simpleName}() ?: ${defaultVal(paramTypeName)}")
                        }
                    }else {
                        codeBlock.add("%M.request.queryParameters.getAll(%S)", callMember,
                                el.simpleName)
                        val parameterizedTypeName = paramTypeName as ParameterizedTypeName
                        if(parameterizedTypeName.typeArguments[0] != String::class.asClassName()) {
                            codeBlock.add("?.map { it.to${(parameterizedTypeName.typeArguments[0] as ClassName).simpleName}() }")
                        }
                        codeBlock.add(" ?: listOf()\n")
                    }
                }else {
                    codeBlock.add("%M.%M<%T>()", callMember,
                            MemberName("io.ktor.request", "receive"),
                            removeTypeProjection(daoMethodResolved.parameterTypes[index].asTypeName()))
                }

                paramOutCount++
            }

            codeBlock.add(")\n")

            when{
                returnType == UNIT -> codeBlock.add("%M.%M(%T.NoContent, \"\")\n", callMember,
                        respondMember, HttpStatusCode::class)

                !isNullableResultType(returnType) -> codeBlock.add("%M.%M(_result)\n", callMember,
                        respondMember)

                else -> codeBlock.beginControlFlow("if(_result != null)")
                        .add("%M.%M(_result)\n", callMember, respondMember)
                        .nextControlFlow("else")
                        .add("%M.%M(%T.NoContent, \"\")\n", callMember,
                                respondMember, HttpStatusCode::class)
                        .endControlFlow()
            }

            codeBlock.endControlFlow()

        }
        codeBlock.endControlFlow()
        daoRouteFn.addCode(codeBlock.build())
        daoImplFile.addFunction(daoRouteFn.build())

        return daoImplFile.build()
    }

    companion object {

        const val OPTION_KTOR_OUTPUT = "door_ktor_server_out"

        const val SUFFIX_KTOR_ROUTE = "KtorRoute"
    }
}