package com.ustadmobile.lib.annotationprocessor.core;


import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam;
import com.ustadmobile.lib.database.annotation.UmUpdate;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * DaoMethodInfo is a convenience wrapper class that can work out information we frequently need
 * when generating DAO method implementations, e.g. is the method asynchronous? What is the result
 * type?
 *
 * Terminology:
 *
 * The ResultType refers to the value returned by the method, or used for a result callback
 * The EntityType refers to the POJO class annotated with @UmEntity being used on an insert, update,
 * or delete method
 */
public class DaoMethodInfo {

    private ProcessingEnvironment processingEnv;

    private ExecutableElement method;

    private TypeElement daoClass;

    TypeElement umCallbackTypeElement;

    TypeElement listTypeElement;

    /**
     * Wrapper constructor
     *
     * @param method The method that is to be generated (e.g. abstract method originating from a DAO
     *               class or interface, whether it is inherited or not)
     * @param daoClass The DAO class that is being generated. Where a method is being implemented from
     *                 a typed interface of superclass, this is used to resolve the type arguments.
     * @param processingEnv Annotation processor processing environment
     */
    public DaoMethodInfo(ExecutableElement method, TypeElement daoClass,
                         ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.method = method;
        this.daoClass = daoClass;
        umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        listTypeElement = processingEnv.getElementUtils().getTypeElement(List.class.getName());
    }

    private VariableElement getFirstParam() {
        return method.getParameters().get(0);
    }

    /**
     * Determines if the first parameter of this method is a List
     *
     * @return true if the first parameter is a list, false otherwise
     */
    public boolean hasEntityListParam() {
        TypeMirror firstParam = getFirstParam().asType();
        return processingEnv.getElementUtils().getTypeElement(List.class.getName()).equals(
                processingEnv.getTypeUtils().asElement(firstParam));
    }

    /**
     * Determines if the result type uses a java.util.List result (directly). This includes when a
     * value is returned directly, or via a callback. This does not check for LiveData where LiveData
     * contains a list.
     *
     * @return true if the result type is a list, false otherwise.
     */
    public boolean hasListResultType() {
        TypeMirror resultType = resolveResultType();
        if(!resultType.getKind().equals(TypeKind.DECLARED))
            return false;

        return listTypeElement.equals(processingEnv.getTypeUtils().asElement(resultType));
    }

    /**
     * Determine if the result type uses an array (directly). This includes when a value is returned
     * directly, or via a callback. This does not check for LiveData where the LiveData contains an
     * array.
     *
     * @return true if the result type is an array, false otherwise
     */
    public boolean hasArrayResultType() {
        return resolveResultType().getKind().equals(TypeKind.ARRAY);
    }


    /**
     * Determine if the first parameter of this method is an array
     *
     * @return true if the first parameter is an array, false otherwise
     */
    public boolean hasEntityArrayParam() {
        return getFirstParam().asType().getKind().equals(TypeKind.ARRAY);
    }

    /**
     * Find the method parameter that is an async callback (e.g. UmCallback)
     *
     * @return index of the parameter that is a UmCallback, -1 if there is no such parameter
     */
    public int getAsyncParamIndex() {
        return getMethodParametersAsElements().indexOf(umCallbackTypeElement);
    }

    public boolean isAsyncMethod() {
        return getAsyncParamIndex() != -1;
    }

    /**
     * Determine if this method uses a live data return type
     *
     * @return true if the return type is live data, false otherwise
     */
    public boolean isLiveDataReturn() {
        if(processingEnv.getElementUtils().getTypeElement(UmLiveData.class.getName())
                .equals(processingEnv.getTypeUtils().asElement(method.getReturnType()))) {
            return true;
        }else {
            return false;
        }
    }


    /**
     * Determine what type of entity parameter is being used for an Insert, Update, or Delete method
     * (where the entity is a parameter of the method). This will figure out the actually entity
     * TypeMirror, regardless of whether the parameter is specified directly, or if the method accepts
     * a list or array of the entity. If the parameter is a list or array, this will return the
     * component type
     *
     * @return TypeMirror representing the entity type
     */
    public TypeMirror resolveEntityParameterComponentType() {
        TypeMirror entityTypeMirror;
        if(hasEntityListParam()) {
            entityTypeMirror = ((DeclaredType)getFirstParam().asType()).getTypeArguments().get(0);
        }else if(hasEntityArrayParam()) {
            entityTypeMirror = ((ArrayType)getFirstParam().asType()).getComponentType();
        }else {
            entityTypeMirror = getFirstParam().asType();
        }

        entityTypeMirror = DbProcessorUtils.resolveType(entityTypeMirror, daoClass, processingEnv);

        return entityTypeMirror;
    }

