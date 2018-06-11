package com.ustadmobile.lib.annotationprocessor.room;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmDatabase;

import org.reflections.Reflections;

import java.io.IOException;
import java.util.Set;
import java.util.Vector;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;


@SupportedAnnotationTypes({"*"})
public class DbProcessorRoom extends AbstractProcessor{

    private Messager messager;

    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
//        processingEnvironment.getElementUtils().getAllMembers(null).size()
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Reflections reflections = new Reflections();
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(UmDatabase.class);

        //generate room database class
//        TypeElement classTypeEl = processingEnv.getElementUtils().getTypeElement(
//                "com.ustadmobile.lib.database.annotation.IUmDatabase")



//        Vector<Class> objectMatches = new Vector<>();
//        FastClasspathScanner objScanner = new FastClasspathScanner().matchSubclassesOf(
//                Object.class, objectMatches::add);
//        objScanner.scan();
//        System.out.println(objectMatches);

        final Vector<Class> dbClassMatchVector = new Vector<>();
//        FastClasspathScanner scanner = new FastClasspathScanner()
//                .matchClassesWithAnnotation(UmDatabase.class, dbClassMatchVector::add);
//        ScanResult result = scanner.scan();



        for(Class dbClass : dbClassMatchVector) {
            TypeSpec.Builder roomDbSpec = TypeSpec.classBuilder(dbClass.getSimpleName() +
                    "_RoomDb").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .superclass(ClassName.get("android.arch.persistence.room",
                    "RoomDatabase"));

            try {
                JavaFile.builder(dbClass.getPackage().getName(),
                        roomDbSpec.build()).build().writeTo(filer);
            }catch(IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Exception writing Room Db class");
            }
        }



        return false;
    }


    //TODO: Put this into a utility class
    private PackageElement findPackageElement(Element element) {
        Element enclosing = element;
        while(enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = element.getEnclosingElement();
        }

        return (PackageElement)enclosing;
    }
}
