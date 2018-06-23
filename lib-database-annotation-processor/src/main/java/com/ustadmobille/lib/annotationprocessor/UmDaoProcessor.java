package com.ustadmobille.lib.annotationprocessor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmEntity;

import java.io.IOException;
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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class UmDaoProcessor extends AbstractProcessor{

    private Messager messager;

    private Filer filer;

    private Elements elements;

    private Set<TypeElement> umDaoSet;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> set = new HashSet();
        set.add(UmDao.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        umDaoSet = new HashSet<>();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //look for things with umentity class
        Set<? extends Element> entityElements = roundEnvironment.getElementsAnnotatedWith(UmEntity.class);
        messager.printMessage(Diagnostic.Kind.NOTE, "Found " + entityElements.size() + " entities");
        MethodSpec numMethodSpec = MethodSpec.methodBuilder("getNumEntities")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassName.get("java.lang", "Integer"))
                .addStatement("return " + entityElements.size())
                .build();



        for(Element element : roundEnvironment.getElementsAnnotatedWith(UmDao.class)) {
            TypeElement typeElement = (TypeElement)element;
            if(!typeElement.getKind().equals(ElementKind.CLASS)) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can only be applied to class");
                return true;
            }


            umDaoSet.add(typeElement);
        }

        TypeSpec.Builder navigationClass = TypeSpec
                .classBuilder("Navigator")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        navigationClass.addMethod(numMethodSpec);

        for(TypeElement element : umDaoSet) {
            String methodName = "make_" + element.getSimpleName();
            MethodSpec methodSpec = MethodSpec.methodBuilder(methodName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(ClassName.get("java.lang", "Object"))
                    .addStatement("return new Object()")
                    .build();
            navigationClass.addMethod(methodSpec);

        }

        try {
            JavaFile.builder("com.ustadmobile.generated", navigationClass.build()).build()
                    .writeTo(filer);
        }catch(IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
