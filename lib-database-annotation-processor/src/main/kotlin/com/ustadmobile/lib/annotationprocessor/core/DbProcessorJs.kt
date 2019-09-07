package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Database
import com.squareup.kotlinpoet.*
import io.ktor.client.HttpClient
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType

private fun TypeSpec.Builder.addDbJsImplPropsAndConstructor(): TypeSpec.Builder {
    addProperty(PropertySpec.builder("_httpClient", HttpClient::class)
            .initializer("_httpClient").build())
    addProperty(PropertySpec.builder("_endpoint", String::class)
            .initializer("_endpoint").build())
    addProperty(PropertySpec.builder("_dbPath", String::class)
            .initializer("_dbPath").build())
    primaryConstructor(FunSpec.constructorBuilder()
            .addParameter("_httpClient", HttpClient::class)
            .addParameter("_endpoint", String::class)
            .addParameter("_dbPath", String::class)
            .build())

    return this
}

class DbProcessorJs : AbstractDbProcessor(){

    override fun process(elements: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        //find all databases on the source path
        if (processingEnv.options[AnnotationProcessorWrapper.OPTION_JS_OUTPUT] == null) {
            //skip output
            return true
        }

        roundEnv.getElementsAnnotatedWith(Database::class.java).map {it as TypeElement}.forEach { dbEl ->
            writeFileSpecToOutputDirs(generateDbJsImpl(dbEl),
                    AnnotationProcessorWrapper.OPTION_JS_OUTPUT, false)
        }

        roundEnv.getElementsAnnotatedWith(Dao::class.java).forEach { daoEl ->
            writeFileSpecToOutputDirs(generateDaoRepositoryClass(daoEl as TypeElement),
                    AnnotationProcessorWrapper.OPTION_JS_OUTPUT, false)
        }


        return true
    }


    fun generateDbJsImpl(dbTypeEl: TypeElement) : FileSpec {
        val dbTypeClassName = dbTypeEl.asClassName()
        val dbType = dbTypeEl.asType() as DeclaredType
        val implFileSpec = FileSpec.builder(dbTypeClassName.packageName,
                "${dbTypeClassName.simpleName}$SUFFIX_JS_IMPL")
        val implTypeSpec = TypeSpec.classBuilder(implFileSpec.name)
                .addDbJsImplPropsAndConstructor()
                .superclass(dbTypeEl.asClassName())
                .addFunction(FunSpec.builder("clearAllTables")
                        .addModifiers(KModifier.OVERRIDE)
                        .build())
                .addProperty(PropertySpec.builder("master", BOOLEAN)
                        .addModifiers(KModifier.OVERRIDE)
                        .initializer("false").build())


        val daoGetterMethods = methodsToImplement(dbTypeEl, dbType, processingEnv)
                .filter{it.kind == ElementKind.METHOD }.map {it as ExecutableElement }

        daoGetterMethods.forEach {
            val daoTypeEl = processingEnv.typeUtils.asElement(it.returnType) as TypeElement?
            if(daoTypeEl == null)
                return@forEach

            val daoTypeClassName = daoTypeEl.asClassName()

            val daoJsImplClassName = ClassName(daoTypeClassName.packageName,
                    "${daoTypeClassName.simpleName}$SUFFIX_JS_IMPL")

            implTypeSpec.addProperty(PropertySpec.builder("_${daoTypeClassName.simpleName}",
                    daoTypeClassName)
                    .delegate("lazy { %T(_httpClient, _endpoint, _dbPath) }", daoJsImplClassName)
                    .build())

            implTypeSpec.addAccessorOverride(it, CodeBlock.of("return _${daoTypeClassName.simpleName}\n"))
        }

        return implFileSpec.addType(implTypeSpec.build()).build()
    }


    fun generateDaoRepositoryClass(daoTypeEl: TypeElement): FileSpec {
        val daoTypeClassName = daoTypeEl.asClassName()
        val daoType = daoTypeEl.asType()
        val daoImplFile = FileSpec.builder(daoTypeClassName.packageName,
                "${daoTypeEl.simpleName}$SUFFIX_JS_IMPL")

        val daoTypeSpec = TypeSpec.classBuilder("${daoTypeClassName.simpleName}$SUFFIX_JS_IMPL")
                .addDbJsImplPropsAndConstructor()
                .superclass(daoTypeEl.asClassName())

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

            if(daoMethodResolved.parameterTypes.any { isContinuationParam(it.asTypeName())}) {
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
            }else {
                overrideFunSpec.addCode("throw %T(%S)\n", ClassName("kotlin", "IllegalStateException"),
                        "Javascript can only access DAO functions which are suspended or return LiveData/DataSource")
            }

            daoTypeSpec.addFunction(overrideFunSpec.build())
        }

        daoImplFile.addType(daoTypeSpec.build())
        return daoImplFile.build()

    }

    companion object {
        const val SUFFIX_JS_IMPL = "_JsImpl"
    }


}