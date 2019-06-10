package com.ustadmobile.lib.database.annotation


/**
 * This annotation can be applied to an abstract method on a class with the @UmDatabase annotation.
 * This generated method will clear all data from all tables. It will not delete the tables themselves.
 */
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
annotation class UmClearAll
