package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import com.squareup.kotlinpoet.*
import javax.lang.model.element.TypeElement
import androidx.room.Dao
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.ustadmobile.door.DoorLiveData
import com.ustadmobile.door.annotation.Repository

internal fun TypeName.toSqlType(dbType: Int = 0) = when {
    this == BOOLEAN -> "BOOL"
    this == BYTE -> "SMALLINT"
    this == SHORT -> "SMALLINT"
    this == INT ->  "INTEGER"
    this == LONG -> "BIGINT"
    this == FLOAT -> "FLOAT"
    this == DOUBLE -> "DOUBLE PRECISION"
    this == String::class.asClassName() -> "TEXT"

    else -> "ERR_UNSUPPORTED_TPYE-$this"
}

internal fun TypeName.isDataSourceFactory(paramTypeFilter: (List<TypeName>) -> Boolean = {true}) = this is ParameterizedTypeName
        && this.rawType == DataSource.Factory::class.asClassName() && paramTypeFilter(this.typeArguments)


fun TypeName.isListOrArray() = (this is ClassName && this.canonicalName =="kotlin.Array")
        || (this is ParameterizedTypeName && this.rawType == List::class.asClassName())

fun TypeName.isList() = (this is ParameterizedTypeName && this.rawType == List::class.asClassName())
        || (this == List::class.asClassName())

/**
 * Determines whether or not this TypeName is nullable when it is the
 */
val TypeName.isNullableAsSelectReturnResult
    get() = this != UNIT
            && !PRIMITIVE.contains(this)
            && !(this is ParameterizedTypeName)


/**
 * If this TypeName represents a List of a type e.g. List<Foo> then return the classname for a
 * singular Foo. If this typename is not a list, just return it
 */
//TODO: Make this handle arrays as well
internal fun TypeName.asComponentClassNameIfList() : ClassName {
    return if(this is ParameterizedTypeName && this.rawType == List::class.asClassName()) {
        val typeArg = this.typeArguments[0]
        if(typeArg is WildcardTypeName) {
            typeArg.outTypes[0] as ClassName
        }else {
            typeArg as ClassName
        }
    }else {
        this as ClassName
    }
}


/**
 * Check to see if this represents a TypeElement for a Dao which is annotated with Repository.
 *
 * This means that repository classes for this should be generated.
 */
val TypeElement.isDaoWithRepository: Boolean
    get() = this.getAnnotation(Dao::class.java) != null
            && this.getAnnotation(Repository::class.java) != null

/**
 * If the given TypeName represents typed LiveData or a DataSource Factory, unwrap it to the
 * raw type.
 *
 * In the case of LiveData this is simply the first parameter type.
 * E.g. LiveData<Foo> will return 'Foo', LiveData<List<Foo>> will return List<Foo>
 *
 * In the case of a DataSource.Factory, this will be a list of the first parameter type (as a
 * DataSource.Factory is providing a list)
 * E.g. DataSource.Factory<Foo> will unwrap as List<Foo>
 */
fun TypeName.unwrapLiveDataOrDataSourceFactory()  =
        if(this is ParameterizedTypeName
                && rawType == DoorLiveData::class.asClassName()) {
            typeArguments[0]
        }else if(this is ParameterizedTypeName
                && rawType == DataSource.Factory::class.asClassName()) {
            List::class.asClassName().parameterizedBy(typeArguments[1])
        }else {
            this
        }

/**
 * Unwrap the component type of an array or list
 */
fun TypeName.unwrapListOrArrayComponentType() =
        if(this is ParameterizedTypeName &&
                (this.rawType == List::class.asClassName() || this.rawType == ClassName("kotlin.Array"))) {
            typeArguments[0]
        }else {
            this
        }

/**
 * Unwrap everything that could be wrapping query return types. This will unwrap DataSource.Factory,
 * LiveData, List, and Array to give the singular type. This can be useful if you want to know
 * the type of entity that is being used.
 */
fun TypeName.unwrapQueryResultComponentType() = unwrapLiveDataOrDataSourceFactory().unwrapListOrArrayComponentType()
