package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Types;

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
                                                           Class<? extends Annotation>  annotationClassList,
                                                           List<Element> resultsList,
                                                           int maxResults,
                                                           ProcessingEnvironment processingEnv) {
        return findElementsWithAnnotation(typeElement, Arrays.asList(annotationClassList),
                resultsList, maxResults, processingEnv);
    }

    public static List<Element> findElementsWithAnnotation(TypeElement typeElement,
                                                           List<Class<? extends Annotation>> annotationClassList,
                                                           List<Element> resultsList,
                                                           int maxResults,
                                                           ProcessingEnvironment processingEnv) {
        TypeMirror searchTypeMirror = typeElement.asType();
        while(!searchTypeMirror.getKind().equals(TypeKind.NONE)) {
            TypeElement searchElement = (TypeElement) processingEnv.getTypeUtils()
                    .asElement(searchTypeMirror);
            for(Element subElement : searchElement.getEnclosedElements()){
                for(Class<? extends Annotation> annotationClass : annotationClassList) {
                    if(subElement.getAnnotation(annotationClass) != null) {
                        resultsList.add(subElement);
                        if(maxResults > 0 && resultsList.size() >= maxResults)
                            return resultsList;
                    }
                }
            }

            for(TypeMirror interfaceMirror : searchElement.getInterfaces()){
                TypeElement interfaceElement = (TypeElement)processingEnv.getTypeUtils()
                    .asElement(interfaceMirror);
                findElementsWithAnnotation(interfaceElement, annotationClassList, resultsList, maxResults,
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


    /**
     * Determine if the given type can be transmitted as a query variable when using REST - e.g.
     * the parameter is either a primitive, String, or an array/list thereof.
     *
     * @param typeMirror TypeMirror of the given parameter
     * @param processingEnv ProcessingEnvironment
     * @return true if the type meets the criteria to be a query parameter for REST calls, false otherwise
     */
    public static boolean isQueryParam(TypeMirror typeMirror, ProcessingEnvironment processingEnv) {
        if (typeMirror.getKind().equals(TypeKind.ARRAY)) {
            typeMirror = ((ArrayType) typeMirror).getComponentType();
        }else if(processingEnv.getElementUtils().getTypeElement(List.class.getName())
                .equals(processingEnv.getTypeUtils().asElement(typeMirror))) {
            typeMirror = ((DeclaredType)typeMirror).getTypeArguments().get(0);
        }

        TypeName typeName = TypeName.get(typeMirror);
        if(typeName.isBoxedPrimitive() ||typeName.isPrimitive()) {
            return true;
        }else if(processingEnv.getElementUtils().getTypeElement(String.class.getName())
                .equals(processingEnv.getTypeUtils().asElement(typeMirror))) {
            return true;
        }

        return false;
    }

    /**
     * Get the number of parameters for a given method that cannot be transmitted as query parameters.
     * This is significant as JAX-WS REST APIs should not generally have more than one non-query
     * param variable, which is then transmitted as the request body.
     *
     * @param method ExecutableElement represeting the method
     * @param processingEnv Processing Environment
     * @param excludedTypes Types that are not actually transmitted... e.g. Callback arguments
     *
     * @return the number of non-query parameter arguments, excluding excludedTypes
     */
    public static int getNonQueryParamCount(ExecutableElement method,
                                            ProcessingEnvironment processingEnv,
                                            TypeElement... excludedTypes) {
        Types types = processingEnv.getTypeUtils();
        int nonQueryParamCount = 0;

        paramLoop:
        for(VariableElement param : method.getParameters()) {
            for(TypeElement excludedType : excludedTypes) {
                if(excludedType.equals(types.asElement(param.asType())))
                    continue paramLoop;
            }

            if(!isQueryParam(param.asType(), processingEnv))
                nonQueryParamCount++;
        }

        return nonQueryParamCount;
    }

    /**
     * Given a database class TypeElement and the DAO TypeElement, find the method on the
     * database class that will return the desired DAO.
     *
     * @param daoType DAO TypeElement
     * @param dbType Database TypeElement
     * @param processsingEnv Processing Environment
     *
     * @return ExecutableElement representing the database method that returns the desired DAO, or null if not found.
     */
    public static ExecutableElement findDaoGetter(TypeElement daoType, TypeElement dbType,
                                                  ProcessingEnvironment processsingEnv) {
        TypeMirror searchMirror = dbType.asType();
        while(!searchMirror.getKind().equals(TypeKind.NONE)) {
            TypeElement searchType = (TypeElement)processsingEnv.getTypeUtils().asElement(searchMirror);
            for(Element subElement : searchType.getEnclosedElements()) {
                if(!subElement.getKind().equals(ElementKind.METHOD))
                    continue;

                ExecutableElement executableElement = (ExecutableElement)subElement;
                if(daoType.equals(processsingEnv.getTypeUtils()
                        .asElement(executableElement.getReturnType())))
                    return executableElement;
            }
        }

        return null;
    }

    /**
     * Determine if the given type mirror is a List
     *
     * @param typeMirror TypeMirror to check
     * @param processingEnv processing environment
     * @return true if typeMirror is a List, false otherwise
     */
    public static boolean isList(TypeMirror typeMirror, ProcessingEnvironment processingEnv) {
        return processingEnv.getElementUtils().getTypeElement(List.class.getName())
                .equals(processingEnv.getTypeUtils().asElement(typeMirror));
    }

    /**
     * Get the component type of an array or list
     *
     * @param typeMirror TypeMirror that represents an array or a list
     * @param processingEnv Processing Environment
     * @return the component type of typeMirror, or null if it is neither a list or array
     */
    public static TypeMirror getArrayOrListComponentType(TypeMirror typeMirror,
                                                ProcessingEnvironment processingEnv) {
        if(typeMirror.getKind().equals(TypeKind.ARRAY)) {
            return ((ArrayType) typeMirror).getComponentType();
        }else if(isList(typeMirror, processingEnv)) {
            return ((DeclaredType)typeMirror).getTypeArguments().get(0);
        }else {
            return null;
        }
    }


    /**
     * Determine if the given element has an auto-increment primary key
     *
     * @param typeElement TypeElement representing the entity
     * @param processingEnv Processing Environment
     * @return true if the given element has an auto increment primary key, false otherwise
     */
    public static boolean entityHasAutoIncrementPrimaryKey(TypeElement typeElement,
                                                    ProcessingEnvironment processingEnv) {
        Element pkElement = findElementWithAnnotation(typeElement, UmPrimaryKey.class, processingEnv);
        if(pkElement == null)
            return false;

        return pkElement.getAnnotation(UmPrimaryKey.class).autoIncrement();
    }

    public static VariableElement findPrimaryKey(TypeElement entityType,
                                                 ProcessingEnvironment processingEnv) {
        for(Element subElement : getEntityFieldElements(entityType, processingEnv)) {
            if(subElement.getAnnotation(UmPrimaryKey.class) != null)
                return (VariableElement)subElement;
        }

        return null;
    }


    /**
     * Returns a list of the entity fields of a particular object. Synonamous to
     * getEntityFieldElements(entityTypeElement, false)
     *
     * @param entityTypeElement The TypeElement representing the entity, from which we wish to get
     *                          the field names
     * @return List of VariableElement representing the entity fields that are persisted
     */
    public static List<VariableElement> getEntityFieldElements(TypeElement entityTypeElement,
                                                           ProcessingEnvironment processingEnv) {
        return getEntityFieldElements(entityTypeElement, processingEnv, false);
    }


    /**
     * Returns a list of the entity fields of a particular object. If getAutoIncLast is true, then
     * any autoincrement primary key will always be returned at the end of the list, e.g. so that a
     * preparedstatement insert with or without an autoincrement id can share the same code to set
     * all other parameters.
     *
     * @param entityTypeElement The TypeElement representing the entity, from which we wish to get
     *                          the field names
     * @param getAutoIncLast if true, then always return any field that is auto increment at the very end
     * @return List of VariableElement representing the entity fields that are persisted
     */
    public static List<VariableElement> getEntityFieldElements(TypeElement entityTypeElement,
                                                               ProcessingEnvironment processingEnv,
                                                               boolean getAutoIncLast) {
        List<VariableElement> entityFieldsList = new ArrayList<>();
        VariableElement pkAutoIncField = null;
        for(Element subElement : entityTypeElement.getEnclosedElements()) {
            if(!subElement.getKind().equals(ElementKind.FIELD) ||
                    subElement.getModifiers().contains(Modifier.STATIC))
                continue;

            if(getAutoIncLast
                    && subElement.getAnnotation(UmPrimaryKey.class) != null
                    && subElement.getAnnotation(UmPrimaryKey.class).autoIncrement()) {
                pkAutoIncField = (VariableElement) subElement;
            }else {
                entityFieldsList.add((VariableElement) subElement);
            }
        }

        if(pkAutoIncField != null)
            entityFieldsList.add(pkAutoIncField);

        return entityFieldsList;
    }
}

