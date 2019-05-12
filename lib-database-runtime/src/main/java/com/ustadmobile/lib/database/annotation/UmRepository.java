package com.ustadmobile.lib.database.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface UmRepository {

    @interface UmRepositoryMethodType {

        /**
         * If delegating to the webservice
         */
        int DELEGATE_TO_WEBSERVICE = 1;

        int DELEGATE_TO_DAO = 2;

        int INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO = 3;
    }

    int delegateType() default UmRepositoryMethodType.DELEGATE_TO_DAO;

}
