package com.ustadmobile.lib.annotationprocessor.core

import androidx.paging.DataSource
import com.squareup.kotlinpoet.*

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