    public TypeMirror resolveEntityParameterType() {
        return DbProcessorUtils.resolveType(getFirstParam().asType(), daoClass, processingEnv);
    }


    /**
     * Determine the result type of the method. The result is either the return value, or the callback
     * result type. This will also resolve type variables.
     *
     * @return TypeMirror representing the result type as above.
     */
    public TypeMirror resolveResultType() {
        int asyncParamIndex = getAsyncParamIndex();
        TypeMirror resultType;
        if(asyncParamIndex != -1) {
            DeclaredType callbackDeclaredType = (DeclaredType)method.getParameters()
                    .get(asyncParamIndex).asType();
            resultType = callbackDeclaredType.getTypeArguments().get(0);
        }else {
            resultType = method.getReturnType();
        }

        return DbProcessorUtils.resolveType(resultType, daoClass, processingEnv);
    }

    /**
     * Resolve the type of entity, or primitive. The difference to resolveResultType is that this
     * will resolve UmLiveData type parameters.
     *
     * @return TypeMirror of the entity of primitive that is being returned. This will not unwrap
     * the List or Array type if applicable, but types will be resolved.
     */
    public TypeMirror resolveResultEntityType() {
        if(isLiveDataReturn()) {
            TypeMirror resultType = ((DeclaredType)method.getReturnType()).getTypeArguments().get(0);
            return DbProcessorUtils.resolveType(resultType, daoClass, processingEnv);
        }else {
            return resolveResultType();
        }
    }

    /**
     * As per resolveResultEntityType, but if the result is a list or array, it will resolve the
     * component type
     *
     * @return
     */
    public TypeMirror resolveResultEntityComponentType() {
        return DbProcessorUtils.getArrayOrListComponentType(resolveResultType(), processingEnv);
    }


    /**
     * As per resolveResultType, but returning an element. Synonymous to
     *  daoMethodInfo.resolveReturnType()
     *
     * @return ResultType as an element
     */
    public Element resolveResultAsElement() {
        return processingEnv.getTypeUtils().asElement(resolveResultType());
    }

    /**
     * Resolve the return type of the method.
     *
     * @return TypeMirror for the return type of the method, with any type variables resolved
     */
    public TypeMirror resolveReturnType() {
        return DbProcessorUtils.resolveType(method.getReturnType(), daoClass, processingEnv);
    }

    protected List<Element> getMethodParametersAsElements() {
        List<? extends VariableElement> variableElementList = method.getParameters();
        List<Element> variableTypeElements = new ArrayList<>();
        for(VariableElement variableElement : variableElementList) {
            variableTypeElements.add(processingEnv.getTypeUtils().asElement(variableElement.asType()));
        }

        return variableTypeElements;
    }

    public boolean hasArrayOrListParameter() {
        for(VariableElement param : method.getParameters()) {
            if(param.asType().getKind().equals(TypeKind.ARRAY))
                return true;
            else if(listTypeElement.equals(processingEnv.getTypeUtils().asElement(param.asType())))
                return true;
        }

        return false;
    }

    /**
     * Determine if the given method is an Update or Insert method (e.g. may require code to
     * increment change sequence numbers)
     *
     * @return true if the method is annotated with Update or Insert, false otherwise
     */
    public boolean isUpdateOrInsert() {
        return method.getAnnotation(UmUpdate.class) != null
                || method.getAnnotation(UmInsert.class) != null;
    }

    /**
     *
     * @return
     */
    public VariableElement getEntityParameterElement() {
        //TODO: go over the list to find the entity parameter
        return method.getParameters().get(0);
    }

    /**
     * Check if this is an insert method, where the entity has a syncable primary key.
     *
     * @return true if this is an insert method, and the entity being inserted using a syncable
     * primary key, false otherwise
     */
    public boolean isInsertWithAutoSyncPrimaryKey() {
        if(method.getAnnotation(UmInsert.class) == null)
            return false;

        TypeElement entityTypeParam = (TypeElement)processingEnv.getTypeUtils().asElement(
                resolveEntityParameterComponentType());
        VariableElement primaryKeyEl = DbProcessorUtils.findPrimaryKey(entityTypeParam,
                processingEnv);

        return primaryKeyEl.getAnnotation(UmPrimaryKey.class).autoGenerateSyncable();
    }

    /**
     * Find the parameter annotated with UmRestAuthorizedUidParam
     *
     * @return VariableElement representing the parameter annotated with UmRestAuthorizedUidParam
     *  if present, otherwise null
     */
    public VariableElement getAuthorizedUidParam(){
        for(VariableElement param : method.getParameters()) {
            if(param.getAnnotation(UmRestAuthorizedUidParam.class) != null)
                return param;
        }

        return null;
    }


}
