package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

/**
 * This base processor is overriden to make platform specific implementations of the database. By
 * calling the process method, it will find all DAOs and Database classes, and call processDbClass
 * and processDbDao accordingly.
 */
public abstract class AbstractDbProcessor {

    protected ProcessingEnvironment processingEnv;

    protected Messager messager;

    protected Filer filer;

    private String outputDirOpt;

    public static final String DESTINATION_FILER = "filer";


    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.processingEnv = processingEnvironment;
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String destination = processingEnv.getOptions().get(getOutputDirOpt());
        if(destination == null)
            return true;

        File destinationDir = new File(destination);

        for(Element dbClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            try {
                processDbClass((TypeElement)dbClassElement, destination);

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
                        processDbDao(returnTypeElement, (TypeElement)dbClassElement, destination);
                    }
                }
            }catch(IOException ioe) {
                messager.printMessage(Diagnostic.Kind.ERROR, "IOException processing DB "
                        + ioe.getMessage());
            }
        }

        messager.printMessage(Diagnostic.Kind.NOTE, "running room processor");
        onDone();

        return true;
    }

    /**
     * This method will be called for each class annotated with UmDatabase found. It should be
     * implemented for each platform and generate an implementation of the Database class.
     *
     * @param dbType TypeElement representing the database class
     * @param destination This can be "filer", indicating that output should go directly to the
     *                    annotation processor filer, or a file path (e.g. to put output in a
     *                    different directory)
     *
     * @throws IOException If an IOException occurs attmepting to write output
     */
    public abstract void processDbClass(TypeElement dbType, String destination) throws IOException;

    /**
     * This method will be called for each class annotated with UmDao found. It should be implemented
     * for each platform and generate an implementation of the DAO
     *
     * @param daoType TypeElement representing the DAO
     * @param dbType TypeElement representing the database
     * @param destination This can be "filer", indicating that output should go directly to the
     *      *                    annotation processor filer, or a file path (e.g. to put output in a
     *      *                    different directory)
     * @throws IOException If an IOException occurs attmepting to write output
     */
    public abstract void processDbDao(TypeElement daoType, TypeElement dbType, String destination) throws IOException;

    /**
     * Get the name of the output option for this processor e.g. umdb_jdbc_out
     *
     * @return The name of the output option for this processor e.g. umdb_jdbc_out
     */
    public String getOutputDirOpt() {
        return outputDirOpt;
    }

    /**
     * Set the name of the output option for this processor e.g. umdb_jdbc_out
     *
     * @param outputDirOpt The name of the output option for this processor e.g. umdb_jdbc_out
     */
    public void setOutputDirOpt(String outputDirOpt) {
        this.outputDirOpt = outputDirOpt;
    }

    public static String defaultValue(TypeMirror type) {
        TypeName typeName = TypeName.get(type);
        if(typeName.isBoxedPrimitive()){
            typeName = typeName.unbox();
        }

        if(typeName.equals(TypeName.INT)) {
            return "0";
        }else if(typeName.equals(TypeName.LONG)) {
            return "0L";
        }else if(typeName.equals(TypeName.FLOAT)) {
            return "0f";
        }else if(typeName.equals(TypeName.DOUBLE)) {
            return "0d";
        }else if(typeName.equals(TypeName.BYTE)) {
            return "0b";
        }else if(typeName.equals(TypeName.SHORT)) {
            return "(short)0";
        }else if(typeName.equals(TypeName.CHAR)) {
            return "(char)0";
        }else if(typeName.equals(TypeName.BYTE)) {
            return "0b";
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

    /**
     * Can be overriden to clean up temporary files etc.
     */
    protected void onDone() {

    }

    protected void writeJavaFileToDestination(JavaFile javaFile, String destination) throws IOException {
        if(destination.equals(DESTINATION_FILER)) {
            javaFile.writeTo(filer);
        }else {
            javaFile.writeTo(new File(destination));
        }
    }

    protected List<VariableElement> getEntityFieldElements(TypeElement entityTypeElement) {
        List<VariableElement> entityFieldsList = new ArrayList<>();
        for(Element subElement : entityTypeElement.getEnclosedElements()) {
            if(!subElement.getKind().equals(ElementKind.FIELD) ||
                    subElement.getModifiers().contains(Modifier.STATIC))
                continue;

            entityFieldsList.add((VariableElement)subElement);
        }

        return entityFieldsList;
    }


    /**
     * Get a list of methods that need to be implemented for the given class. This would generally
     * be an abstract class. The methods include any abstract methods (including those inherited
     * from ancestors and interfaces).
     *
     * @param clazz TypeElement representing the abstract class for which we want to determine which
     *              methods are left to be implemented.
     *
     * @return A list of methods that need to be implemented for any non-abstract child class.
     */
    protected List<ExecutableElement> findDaoMethodsToImplement(TypeElement clazz) {
        return findDaoMethodsToImplement(clazz, clazz, new ArrayList<>());
    }


    protected List<ExecutableElement> findDaoMethodsToImplement(TypeElement clazz,
                                                                TypeElement daoClass,
                                                                List<ExecutableElement> methodsToImplement) {
        TypeElement searchClass = clazz;

        List<TypeMirror> interfaces = new ArrayList<>();
        while(searchClass != null) {
            for(Element subElement : searchClass.getEnclosedElements()) {
                if (!subElement.getKind().equals(ElementKind.METHOD))
                    continue;

                if (!subElement.getModifiers().contains(Modifier.ABSTRACT))
                    continue;

                ExecutableElement method = (ExecutableElement) subElement;
                if(!isMethodImplemented(method, daoClass)
                        && !listContainsMethod(method, methodsToImplement, daoClass)) {
                    methodsToImplement.add(method);
                }
            }

            interfaces.addAll(searchClass.getInterfaces());

            searchClass = searchClass.getSuperclass().getKind().equals(TypeKind.NONE) ?
                    null : (TypeElement)processingEnv.getTypeUtils().asElement(searchClass.getSuperclass());
        }

        for(TypeMirror interfaceMirror : interfaces) {
            findDaoMethodsToImplement(
                    (TypeElement)processingEnv.getTypeUtils().asElement(interfaceMirror),
                    daoClass, methodsToImplement);
        }

        return methodsToImplement;
    }

    private boolean isMethodImplemented(ExecutableElement method, TypeElement daoClass) {
        TypeElement searchClass = daoClass;
        while(searchClass != null) {
            for(Element subElement : searchClass.getEnclosedElements()){
                if(!subElement.getKind().equals(ElementKind.METHOD))
                    continue;

                ExecutableElement subMethod = (ExecutableElement)subElement;
                if(subMethod.getModifiers().contains(Modifier.ABSTRACT))
                    continue;

                if(!subMethod.getSimpleName().equals(method.getSimpleName()))
                    continue;

                if(areMethodParamSignaturesMatching(method, subMethod, daoClass))
                    return true;
            }

            searchClass = searchClass.getSuperclass().getKind().equals(TypeKind.NONE) ?
                    null :
                    (TypeElement)processingEnv.getTypeUtils().asElement(searchClass.getSuperclass());
        }


        return false;
    }

    private boolean areMethodParamSignaturesMatching(ExecutableElement method1,
                                                     ExecutableElement method2,
                                                     TypeElement implementingClass) {
        if(method1.getParameters().size() != method2.getParameters().size())
            return false;

        for(int i = 0; i < method1.getParameters().size(); i++) {
            TypeMirror method1ResolvedType = resolveDeclaredType(method1.getParameters().get(0).asType(),
                    implementingClass, (TypeElement)method1.getEnclosingElement(), processingEnv);
            TypeMirror method2ResolvedType = resolveDeclaredType(method2.getParameters().get(0).asType(),
                    implementingClass, (TypeElement)method2.getEnclosingElement(), processingEnv);

            //check if these are the same as far as the method signature is concerned - use toString
            if(!method1ResolvedType.toString().equals(method2ResolvedType.toString()))
                return false;
        }

        return true;
    }

    private boolean listContainsMethod(ExecutableElement method, List<ExecutableElement> methodList,
                                       TypeElement implementingClass) {
        for(ExecutableElement checkMethod : methodList) {
            if(!checkMethod.getSimpleName().equals(method.getSimpleName()))
                continue;

            if(areMethodParamSignaturesMatching(method, checkMethod, implementingClass))
                return true;
        }

        return false;
    }

    /**
     * Where one has a situation such as :
     *
     * abstract class SomeDao implements SomeInterface&lt;Entity&gt; ...
     *
     * interface SomeInterface&lt;T&gt;
     *   void insert(T entity) ...
     *
     * We need to determine the type argument used for T when we are generating a method, even when
     * it has not been directly declared in SomeDao to be able to generate the method for it.
     *
     * @param childTypeEl The child class (or interface) e.g. SomeDao
     * @param parentTypeEl The parent class (or interface) e.g. SomeInterface
     *
     * @return The DeclaredType where this has been declared, from which the type arguments can be obtained
     */
    public static DeclaredType findDeclaredType(TypeElement childTypeEl, TypeElement parentTypeEl,
                                                ProcessingEnvironment processingEnv) {
        DeclaredType result;

        TypeMirror checkClazzType = childTypeEl.asType();
        while(!checkClazzType.getKind().equals(TypeKind.NONE)) {
            TypeElement checkClazzEl = (TypeElement)processingEnv.getTypeUtils()
                    .asElement(checkClazzType);
            if(parentTypeEl.equals(checkClazzEl)
                    && checkClazzType instanceof DeclaredType) {
                return (DeclaredType)checkClazzType;
            }

            for(TypeMirror interfaceMirror : checkClazzEl.getInterfaces()){
                TypeElement checkInterfaceEl = (TypeElement)processingEnv.getTypeUtils()
                        .asElement(interfaceMirror);
                if(checkInterfaceEl.equals(parentTypeEl)
                        && interfaceMirror instanceof DeclaredType) {
                    return (DeclaredType)interfaceMirror;
                }else {
                    result = findDeclaredType(checkInterfaceEl, parentTypeEl, processingEnv);
                    if(result != null)
                        return result;
                }
            }


            checkClazzType = checkClazzEl.getSuperclass();
        }

        return null;
    }

    /**
     * Resolve a type variable
     *
     * @param variableMirror The TypeMirror that might contain a type variable e.g. T
     * @param childTypeEl the class which should implement a method with the type name resolved e.g.
     *                    the DAO class.
     * @param parentTypeEl the typed class which contains a type parameter e.g. an interface being
     *                     implemented such as BaseDao
     * @param processingEnv annotation processing environment
     *
     * @return The resolved TypeMirror as above if typeVariableMirror is a type variable, otherwise
     * return the typeVariableMirror unchanged.
     */
    protected static TypeMirror resolveDeclaredType(TypeMirror variableMirror, TypeElement childTypeEl,
                                             TypeElement parentTypeEl,
                                             ProcessingEnvironment processingEnv) {


        if(variableMirror.getKind().equals(TypeKind.DECLARED)){
            DeclaredType dt = (DeclaredType)variableMirror;
            TypeMirror[] typeMirrors = new TypeMirror[dt.getTypeArguments().size()];
            boolean declaredTypeArgumentsResolved = false;
            for(int i = 0; i < dt.getTypeArguments().size(); i++) {
                if(dt.getTypeArguments().get(i).getKind().equals(TypeKind.TYPEVAR)) {
                    declaredTypeArgumentsResolved = true;
                    typeMirrors[i] = resolveTypeVariable((TypeVariable) dt.getTypeArguments().get(i),
                            childTypeEl, parentTypeEl, processingEnv);
                }else {
                    typeMirrors[i] = dt.getTypeArguments().get(i);
                }
            }

            if(declaredTypeArgumentsResolved)
                variableMirror = processingEnv.getTypeUtils().getDeclaredType(
                    (TypeElement)processingEnv.getTypeUtils().asElement(variableMirror), typeMirrors);
        }else if(variableMirror.getKind().equals(TypeKind.ARRAY)
                && ((ArrayType)variableMirror).getComponentType().getKind().equals(TypeKind.TYPEVAR)) {
            TypeVariable arrayCompTypeVariable = (TypeVariable)((ArrayType)variableMirror)
                    .getComponentType();
            variableMirror = processingEnv.getTypeUtils().getArrayType(
                    resolveTypeVariable(arrayCompTypeVariable, childTypeEl, parentTypeEl,
                            processingEnv));
        }


        if(variableMirror.getKind().equals(TypeKind.TYPEVAR)) {
            variableMirror = resolveTypeVariable((TypeVariable)variableMirror, childTypeEl,
                    parentTypeEl, processingEnv);
        }

        return variableMirror;
    }

    protected static TypeMirror resolveTypeVariable(TypeVariable typeVariable,
                                                    TypeElement implementingClassParent,
                                                    TypeElement classWithType,
                                                    ProcessingEnvironment processingEnv) {
        DeclaredType declaredType = findDeclaredType(implementingClassParent, classWithType, processingEnv);

        if(declaredType != null) {
            List<? extends TypeParameterElement> parameterElements = classWithType.getTypeParameters();
            for(int i = 0; i < parameterElements.size(); i++) {
                if(parameterElements.get(i).getSimpleName().equals(typeVariable.asElement().getSimpleName())) {
                    return declaredType.getTypeArguments().get(i);
                }
            }
        }

        return typeVariable;
    }

    /**
     * Create a method that overrides the given method, and resolves type variables found on the
     * parameter and return types.
     *
     * @param method method to override (this may originate from childClass, or it may be any
     *               inherited abstract method from a superclass or interface
     * @param childClass the parent of the class that is to implement the method (should this be called implementerParent)
     * @param processingEnv processing environment
     *
     * @return MethodSpec.Builder with type variables resolved
     */
    public static MethodSpec.Builder overrideAndResolve(ExecutableElement method, TypeElement childClass,
                                                 ProcessingEnvironment processingEnv) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .returns(TypeName.get(
                        AbstractDbProcessor.resolveDeclaredType(method.getReturnType(), childClass,
                                (TypeElement)method.getEnclosingElement(), processingEnv)))
                .addAnnotation(Override.class);

        if(method.getModifiers().contains(Modifier.PUBLIC))
            methodBuilder.addModifiers(Modifier.PUBLIC);
        else if(method.getModifiers().contains(Modifier.PROTECTED))
            methodBuilder.addModifiers(Modifier.PROTECTED);


        for(VariableElement variableElement : method.getParameters()) {
            TypeMirror varTypeMirror = variableElement.asType();
            varTypeMirror = resolveDeclaredType(varTypeMirror, childClass,
                    (TypeElement)method.getEnclosingElement(), processingEnv);

            ParameterSpec.Builder paramSpec = ParameterSpec.builder(TypeName.get(varTypeMirror),
                    variableElement.getSimpleName().toString());
            for(AnnotationMirror mirror: variableElement.getAnnotationMirrors()) {
                paramSpec.addAnnotation(AnnotationSpec.get(mirror));
            }

            paramSpec.addModifiers(variableElement.getModifiers());
            methodBuilder.addParameter(paramSpec.build());
        }

        return methodBuilder;
    }

    protected int findAsyncParamIndex(ExecutableElement method) {
        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
        List<Element> variableTypeElements = getMethodParametersAsElements(method);
        return variableTypeElements.indexOf(umCallbackTypeElement);
    }

    /**
     * Generate the SQL for find an entity to select by primary key.
     *
     * @param daoType TypeElement representing the DAO itself
     * @param daoMethod ExecutableElement representing the method annotated as @UmFindByPrimaryKey
     * @param processingEnv annotation processing environment
     * @param identifierQuoteChar identifier quote character
     *
     * @return SQL to find the entity by it's primary key e.g. "SELECT * FROM EntityName WHERE pkFieldName = :pkFieldParamValue"
     */
    protected String generateFindByPrimaryKeySql(TypeElement daoType, ExecutableElement daoMethod,
                                                 ProcessingEnvironment processingEnv, char identifierQuoteChar){
        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeElement entityTypeEl = (TypeElement)daoMethodInfo.resolveResultAsElement();
        VariableElement primaryKeyEl = findPrimaryKey(entityTypeEl);
        if(primaryKeyEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Error generating find by primary key sql method: no primary key found " +
                    ": DAO class " + daoType.getQualifiedName() + "method: " +
                            formatMethodForErrorMessage(daoMethod));
            return "";
        }
        return "SELECT * FROM " + identifierQuoteChar + entityTypeEl.getSimpleName() +
                identifierQuoteChar + " WHERE " + identifierQuoteChar +
                primaryKeyEl.getSimpleName() + identifierQuoteChar + " = :" +
                daoMethod.getParameters().get(0).getSimpleName();
    }

    protected VariableElement findPrimaryKey(TypeElement entityType) {
        for(Element subElement : getEntityFieldElements(entityType)) {
            if(subElement.getAnnotation(UmPrimaryKey.class) != null)
                return (VariableElement)subElement;
        }

        return null;
    }

    protected String formatMethodForErrorMessage(ExecutableElement element) {
        return ((TypeElement)element.getEnclosingElement()).getQualifiedName() + "." +
                element.getSimpleName();
    }



}
