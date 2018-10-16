package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)

/**
 * Indicates that the given method should auto generate a query method that will select the entity
 * (as per the class type of the first parameter of the method) by it's primary key
 */
public @interface UmQueryFindByPrimaryKey {
}
