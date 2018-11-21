package com.ustadmobile.lib.annotationprocessor.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor8;

/**
 * This TypeVisitor is used to resolve types. For example
 *
 * interface BaseDao&lt;T&gt;
 *   void insert(T);
 *
 * and:
 *
 * FooDao implements BaseDao&lt;FooEntity&gt;
 *
 * Now when we generated the insert method - we must resolve T to being FooEntity. This type visitor
 * also works when the type variable is inherited (e.g. SyncableDao&lt;T&gt; extends BaseDao&lt;T&gt;
 * situations.
 */
public class TypeVariableResolutionVisitor extends SimpleTypeVisitor8<TypeMirror, List<TypeMirror>> {

    private TypeVariable typeVariable;

    private Element typeVariableOwner;

    private String typeVariableName;

    /**
     * Constructor
     *
     * @param typeVariable The TypeVariable to be resolved
     */
    public TypeVariableResolutionVisitor(TypeVariable typeVariable) {
        this.typeVariable = typeVariable;
        typeVariableName = typeVariable.asElement().getSimpleName().toString();
        this.typeVariableOwner = typeVariable.asElement().getEnclosingElement();
    }

    public TypeVariableResolutionVisitor(String typeVariableName, TypeElement typeVariableOwner) {
        this.typeVariableName = typeVariableName;
        this.typeVariableOwner = typeVariableOwner;
    }

    /**
     * visitDeclared implementation to resolve the TypeVariable
     *
     * @param declaredType The DeclaredType in which the TypeVariable is to be resolved (e.g. the
     *                     class in which it is being implemented, or the parent thereof)
     * @param typeArgumentsList A list of type arguments that are inherited from the last
     *                          DeclaredType we visited. Pass a blank list to start.
     * @return The resolved TypeMirror for the given type, or null if it's not resolvable
     */
    @Override
    public TypeMirror visitDeclared(DeclaredType declaredType,
                                    List<TypeMirror> typeArgumentsList) {
        TypeElement typeElement = (TypeElement)declaredType.asElement();
        Map<String, TypeMirror> typeMap = new HashMap<>();
        for(int i = 0; i < typeElement.getTypeParameters().size(); i++) {
            typeMap.put(typeElement.getTypeParameters().get(i).getSimpleName().toString(),
                    typeArgumentsList.get(i));
        }

        if(declaredType.asElement().equals(typeVariableOwner)) {
            //found it - return the resolved type
            return typeMap.get(typeVariableName);
        }

        if(!typeElement.getSuperclass().getKind().equals(TypeKind.NONE)) {
            TypeMirror superResult = visitType(typeMap, typeElement.getSuperclass());
            if(superResult != null)
                return superResult;
        }

        for(TypeMirror interfaceMirror : typeElement.getInterfaces()){
            TypeMirror result = visitType(typeMap, interfaceMirror);
            if(result != null)
                return result;
        }

        return null;
    }

    /**
     * Used to visit a super interface or super class
     *
     * @param typeMap
     * @param typeElement
     * @return
     */
    private TypeMirror visitType(Map<String, TypeMirror> typeMap, TypeMirror typeElement) {
        DeclaredType superDeclaredType = (DeclaredType)typeElement;
        List<TypeMirror> typeArguments = new ArrayList<>();
        for(TypeMirror superTypeArg : superDeclaredType.getTypeArguments()){
            if(superTypeArg.getKind().equals(TypeKind.TYPEVAR)) {
                TypeVariable typeVar = (TypeVariable)superTypeArg;
                TypeMirror inheritedTypeMirror = typeMap.get(
                        typeVar.asElement().getSimpleName().toString());
                typeArguments.add(inheritedTypeMirror);
            }else {
                typeArguments.add(superTypeArg);
            }
        }

        return typeElement.accept(this, typeArguments);
    }
}