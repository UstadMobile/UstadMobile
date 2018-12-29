package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface UmDao {

    int syncType() default UmSyncType.SYNC_NONE;

    String selectPermissionCondition() default "TRUE";

    String insertPermissionCondition() default "TRUE";

    String updatePermissionCondition() default "TRUE";

}
