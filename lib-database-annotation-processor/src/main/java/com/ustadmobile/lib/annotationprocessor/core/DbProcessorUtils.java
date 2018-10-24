package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

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

    public static List<Element> findElementsWithAnnotation(TypeElement typeElement,
                                                           Class<? extends Annotation> annotationClass,
                                                           List<Element> resultsList,
                                                           int maxResults,
                                                           ProcessingEnvironment processingEnv) {
        TypeMirror searchTypeMirror = typeElement.asType();
        while(!searchTypeMirror.getKind().equals(TypeKind.NONE)) {
            TypeElement searchElement = (TypeElement) processingEnv.getTypeUtils()
                    .asElement(searchTypeMirror);
            for(Element subElement : searchElement.getEnclosedElements()){
                if(subElement.getAnnotation(annotationClass) != null) {
                    resultsList.add(subElement);
                    if(resultsList.size() >= maxResults)
                        return resultsList;
                }

            }

            for(TypeMirror interfaceMirror : searchElement.getInterfaces()){
                TypeElement interfaceElement = (TypeElement)processingEnv.getTypeUtils()
                    .asElement(interfaceMirror);
                findElementsWithAnnotation(interfaceElement, annotationClass, resultsList, maxResults,
                        processingEnv);
            }

            searchTypeMirror = searchElement.getSuperclass();
        }


        return resultsList;
    }

    public static Element findElementWithAnnotation(TypeElement typeElement,
                                                    Class<? extends Annotation> annotation,
                                                    ProcessingEnvironment processingEnv) {
        List<Element> result = findElementsWithAnnotation(typeElement, annotation, new ArrayList<>(),
                1, processingEnv);
        return !result.isEmpty() ? result.get(0) : null;
    }

    /**
     * Simply capitalize the first character of a string (e.g. for setter/getter method names)
     *
     * @param string
     * @return
     */
    public static String capitalize(String string) {
        return Character.toUpperCase(string.charAt(0)) + string.substring(1);
    }

    public static String capitalize(Name name) {
        return capitalize(name.toString());
    }

    public static TypeMirror resolveType(TypeMirror variableMirror, TypeElement implementationClass,
                                         boolean resolveTypeArgs, boolean resolveArrays,
                                         ProcessingEnvironment processingEnv) {

        if(resolveTypeArgs && variableMirror.getKind().equals(TypeKind.DECLARED)){
            DeclaredType dt = (DeclaredType)variableMirror;
            TypeMirror[] typeMirrors = new TypeMirror[dt.getTypeArguments().size()];
            boolean declaredTypeArgumentsResolved = false;
            for(int i = 0; i < dt.getTypeArguments().size(); i++) {
                if(dt.getTypeArguments().get(i).getKind().equals(TypeKind.TYPEVAR)) {
                    declaredTypeArgumentsResolved = true;
                    typeMirrors[i] = resolveType(dt.getTypeArguments().get(i), implementationClass,
                            true, resolveArrays, processingEnv);
                }else {
                    typeMirrors[i] = dt.getTypeArguments().get(i);
                }
            }

            if(declaredTypeArgumentsResolved)
                variableMirror = processingEnv.getTypeUtils().getDeclaredType(
                        (TypeElement)processingEnv.getTypeUtils().asElement(variableMirror), typeMirrors);
        }else if(resolveArrays && variableMirror.getKind().equals(TypeKind.ARRAY)
                && ((ArrayType)variableMirror).getComponentType().getKind().equals(TypeKind.TYPEVAR)) {
            TypeVariable arrayCompTypeVariable = (TypeVariable)((ArrayType)variableMirror)
                    .getComponentType();
            variableMirror = processingEnv.getTypeUtils().getArrayType(
                    resolveType(arrayCompTypeVariable, implementationClass, resolveTypeArgs, true,
                            processingEnv));
        }

        if(variableMirror.getKind().equals(TypeKind.TYPEVAR)) {
            return implementationClass.asType().accept(new TypeVariableResolutionVisitor(
                    (TypeVariable)variableMirror), new ArrayList<>());
        }else {
            return variableMirror;
        }
    }



    public static TypeMirror resolveType(TypeMirror typeMirror, TypeElement implementationClass,
                                         ProcessingEnvironment processingEnv) {
        return resolveType(typeMirror, implementationClass, true, true,
                processingEnv);
    }


}

