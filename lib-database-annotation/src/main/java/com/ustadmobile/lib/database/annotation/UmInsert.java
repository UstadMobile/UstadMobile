package com.ustadmobile.lib.database.annotation;

/**
 * Created by mike on 1/15/18.
 */

public @interface UmInsert {

    int onConflict() default UmOnConflictStrategy.ABORT;

}
