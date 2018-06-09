package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmDatabase;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

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
        for(Element daoClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            TypeSpec.Builder factoryClassBuilder =
                    TypeSpec.classBuilder(daoClassElement.getSimpleName().toString() + "_Factory")
                    .addModifiers(Modifier.PUBLIC);
            PackageElement packageElement = findPackageElement(daoClassElement);

            MethodSpec makeMethodSpec = MethodSpec.methodBuilder("make" + daoClassElement.getSimpleName())
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(ParameterSpec.builder(
                            ClassName.get("java.lang", "Object"), "context").build())
                    .returns(ClassName.get((TypeElement)daoClassElement))
                    .addCode("throw new RuntimeException(\"must be replaced with an implementation\");\n")
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



        return true;
    }

    private PackageElement findPackageElement(Element element) {
        Element enclosing = element;
        while(enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = element.getEnclosingElement();
        }

        return (PackageElement)enclosing;
    }
}
