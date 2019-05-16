package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import androidx.room.Insert
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror

fun isList(type: TypeMirror, processingEnv: ProcessingEnvironment): Boolean =
        processingEnv.typeUtils.isAssignable(type, processingEnv.elementUtils.getTypeElement("java.lang.List").asType())


fun entityTypeFromParams(method: ExecutableElement, enclosing: DeclaredType, processingEnv: ProcessingEnvironment) : TypeMirror {
    val methodResolved = processingEnv.typeUtils.asMemberOf(enclosing, method) as ExecutableType
    val firstParamType = methodResolved.parameterTypes[0]
    if(isList(firstParamType, processingEnv)) {
        val listDt = firstParamType as DeclaredType

    }

}

class DbProcessorJdbcKotlin: AbstractProcessor() {

    private var messager: Messager? = null

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        messager = p0?.messager
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val daos = roundEnv!!.getElementsAnnotatedWith(Dao::class.java)

        for(daoElement in daos) {
            val daoTypeEl = daoElement as TypeElement
            val daoPkgName = processingEnv.elementUtils.getPackageOf(daoElement)!!.qualifiedName
                    .toString()

            val daoFile = FileSpec.builder(daoPkgName, "${daoElement.simpleName}_$SUFFIX_JDBC_KT")
            val daoType = TypeSpec.Companion.classBuilder("${daoElement.simpleName}_$SUFFIX_JDBC_KT")

            for(daoSubElement in daoTypeEl.enclosedElements) {
                if(daoSubElement.kind != ElementKind.METHOD) {
                    continue
                }

                val daoMethod = daoElement as ExecutableElement

                if(daoMethod.getAnnotation(Insert::class.java) != null) {
                    //Generate insert method
                    val funSpec = FunSpec.overriding(daoMethod, daoElement.asType() as DeclaredType,
                            processingEnv.typeUtils)


                }


            }
        }

        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }





    companion object {

        const val SUFFIX_JDBC_KT = "_JdbcKt"

    }
}