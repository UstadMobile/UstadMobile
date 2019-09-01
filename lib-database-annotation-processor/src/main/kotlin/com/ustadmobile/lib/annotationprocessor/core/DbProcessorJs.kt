package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import com.squareup.kotlinpoet.*
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.tools.Diagnostic

class DbProcessorJs : AbstractDbProcessor(){

    override fun process(elements: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        //find all databases on the source path
        if (processingEnv.options[AnnotationProcessorWrapper.OPTION_JS_OUTPUT] == null) {
            //skip output
            return true
        }

        roundEnv.getElementsAnnotatedWith(Dao::class.java).forEach { daoEl ->
            writeFileSpecToOutputDirs(generateDaoRepositoryClass(daoEl as TypeElement),
                    AnnotationProcessorWrapper.OPTION_JS_OUTPUT, false)
        }


        return true
    }

    fun generateDaoRepositoryClass(daoTypeEl: TypeElement): FileSpec {
        val daoTypeClassName = daoTypeEl.asClassName()
        val daoType = daoTypeEl.asType()
        val daoImplFile = FileSpec.builder(daoTypeClassName.packageName,
                "${daoTypeEl.simpleName}$SUFFIX_JS_DAO")

        val daoTypeSpec = TypeSpec.classBuilder(daoTypeClassName.simpleName)

        methodsToImplement(daoTypeEl, daoType as DeclaredType, processingEnv).forEach {daoSubEl ->
            if (daoSubEl.kind != ElementKind.METHOD)
                return@forEach

            val daoMethodEl = daoSubEl as ExecutableElement

            val daoMethodResolved = processingEnv.typeUtils.asMemberOf(daoType, daoMethodEl) as ExecutableType

            val returnTypeResolved = resolveReturnTypeIfSuspended(daoMethodResolved).javaToKotlinType()
            val resultType = resolveQueryResultType(returnTypeResolved)

            //daoFunSpec is generated so that it can be passed to the generateKtorRequestCodeBlock method
            val (overrideFunSpec, daoFunSpec) = (0..1).map {overrideAndConvertToKotlinTypes(daoMethodEl,
                    daoType, processingEnv,
                    forceNullableReturn = isNullableResultType(returnTypeResolved),
                    forceNullableParameterTypeArgs = isLiveData(returnTypeResolved)
                            && isNullableResultType((returnTypeResolved as ParameterizedTypeName).typeArguments[0])) }
                    .zipWithNext()[0]

            daoFunSpec.addAnnotations(daoMethodEl.annotationMirrors.map { AnnotationSpec.get(it) })

            var codeBlock = generateKtorRequestCodeBlockForMethod(
                    daoName = daoTypeClassName.simpleName,
                    dbPathVarName = "_dbPath",
                    methodName = daoSubEl.simpleName.toString(),
                    httpResultType = resultType,
                    params = daoFunSpec.parameters)

            if(returnTypeResolved != UNIT) {
                codeBlock = CodeBlock.builder().add(codeBlock).add("return _httpResult\n").build()
            }

            overrideFunSpec.addCode(codeBlock)
            daoTypeSpec.addFunction(overrideFunSpec.build())
        }

        daoImplFile.addType(daoTypeSpec.build())
        return daoImplFile.build()

    }

    companion object {
        const val SUFFIX_JS_DAO = "_JsDaoImpl"
    }


}