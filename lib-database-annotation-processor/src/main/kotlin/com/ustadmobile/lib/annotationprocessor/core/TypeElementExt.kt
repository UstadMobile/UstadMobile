package com.ustadmobile.lib.annotationprocessor.core

import androidx.room.Dao
import com.squareup.kotlinpoet.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import androidx.room.*
import com.ustadmobile.door.SyncableDoorDatabase
import javax.lang.model.type.TypeMirror

internal fun TypeElement.asEntityTypeSpecBuilder(): TypeSpec.Builder {
    val typeSpecBuilder = TypeSpec.classBuilder(this.simpleName.toString())
    this.enclosedElements
            .filter { it.kind == ElementKind.FIELD
                    && it.simpleName.toString() != "Companion"
                    && Modifier.STATIC !in it.modifiers }
            .forEach {
        val propSpec = PropertySpec.builder(it.simpleName.toString(),
                it.asType().asTypeName().javaToKotlinType())
        propSpec.addAnnotations( it.annotationMirrors.map { AnnotationSpec.get(it) })
        typeSpecBuilder.addProperty(propSpec.build())
    }

    return typeSpecBuilder
}

internal fun TypeElement.asEntityTypeSpec() = this.asEntityTypeSpecBuilder().build()

internal fun TypeElement.hasDataSourceFactory(paramTypeFilter: (List<TypeName>) -> Boolean = {true})
        = enclosedElements.any { it.kind == ElementKind.METHOD
        && (it as ExecutableElement).returnType.asTypeName().isDataSourceFactory(paramTypeFilter) }

/**
 * Get a list of all the methods of the given TypeElement including those that are declared in
 * parent classes and interfaces that can be overriden. This function can exclude methods that are
 * already implemented in parent classes (including resolving generic types as required)
 */
fun TypeElement.allOverridableMethods(processingEnv: ProcessingEnvironment,
                                      enclosing: DeclaredType = this.asType() as DeclaredType,
                                      includeImplementedMethods: Boolean = false) :List<ExecutableElement> {
    return ancestorsAsList(processingEnv).flatMap {
        it.enclosedElements.filter {
            it.kind ==  ElementKind.METHOD
                    && (includeImplementedMethods || Modifier.ABSTRACT in it.modifiers) //abstract methods in this class
                    && (Modifier.FINAL !in it.modifiers)
        } + it.interfaces.flatMap {
            processingEnv.typeUtils.asElement(it).enclosedElements.filter { it.kind == ElementKind.METHOD } //methods from the interface
        }
    }.filter {
        includeImplementedMethods || !isMethodImplemented(it as ExecutableElement, this, processingEnv)
    }.distinctBy {
        val signatureParamTypes = (processingEnv.typeUtils.asMemberOf(enclosing, it) as ExecutableType)
                .parameterTypes.filter { ! isContinuationParam(it.asTypeName()) }
        MethodToImplement(it.simpleName.toString(), signatureParamTypes)
    }.map {
        it as ExecutableElement
    }
}

/**
 * Shorthand extension for a TypeElement that represents a class with the @Database annotation
 * that will give a list of all the DAO getter functions
 */
fun TypeElement.allDbClassDaoGetters(processingEnv: ProcessingEnvironment) =
        allOverridableMethods(processingEnv)
            .filter { it.returnType.asTypeElement(processingEnv)?.hasAnnotation(Dao::class.java) == true}

/**
 * Gets a list of all ancestor parent classes and interfaces.
 */
fun TypeElement.ancestorsAsList(processingEnv: ProcessingEnvironment): List<TypeElement> {
    val entityAncestors = mutableListOf<TypeElement>()

    var nextEntity = this as TypeElement?

    do {
        entityAncestors.add(nextEntity!!)
        val nextElement = processingEnv.typeUtils.asElement(nextEntity.superclass)
        nextEntity = if(nextElement is TypeElement && nextElement.qualifiedName.toString() != "java.lang.Object") {
            nextElement
        } else {
            null
        }
    }while(nextEntity != null)

    return entityAncestors
}

/**
 * Get a list of all the methods on this TypeElement that have any of the given annotations
 */
fun <A: Annotation> TypeElement.allMethodsWithAnnotation(annotationList: List<Class<out A>>): List<ExecutableElement> {
    return enclosedElements.filter { subEl ->
        subEl.kind == ElementKind.METHOD && annotationList.any { subEl.hasAnnotation(it) }
    }.map {
        it as ExecutableElement
    }
}

/**
 * Get a list of all the methods on this DAO that have a query that could modify the database
 */
fun TypeElement.allDaoClassModifyingQueryMethods() : List<ExecutableElement> {
    val annotations = listOf(Query::class.java, Update::class.java, Delete::class.java, Insert::class.java)
    return allMethodsWithAnnotation(annotations).filter {
        if(it.hasAnnotation(Query::class.java)) {
            it.getAnnotation(Query::class.java).value.isSQLAModifyingQuery()
        }else {
            true
        }
    }
}

/**
 * Where the TypeElement represents a database class, get a list of TypeElements representing
 * all the entities as per the @Database annotation.
 */
fun TypeElement.allDbEntities(processingEnv: ProcessingEnvironment): List<TypeElement> {
    val entityTypeElements = mutableListOf<TypeElement>()
    for (annotationMirror in getAnnotationMirrors()) {
        val annotationTypeEl = processingEnv.typeUtils
                .asElement(annotationMirror.getAnnotationType()) as TypeElement
        if (annotationTypeEl.qualifiedName.toString() != "androidx.room.Database")
            continue

        val annotationEntryMap = annotationMirror.getElementValues()
        for (entry in annotationEntryMap.entries) {
            val key = entry.key.getSimpleName().toString()
            val value = entry.value.getValue()
            if (key == "entities") {
                val typeMirrors = value as List<AnnotationValue>
                for (entityValue in typeMirrors) {
                    entityTypeElements.add(processingEnv.typeUtils
                            .asElement(entityValue.value as TypeMirror) as TypeElement)
                }
            }
        }
    }


    return entityTypeElements.toList()
}


fun TypeElement.asClassNameWithSuffix(suffix: String) =
        ClassName(packageName, "$simpleName$suffix")


fun TypeElement.isDbSyncable(processingEnv: ProcessingEnvironment): Boolean {
    return processingEnv.typeUtils.isAssignable(asType(),
            processingEnv.elementUtils.getTypeElement(SyncableDoorDatabase::class.java.canonicalName).asType())
}

