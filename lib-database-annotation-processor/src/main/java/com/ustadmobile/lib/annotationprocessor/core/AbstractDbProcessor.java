package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;
import com.ustadmobile.lib.db.sync.SyncResponse;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorUtils.capitalize;

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
            TypeMirror method1ResolvedType = DbProcessorUtils.resolveType(
                    method1.getParameters().get(i).asType(), implementingClass, processingEnv);
            TypeMirror method2ResolvedType = DbProcessorUtils.resolveType(
                    method2.getParameters().get(i).asType(), implementingClass, processingEnv);

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
                .returns(TypeName.get(DbProcessorUtils.resolveType(method.getReturnType(),
                        childClass, processingEnv)))
                .addAnnotation(Override.class);

        if(method.getModifiers().contains(Modifier.PUBLIC))
            methodBuilder.addModifiers(Modifier.PUBLIC);
        else if(method.getModifiers().contains(Modifier.PROTECTED))
            methodBuilder.addModifiers(Modifier.PROTECTED);


        for(VariableElement variableElement : method.getParameters()) {
            TypeMirror varTypeMirror = variableElement.asType();
            varTypeMirror = DbProcessorUtils.resolveType(varTypeMirror, childClass, processingEnv);

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

    protected String formatMethodForErrorMessage(ExecutableElement element, TypeElement daoClass) {
        return ((TypeElement)element.getEnclosingElement()).getQualifiedName() + "." +
                element.getSimpleName();
    }

    protected String formatMethodForErrorMessage(ExecutableElement element) {
        return formatMethodForErrorMessage(element, null);
    }


    /**
     * Generates a handle incoming sync method implementation
     *
     * @param daoMethod method to generate an implementation for
     * @param daoType TypeElement representing the DAO
     * @param daoBuilder TypeSpec.Builder for the DAO implementation class being generated
     * @param dbName the variable name of the database object
     *
     * @return MethodSpec.Builder object for a generated handle incoming sync method
     */
    public MethodSpec.Builder addSyncHandleIncomingMethod(ExecutableElement daoMethod,
                                                          TypeElement daoType,
                                                          TypeSpec.Builder daoBuilder,
                                                          String dbName) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        VariableElement incomingChangesParam = daoMethod.getParameters().get(0);
        VariableElement fromLocalChangeSeqNumParam = daoMethod.getParameters().get(1);
        VariableElement fromMasterChangeSeqNumParam = daoMethod.getParameters().get(2);
        VariableElement accountPersonUidParam = daoMethod.getParameters().get(3);

        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeMirror entityType = daoMethodInfo.resolveEntityParameterType();
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils()
                .asElement(entityType);
        String entityPrimaryKeyFieldName = findPrimaryKey(entityTypeElement).getSimpleName().toString();
        UmEntity umEntityAnnotation = entityTypeElement.getAnnotation(UmEntity.class);
        String incomingChangesParamName = daoMethod.getParameters().get(0).getSimpleName().toString();
        Element masterChangeSeqFieldEl = DbProcessorUtils.findElementWithAnnotation(
                entityTypeElement, UmSyncMasterChangeSeqNum.class, processingEnv);

        if(masterChangeSeqFieldEl == null){
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                    ": Method annotated @UmSyncIncoming entity type must have one field " +
                            "annotated with @UmSyncMasterChangeSeqNum", daoMethod);
            return methodBuilder;
        }

        Element localChangeSeqFieldEl = DbProcessorUtils.findElementWithAnnotation(
                entityTypeElement, UmSyncLocalChangeSeqNum.class, processingEnv);
        if(localChangeSeqFieldEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                    ": Method annotated @UmSyncIncoming entity type must have one field " +
                    "annotated with @UmSyncLocalChangeSeqNum", daoMethod);
            return methodBuilder;
        }

        Element findUpdateableEntitiesMethod = DbProcessorUtils.findElementWithAnnotation(
                daoType, UmSyncFindUpdateable.class, processingEnv);
        if(findUpdateableEntitiesMethod == null){
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                ": Method Method annotated @UmSyncIncoming requires a method annotated with @UmSyncFindUpdateable");
            return methodBuilder;
        }

        Element findChangedEntitiesMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncFindAllChanges.class, processingEnv);
        if(findChangedEntitiesMethod == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) +" method annotated " +
                            "@UmSyncIncoming requires a method anotated with @UmSincFindAllChanges");
            return methodBuilder;
        }

        codeBlock.add("$1T _syncableDb = ($1T)$2L;\n", UmSyncableDatabase.class, dbName)
                .add("$T _response = new $T<>();\n",
                ParameterizedTypeName.get(ClassName.get(SyncResponse.class),
                        TypeName.get(entityType)), ClassName.get(SyncResponse.class))
            .add("long _toMasterChangeSeq = Long.MAX_VALUE;\n")
            .add("long _toLocalChangeSeq = Long.MAX_VALUE;\n")
            .add("boolean _isMaster = _syncableDb.isMaster();\n")
            .beginControlFlow("if(_isMaster)")
                .add("long _changeSeqNum = _syncableDb.getSyncStatusDao().getAndIncrementNextMasterChangeSeqNum($L, 1);\n",
                    umEntityAnnotation.tableId())
                .add("_response.setSyncedUpToMasterChangeSeqNum(_changeSeqNum + 1);\n")
                .add("_toMasterChangeSeq = _changeSeqNum - 1;\n")
                .beginControlFlow("for($T _changed : $L)",
                        entityType, incomingChangesParamName)
                    .add("_changed.set$L(_changeSeqNum);\n",
                            capitalize(masterChangeSeqFieldEl.getSimpleName()))
                    .add("_changed.set$L(0);\n", capitalize(localChangeSeqFieldEl.getSimpleName()))
                .endControlFlow()
            .endControlFlow()
            .add("$T<$T> _updateList = new $T<>();\n", List.class, entityTypeElement,
                    ArrayList.class)
            .add("$T<$T> _insertList = new $T<>();\n", List.class, entityTypeElement,
                    ArrayList.class)
            .add("$T<$T> _primaryKeyList = new $T<>();\n", List.class, Long.class, ArrayList.class)
            .beginControlFlow("for($T _entry : $L)", entityTypeElement,
                    incomingChangesParam.getSimpleName())
                .add("_primaryKeyList.add(_entry.get$L());\n",
                        capitalize(entityPrimaryKeyFieldName))
            .endControlFlow()
            .add("$T<$T> _updateableEntities = $L(_primaryKeyList, $L);\n", List.class,
                    UmSyncExistingEntity.class, findUpdateableEntitiesMethod.getSimpleName(),
                    accountPersonUidParam.getSimpleName())
            .add("$T<$T, $T> _updateableMap = new $T<>();\n", Map.class, Long.class,
                    UmSyncExistingEntity.class, HashMap.class)
            .beginControlFlow("for($T _entity: _updateableEntities)", UmSyncExistingEntity.class)
                .add("_updateableMap.put(_entity.getPrimaryKey(), _entity);\n")
            .endControlFlow()
            .beginControlFlow("for($T _entity : $L)", entityType, incomingChangesParamName)
                .beginControlFlow("if(_updateableMap.containsKey(_entity.get$L()))",
                        capitalize(entityPrimaryKeyFieldName))
                    .beginControlFlow("if(_updateableMap.get(_entity.get$L()).isUserCanUpdate())",
                            capitalize(entityPrimaryKeyFieldName))
                        .add("_updateList.add(_entity);\n")
                    .endControlFlow()
                .nextControlFlow("else")
                    .add("_insertList.add(_entity);\n")
                .endControlFlow()
            .endControlFlow()
            .add("insertList(_insertList);\n")
            .add("updateList(_updateList);\n")
            .add("_response.setRemoteChangedEntities($L($L, $L, $L, $L, $L));\n",
                    findChangedEntitiesMethod.getSimpleName(),
                    fromLocalChangeSeqNumParam.getSimpleName(), "_toLocalChangeSeq",
                    fromMasterChangeSeqNumParam.getSimpleName(), "_toMasterChangeSeq",
                    accountPersonUidParam.getSimpleName())
            .add("return _response;\n");


        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());

        return methodBuilder;
    }


    /**
     * Generate an outgoing sync method to sync with another database.
     *
     * @param daoMethod The dao method that we are generating an implementation for
     * @param daoType The TypeElement representing the DAO
     * @param daoBuilder TypeSpec.Builder for the DAO implementation class being generated
     * @param dbName The variable name of the database object
     *
     * @return MethodSpec.Builder with a generated method to handle outgoing synchronization
     */
    public MethodSpec.Builder addSyncOutgoing(ExecutableElement daoMethod,
                                              TypeElement daoType,
                                              TypeSpec.Builder daoBuilder,
                                              String dbName) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);
        Element syncIncomingMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncIncoming.class, processingEnv);

        if(syncIncomingMethod == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " class with " +
                    "@UmSyncOutgoing method must have corresponding method annotated @UmSyncIncoming");
            return methodBuilder;
        }

        DaoMethodInfo daoMethodInfo = new DaoMethodInfo((ExecutableElement)syncIncomingMethod, daoType,
                processingEnv);
        TypeMirror entityType = daoMethodInfo.resolveEntityParameterType();
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                entityType);
        UmEntity umEntityAnnotation = entityTypeElement.getAnnotation(UmEntity.class);
        Element findLocalChangesMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncFindLocalChanges.class, processingEnv);
        VariableElement otherDaoParam = daoMethod.getParameters().get(0);
        VariableElement accountPersonUidParam = daoMethod.getParameters().get(1);

        CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("$1T _syncableDb = ($1T)$2L;\n", UmSyncableDatabase.class, dbName)
                .add("$T _syncStatus = _syncableDb.getSyncStatusDao().findByUid($L);\n",
                        SyncStatus.class, umEntityAnnotation.tableId())
                .add("$T<$T> _locallyChangedEntities = $L(_syncStatus.getSyncedToLocalChangeSeqNum(), $L);\n",
                        List.class, entityType, findLocalChangesMethod.getSimpleName(),
                        accountPersonUidParam.getSimpleName())
                .add("$T<$T> _remoteChanges = $L.$L(_locallyChangedEntities, 0, " +
                        "_syncStatus.getSyncedToMasterChangeNum(), $L);\n",
                        SyncResponse.class, entityType, otherDaoParam.getSimpleName(),
                        syncIncomingMethod.getSimpleName(),
                        accountPersonUidParam.getSimpleName())
                .add("replaceList(_remoteChanges.getRemoteChangedEntities());\n")
                .add("_syncableDb.getSyncStatusDao().updateSyncedToChangeSeqNums(" +
                        "$L, _syncStatus.getNextLocalChangeSeqNum(), " +
                        "_remoteChanges.getSyncedUpToMasterChangeSeqNum());\n",
                        umEntityAnnotation.tableId());

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
        return methodBuilder;
    }


}
