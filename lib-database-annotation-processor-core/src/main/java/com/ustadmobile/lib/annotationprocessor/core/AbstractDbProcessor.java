package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.TypeName;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public abstract class AbstractDbProcessor {

    protected ProcessingEnvironment processingEnv;

    protected Messager messager;

    protected Filer filer;

    private String outputDirOpt;

    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.processingEnv = processingEnvironment;
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String destinationPath = processingEnv.getOptions().get(getOutputDirOpt());
        if(destinationPath == null)
            return true;

        File destinationDir = new File(destinationPath);

        for(Element dbClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            try {
                processDbClass((TypeElement)dbClassElement, destinationDir);

                for(Element subElement : dbClassElement.getEnclosedElements()) {
                    if (subElement.getKind() != ElementKind.METHOD)
                        continue;

                    ExecutableElement dbMethod = (ExecutableElement) subElement;
                    if(!dbMethod.getModifiers().contains(Modifier.ABSTRACT))
                        continue;

                    if(dbMethod.getAnnotation(UmDbContext.class) != null
                            || dbMethod.getAnnotation(UmClearAll.class) != null)
                        continue;



                    if(!dbMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                                dbClassElement.getSimpleName().toString() + "." +
                                        dbMethod.getSimpleName() +
                                        " abstract method must return a DAO or be annotated with @UmContext");
                        continue;
                    }

                    TypeElement returnTypeElement = (TypeElement)processingEnv.getTypeUtils()
                            .asElement(dbMethod.getReturnType());


                    if(returnTypeElement.getAnnotation(UmDao.class) != null) {
                        processDbDao(returnTypeElement, (TypeElement)dbClassElement, destinationDir);
                    }
                }
            }catch(IOException ioe) {
                messager.printMessage(Diagnostic.Kind.ERROR, "IOException processing DB "
                        + ioe.getMessage());
            }
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "running room processor");
        return true;
    }

    public abstract void processDbClass(TypeElement dbType, File destinationDir) throws IOException;

    public abstract void processDbDao(TypeElement daoType, TypeElement dbType, File destinationDir) throws IOException;

    public String getOutputDirOpt() {
        return outputDirOpt;
    }

    public void setOutputDirOpt(String outputDirOpt) {
        this.outputDirOpt = outputDirOpt;
    }

    public static String defaultValue(TypeMirror type) {
        TypeName typeName = TypeName.get(type);
        if(typeName.isBoxedPrimitive()){
            typeName = typeName.unbox();
        }

        if(typeName.equals(TypeName.INT) || typeName.equals(TypeName.LONG)
                || typeName.equals(TypeName.FLOAT) || typeName.equals(TypeName.DOUBLE)
                || typeName.equals(TypeName.BYTE) || typeName.equals(TypeName.SHORT)
                || typeName.equals(TypeName.CHAR) || typeName.equals(TypeName.BYTE)) {
            return "0";
        }else if(typeName.equals(TypeName.BOOLEAN)) {
            return "false";
        }else {
            return "null";
        }
    }

    /**
     * Get a list of elements (e.g. TypeElements) for all the parameters for a given method
     *
     * @param method Method for which we want a list of TypeElements
     *
     * @return List of Elements for each parameter.
     */
    protected List<Element> getMethodParametersAsElements(ExecutableElement method) {
        List<? extends VariableElement> variableElementList = method.getParameters();
        List<Element> variableTypeElements = new ArrayList<>();
        for(VariableElement variableElement : variableElementList) {
            variableTypeElements.add(processingEnv.getTypeUtils().asElement(variableElement.asType()));
        }

        return variableTypeElements;
    }

    protected boolean isVoid(TypeMirror typeMirror) {
        if(typeMirror.getKind().equals(TypeKind.VOID)) {
            return true;
        }else if(typeMirror.getKind().equals(TypeKind.DECLARED)
            && ((TypeElement)processingEnv.getTypeUtils().asElement(typeMirror)).getQualifiedName()
                .toString().equals("java.lang.Void")) {
            return true;
        }else {
            return false;
        }
    }

}
