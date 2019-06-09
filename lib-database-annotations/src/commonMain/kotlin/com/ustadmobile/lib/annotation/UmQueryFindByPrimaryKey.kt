package com.ustadmobile.lib.database.annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation
/**
 * Indicates that the given method should auto generate a query method that will select the entity
 * (as per the class type of the first parameter of the method) by it's primary key
 */
class UmQueryFindByPrimaryKey
