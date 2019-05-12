package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public interface QueryMethodGenerator {

    MethodSpec generateQueryMethod(String querySql,
                                          ExecutableElement daoMethod,
                                          TypeElement daoType,
                                          TypeElement dbType,
                                          TypeSpec.Builder daoBuilder);

}
