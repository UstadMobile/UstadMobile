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
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
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

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_ROOM_OUTPUT;

/**
 * DbProcessorCore will generate a factory class for each database, and then run annotation
 * processors for each implementation to be generated.
 */

@SupportedOptions({OPT_ROOM_OUTPUT})
public class DbProcessorCore extends AbstractProcessor{

    public static final String OPT_ROOM_OUTPUT = "umdb_room_out";

    private Messager messager;

    private Filer filer;

    private DbProcessorRoom dbProcessorRoom;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(UmDatabase.class.getCanonicalName());
        set.add(UmDao.class.getCanonicalName());
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

        dbProcessorRoom = new DbProcessorRoom();
        dbProcessorRoom.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        String roomOut = processingEnv.getOptions().get(OPT_ROOM_OUTPUT);
        messager.printMessage(Diagnostic.Kind.NOTE, "Room out dir: " + roomOut);

        Set<? extends Element> daoSet = roundEnvironment.getElementsAnnotatedWith(UmDao.class);

        //Generate core factory method
        for(Element daoClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            TypeSpec.Builder factoryClassBuilder =
                    TypeSpec.classBuilder(daoClassElement.getSimpleName().toString() + "_Factory")
                    .addModifiers(Modifier.PUBLIC);
            PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(daoClassElement);

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

        boolean result = dbProcessorRoom.process(annotations, roundEnvironment);

        return result;
    }

}
