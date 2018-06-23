package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmNamedParameter;
import com.ustadmobile.lib.database.annotation.UmTransaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.NoType;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

/**
 * DbProcessorCore will generate a factory class for each database, and an _Intermediate class for
 * each Dao referenced with annotations to preserve the names of DAO method parameters
 *  ( see https://bugs.openjdk.java.net/browse/JDK-8191074 )
 */
public class DbProcessorCore extends AbstractProcessor{

    private Messager messager;

    private Filer filer;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(UmDatabase.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> daoSet = roundEnvironment.getElementsAnnotatedWith(UmDao.class);


        for(Element daoClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            TypeSpec.Builder factoryClassBuilder =
                    TypeSpec.classBuilder(daoClassElement.getSimpleName().toString() + "_Factory")
                    .addModifiers(Modifier.PUBLIC);
            PackageElement packageElement = findPackageElement(daoClassElement);

            MethodSpec makeMethodSpec = MethodSpec.methodBuilder(
                    "make" + daoClassElement.getSimpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ParameterSpec.builder(
                            ClassName.get("java.lang", "Object"), "context").build())
                    .returns(ClassName.get((TypeElement)daoClassElement))
                    .addCode("throw new RuntimeException(\"must be replaced with a platform implementation\");\n")
                    .build();
            factoryClassBuilder.addMethod(makeMethodSpec);

            try {
                JavaFile.builder(packageElement.getQualifiedName().toString(),
                        factoryClassBuilder.build()).build().writeTo(filer);
            }catch(IOException ioe) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Exception writing factory class "
                        + ioe.getMessage());
            }
        }

        for(Element daoElement : daoSet) {
            generateIntermediateDao((TypeElement)daoElement);
        }

        return true;
    }




    private void generateIntermediateDao(TypeElement element){
        TypeSpec.Builder intermediateDaoBuilder = TypeSpec.classBuilder(
                element.getSimpleName() + "_CoreIntermediate")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(ClassName.get(element));

        for(Element subElement : element.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement executableElement = (ExecutableElement)subElement;
            if(!(executableElement.getModifiers().contains(Modifier.ABSTRACT)
                || executableElement.getAnnotation(UmTransaction.class) != null))
                continue;

            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(executableElement.getSimpleName().toString())
                    .addAnnotation(Override.class)
                    .returns(TypeName.get(executableElement.getReturnType()))
                    .addModifiers(executableElement.getModifiers());

            for(VariableElement variableElement : executableElement.getParameters()) {
                ParameterSpec.Builder spec = ParameterSpec.builder(TypeName.get(variableElement.asType()),
                    variableElement.getSimpleName().toString());
                AnnotationSpec.Builder annotationBuilder =AnnotationSpec.builder(UmNamedParameter.class);
                annotationBuilder.addMember("value",
                        "\"" + variableElement.getSimpleName().toString() + "\"");
                spec.addAnnotation(annotationBuilder.build());
                methodBuilder.addParameter(spec.build());
            }

            if(!executableElement.getModifiers().contains(Modifier.ABSTRACT)) {
                //we need to make a super call
                CodeBlock.Builder superCall = CodeBlock.builder();
                List<String> paramNames = new ArrayList<>();
                for(VariableElement variableElement : executableElement.getParameters()) {
                    paramNames.add(variableElement.getSimpleName().toString());
                }

                methodBuilder.addCode(executableElement.getReturnType() instanceof NoType ?
                                "super.$L($L);\n" : "return super.$L($L);\n",
                        executableElement.getSimpleName().toString(),
                        String.join(", ", paramNames));
            }

            intermediateDaoBuilder.addMethod(methodBuilder.build());
        }

        try {

            JavaFile.builder(findPackageElement(element).getQualifiedName().toString(),
                    intermediateDaoBuilder.build()).build().writeTo(filer);
        }catch(IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Exception writing intermdiate DAO"
                + e.getMessage());
        }
    }

    private PackageElement findPackageElement(Element element) {
        Element enclosing = element;
        while(enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = element.getEnclosingElement();
        }

        return (PackageElement)enclosing;
    }
}
