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
    this == DOUBLE -> "DOUBLE"
    this == String::class.asClassName() -> "TEXT"

    else -> "ERR_UNSUPPORTED_TPYE-$this"
}

internal fun TypeName.isDataSourceFactory(paramTypeFilter: (List<TypeName>) -> Boolean = {true}) = this is ParameterizedTypeName
        && this.rawType == DataSource.Factory::class.asClassName() && paramTypeFilter(this.typeArguments)
