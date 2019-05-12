package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mike on 1/13/18.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UmQuery {

    String value();

    /**
     * When an entity has a field annotated with @UmSyncLastModified, and if this query is an SQL
     * UPDATE, the query will by default by modified to set the last modified field. This can be
     * disabled by setting this to true.
     *
     * @return Whether or not this query (if it is an update query) will be modified automatically
     *         to set the lastModifiedBy field.
     */
    boolean noAutoUpdateSetLastModified() default false;

}
