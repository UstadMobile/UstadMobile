package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class DbProcessorUtils {

    public static TypeSpec.Builder makeFactoryClass(TypeElement dbType, String implClassName) {
        return TypeSpec.classBuilder(dbType.getSimpleName() + "_Factory")
                .addModifiers(Modifier.PUBLIC)
                .addField(ClassName.get(dbType), "defaultInstance", Modifier.PRIVATE,
                        Modifier.STATIC, Modifier.VOLATILE)
                .addField(ParameterizedTypeName.get(ClassName.get(HashMap.class),
                        ClassName.get(String.class), ClassName.get(dbType)), "namedInstances",
                        Modifier.PRIVATE, Modifier.STATIC, Modifier.VOLATILE)
                .addStaticBlock(CodeBlock.of("namedInstances = new HashMap<>();\n"))
                .addMethod(MethodSpec.methodBuilder("make" + dbType.getSimpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addParameter(ClassName.get(Object.class), "context")
                        .returns(ClassName.get(dbType))
                        .addCode(CodeBlock.builder().add("if(defaultInstance == null) \n")
                                .add("\tdefaultInstance = new $L(context, \"$L\");\n",
                                        implClassName, dbType.getSimpleName().toString())
                                .add("return defaultInstance;\n").build()).build())
                .addMethod(MethodSpec.methodBuilder("make" + dbType.getSimpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.SYNCHRONIZED)
                        .addParameter(ClassName.get(Object.class), "context")
                        .addParameter(ClassName.get(String.class), "dbName")
                        .returns(ClassName.get(dbType))
                        .addCode(CodeBlock.builder().add("if(!namedInstances.containsKey(dbName)){\n")
                                .add("\tnamedInstances.put(dbName, new $L(context, dbName));\n", implClassName)
                                .add("}\n")
                                .add("return namedInstances.get(dbName);\n").build()).build())
                .addJavadoc("Generated code - DO NOT EDIT!");
    }

}

