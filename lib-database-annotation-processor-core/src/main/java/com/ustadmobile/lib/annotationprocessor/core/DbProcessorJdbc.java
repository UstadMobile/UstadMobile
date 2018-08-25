package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JDBC_OUTPUT;

public class DbProcessorJdbc extends AbstractDbProcessor {

    private static String SUFFIX_JDBC_DBMANAGER = "_Jdbc";

    public DbProcessorJdbc() {
        setOutputDirOpt(OPT_JDBC_OUTPUT);
    }

    public void processDbClass(TypeElement dbType, File destinationDir) throws IOException {
        String jdbcDbClassName = dbType.getSimpleName() + SUFFIX_JDBC_DBMANAGER;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(dbType);

        ClassName initialContextClassName = ClassName.get(InitialContext.class);
        TypeSpec.Builder jdbcDbTypeSpec = TypeSpec.classBuilder(jdbcDbClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addJavadoc("Generated code - DO NOT EDIT!")
                .addField(ClassName.get(Object.class), "_context")
                .addField(ClassName.get("javax.sql", "DataSource"), "_dataSource")
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(Object.class), "context")
                    .addParameter(TypeName.get(String.class), "dbName")
                    .addCode(CodeBlock.builder().add("\tthis._context = context;\n")
                        .add("\ttry {\n")
                        .add("\t\t$T iContext = new $T();\n", initialContextClassName, initialContextClassName)
                        .add("\t\tthis._dataSource = (DataSource)iContext.lookup(\"java:/comp/env/jdbc/\"+dbName);\n")
                        .add("\t}catch($T e){\n", ClassName.get(NamingException.class))
                        .add("\t\tthrow new RuntimeException(e);\n")
                        .add("\t}\n")
                        .build())
                .build());

        TypeSpec.Builder factoryClassSpec = DbProcessorUtils.makeFactoryClass(dbType, jdbcDbClassName);


        for(Element subElement : dbType.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement dbMethod = (ExecutableElement)subElement;
            if(dbMethod.getModifiers().contains(Modifier.STATIC))
                continue;


            MethodSpec.Builder overrideSpec =  MethodSpec.overriding(dbMethod);
            TypeKind returnTypeKind = dbMethod.getReturnType().getKind();
            if(returnTypeKind.equals(TypeKind.DECLARED)) {
                overrideSpec.addCode("return null;\n");
            }else if(returnTypeKind.equals(TypeKind.LONG) || returnTypeKind.equals(TypeKind.INT)) {
                overrideSpec.addCode("return 0;\n");
            }

            jdbcDbTypeSpec.addMethod(overrideSpec.build());

        }



        JavaFile.builder(packageElement.getQualifiedName().toString(), factoryClassSpec.build())
                .build().writeTo(destinationDir);
        JavaFile.builder(packageElement.getQualifiedName().toString(), jdbcDbTypeSpec.build())
                .build().writeTo(destinationDir);
    }

    @Override
    public void processDbDao(TypeElement dbDao, File DestinationDir) throws IOException {

    }
}
