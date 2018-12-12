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
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.sync.SyncResponse;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;
import com.ustadmobile.lib.db.sync.UmRepositoryUtils;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorUtils.capitalize;
import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorUtils.findElementWithAnnotation;
import static com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils.PRODUCT_NAME_POSTGRES;
import static com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils.PRODUCT_NAME_SQLITE;

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

    protected TypeElement umCallbackTypeElement;


    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.processingEnv = processingEnvironment;
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
        umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                UmCallback.class.getName());
    }

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String destination = processingEnv.getOptions().get(getOutputDirOpt());
        if(destination == null)
            return true;

        File destinationDir = new File(destination);

        for(Element dbClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            try {
                HashMap<Integer, TypeElement> tableIdMap = new HashMap<>();
                for(TypeElement entityType : findEntityTypes((TypeElement)dbClassElement)) {
                    int tableId = entityType.getAnnotation(UmEntity.class).tableId();
                    if(tableId != 0 && tableIdMap.containsKey(tableId)) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Duplicate UmEntity " +
                                "tableId: " + tableId + " assigned to " +
                                entityType.getQualifiedName() + " and " +
                                tableIdMap.get(tableId).getQualifiedName(), entityType);
                    }

                    tableIdMap.put(tableId, entityType);
                }


                processDbClass((TypeElement)dbClassElement, destination);

                for(Element subElement : dbClassElement.getEnclosedElements()) {
                    if (subElement.getKind() != ElementKind.METHOD)
                        continue;

                    ExecutableElement dbMethod = (ExecutableElement) subElement;
                    if(!dbMethod.getModifiers().contains(Modifier.ABSTRACT))
                        continue;

                    if(dbMethod.getAnnotation(UmDbContext.class) != null
                            || dbMethod.getAnnotation(UmClearAll.class) != null
                            || dbMethod.getAnnotation(UmSyncOutgoing.class) != null)
                        continue;



                    if(!dbMethod.getReturnType().getKind().equals(TypeKind.DECLARED)) {
                        messager.printMessage(Diagnostic.Kind.ERROR,
                                dbClassElement.getSimpleName().toString() + "." +
                                        dbMethod.getSimpleName() +
                                        " abstract method must return a DAO or be annotated with " +
                                        "@UmContext, @UmClearAll, or @UmSyncOutgoing.");
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
    protected List<ExecutableElement> findMethodsToImplement(TypeElement clazz) {
        return findMethodsToImplement(clazz, clazz, new ArrayList<>());
    }


    /**
     * Method used to implement findMethodToImplement(TypeElement). This is called recursively to
     * traverse all parent classes and interfaces.
     *
     * @param clazz The class or interface to examine
     * @param implementerParentClazz the parent of the class that is going to implement the method.
     *                               This is used to resolve type variables.
     * @param methodsToImplement Methods that have already been found and identified as requiring
     *                           implementation
     * @return List of methods that require an implementation in order for the child class to be non-abstract
     */
    protected List<ExecutableElement> findMethodsToImplement(TypeElement clazz,
                                                             TypeElement implementerParentClazz,
                                                             List<ExecutableElement> methodsToImplement) {
        TypeElement searchClass = clazz;

        List<TypeMirror> interfaces = new ArrayList<>();
        while(searchClass != null) {
            for(Element subElement : searchClass.getEnclosedElements()) {
                if (!subElement.getKind().equals(ElementKind.METHOD)
                    || !subElement.getModifiers().contains(Modifier.ABSTRACT)
                    || subElement.getModifiers().contains(Modifier.STATIC))
                    continue;

                ExecutableElement method = (ExecutableElement) subElement;
                if(!isMethodImplemented(method, implementerParentClazz)
                        && !listContainsMethod(method, methodsToImplement, implementerParentClazz)) {
                    methodsToImplement.add(method);
                }
            }

            interfaces.addAll(searchClass.getInterfaces());

            searchClass = searchClass.getSuperclass().getKind().equals(TypeKind.NONE) ?
                    null : (TypeElement)processingEnv.getTypeUtils().asElement(searchClass.getSuperclass());
        }

        for(TypeMirror interfaceMirror : interfaces) {
            findMethodsToImplement(
                    (TypeElement)processingEnv.getTypeUtils().asElement(interfaceMirror),
                    implementerParentClazz, methodsToImplement);
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

    protected String generateFindLocalChangesSql(TypeElement daoType, ExecutableElement daoMethod,
                                                 ProcessingEnvironment processingEnv) {
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveResultEntityComponentType());
        Element localChangeSeqNumEl = DbProcessorUtils.findElementWithAnnotation(entityTypeEl,
                UmSyncLocalChangeSeqNum.class, processingEnv);
        if(localChangeSeqNumEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + "Attempting to generate a " +
                    "findLocalChangeSeq method: no element annotated @UmSyncLocalChangeSeqNum");
            return "";
        }

        String readPermissionCondition = daoType.getAnnotation(UmDao.class) != null ?
                                daoType.getAnnotation(UmDao.class).readPermissionCondition() : "";
        if(readPermissionCondition.equals("")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " Attempting to generate " +
                            "findLocalchangeSeq method: UmDao does not have a readPermissionCondition" +
                            " set to use for the where clause. It needs to be added");
            return "";
        }

        if(daoMethod.getParameters().size() != 2) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " Attempting to generate" +
                            "findLocalChangeSeq method: method must have exactly two long " +
                            "parameters - the starting local change sequence number and the account " +
                            "uid of the account being used for the sync");
            return "";
        }

        return String.format("SELECT * FROM %s WHERE %s >= :%s AND %s",
                entityTypeEl.getSimpleName().toString(),
                localChangeSeqNumEl.getSimpleName().toString(),
                daoMethod.getParameters().get(0).getSimpleName().toString(),
                readPermissionCondition);
    }

    protected String generateSyncFindAllChanges(TypeElement daoType, ExecutableElement daoMethod,
                                                ProcessingEnvironment processingEnv) {
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveResultEntityComponentType());
        Element localChangeSeqNumEl = DbProcessorUtils.findElementWithAnnotation(entityTypeEl,
                UmSyncLocalChangeSeqNum.class, processingEnv);
        Element masterChangeSeqNumEl = DbProcessorUtils.findElementWithAnnotation(entityTypeEl,
                UmSyncMasterChangeSeqNum.class, processingEnv);
        if(localChangeSeqNumEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + "Attempting to generate a " +
                            "findAllChanges method: no element annotated @UmSyncLocalChangeSeqNum",
                            daoType);
            return "";
        }else if(masterChangeSeqNumEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                            "generate findAllChanges method: no element annotated " +
                            "@UmSyncMasterChangeSeqNum",
                    daoType);
            return "";
        }

        String readPermissionCondition = daoType.getAnnotation(UmDao.class) != null ?
                daoType.getAnnotation(UmDao.class).readPermissionCondition() : "";
        if(readPermissionCondition.equals("")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                            "generate findAllChanges method: Dao has no readPermissionCondition",
                    daoType);
            return "";
        }

        if(daoMethod.getParameters().size() != 5) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                            "generate findAllChanges method: method must have exactly 5 parameters" +
                            " - fromLocalChangeSeqNum, toLocalChangeSeqNum, fromMasterChangeSeqNum," +
                            "toMasterChangeSeqNum, and accountPersonUid",
                    daoType);
            return "";
        }

        return String.format("SELECT * FROM %s WHERE %s BETWEEN :%s AND :%s " +
                "AND %s BETWEEN :%s AND :%s AND %s",
                entityTypeEl.getSimpleName(),
                localChangeSeqNumEl.getSimpleName(),
                daoMethod.getParameters().get(0).getSimpleName(),
                daoMethod.getParameters().get(1).getSimpleName(),
                masterChangeSeqNumEl.getSimpleName(),
                daoMethod.getParameters().get(2).getSimpleName(),
                daoMethod.getParameters().get(3).getSimpleName(),
                readPermissionCondition);

    }

    protected String generateSyncFindUpdatable(TypeElement daoType, ExecutableElement daoMethod,
                                               ProcessingEnvironment processingEnv) {
        TypeElement baseDaoTypeEl = processingEnv.getElementUtils().getTypeElement(
                BaseDao.class.getName());
        TypeMirror entityTypeMirror = daoType.asType().accept(
                new TypeVariableResolutionVisitor("T", baseDaoTypeEl), new ArrayList<>());

        if(entityTypeMirror == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                            "generate syncFindUpdateable: DAO class must extend a BaseDao with an " +
                            "entity type variable argument.");
            return "";
        }

        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(entityTypeMirror);
        Element primaryKeyEl = findPrimaryKey(entityTypeEl);
        String readPermissionCondition = daoType.getAnnotation(UmDao.class) != null ?
                daoType.getAnnotation(UmDao.class).readPermissionCondition() : "";
        if(readPermissionCondition.equals("")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                    "generate syncFindUpdateable: DAO class must have readPermissionCondition.");
            return "";
        }


        return String.format("SELECT %s.%s as primaryKey, 1 as userCanUpdate FROM %s " +
                "WHERE %s.%s in (:%s) AND %s",
                entityTypeEl.getSimpleName(),
                primaryKeyEl.getSimpleName(),
                entityTypeEl.getSimpleName(),
                entityTypeEl.getSimpleName(),
                primaryKeyEl.getSimpleName(),
                daoMethod.getParameters().get(0).getSimpleName(),
                readPermissionCondition);
    }

    protected VariableElement findPrimaryKey(TypeElement entityType) {
        for(Element subElement : DbProcessorUtils.getEntityFieldElements(entityType, processingEnv)) {
            if(subElement.getAnnotation(UmPrimaryKey.class) != null)
                return (VariableElement)subElement;
        }

        return null;
    }

    protected String formatMethodForErrorMessage(ExecutableElement element, TypeElement daoClass) {
        String msg = ((TypeElement)element.getEnclosingElement()).getQualifiedName() + "." +
                element.getSimpleName();

        if(daoClass != null)
                msg += ", being implemented for " +
                    ((daoClass != null) ? daoClass.getQualifiedName().toString() : " (unknown)");

        return msg;
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
        TypeMirror entityType = daoMethodInfo.resolveEntityParameterComponentType();
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
//                .add("long _changeSeqNum = _syncableDb.getSyncStatusDao().getMasterChangeSeqNum($L);\n",
//                    umEntityAnnotation.tableId())
//                .add("_toMasterChangeSeq = _changeSeqNum - 1;\n")
                .beginControlFlow("for($T _changed : $L)",
                        entityType, incomingChangesParamName)
//                    .add("_changed.set$L(_changeSeqNum);\n",
//                            capitalize(masterChangeSeqFieldEl.getSimpleName()))
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
            .beginControlFlow("if(_isMaster)")
                .add("_response.setSyncedUpToMasterChangeSeqNum(_syncableDb.getSyncStatusDao()" +
                        ".getMasterChangeSeqNum($L) - 1);\n", umEntityAnnotation.tableId())
            .endControlFlow()
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
        TypeMirror entityType = daoMethodInfo.resolveEntityParameterComponentType();
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                entityType);
        UmEntity umEntityAnnotation = entityTypeElement.getAnnotation(UmEntity.class);
        Element findLocalChangesMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncFindLocalChanges.class, processingEnv);
        VariableElement otherDaoParam = daoMethod.getParameters().get(0);
        VariableElement accountPersonUidParam = daoMethod.getParameters().get(1);

        CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("$1T _syncableDb = ($1T)$2L;\n", UmSyncableDatabase.class, dbName)
                .add("$T _syncStatus = _syncableDb.getSyncStatusDao().getByUid($L);\n",
                        SyncStatus.class, umEntityAnnotation.tableId())
                .add("$T<$T> _locallyChangedEntities = $L(_syncStatus.getSyncedToLocalChangeSeqNum() + 1, $L);\n",
                        List.class, entityType, findLocalChangesMethod.getSimpleName(),
                        accountPersonUidParam.getSimpleName())
                .add("$T<$T> _remoteChanges = $L.$L(_locallyChangedEntities, 0, " +
                        "_syncStatus.getSyncedToMasterChangeNum() + 1, $L);\n",
                        SyncResponse.class, entityType, otherDaoParam.getSimpleName(),
                        syncIncomingMethod.getSimpleName(),
                        accountPersonUidParam.getSimpleName())
                .beginControlFlow("if(_remoteChanges != null)")
                    //TODO: Add code to handle if any changes happened whilst this sync was ongoing
                    //TODO: e.g. before replace, check if there was any change to the local change
                    // sequence number, then bump the change numbers for these entities so they get
                    //picked up by the next sync round
                    .add("replaceList(_remoteChanges.getRemoteChangedEntities());\n")
                    .add("_syncableDb.getSyncStatusDao().updateSyncedToChangeSeqNums(" +
                                "$1L, _syncableDb.getSyncStatusDao().getLocalChangeSeqNum($1L) - 1, " +
                                "_remoteChanges.getSyncedUpToMasterChangeSeqNum());\n",
                        umEntityAnnotation.tableId())
                .endControlFlow();

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
        return methodBuilder;
    }

    /**
     * Generates a CodeBlock that contains a string starting with open brackets, and passing
     * on the same arguments of an input method in the same order.
     *
     * e.g. for the method signature void doSomething(String str1, int number) this will generate
     *   (str1, number)
     *
     * This method can also exclude particular types of parameters, which can be useful to exclude
     * callbacks. For example if UmCallback typeElement is given as an excludedElement then
     *
     * for the method void doSomething(String str1, int number, UmCallback&lt;Long&gt;) it will
     * generate (str1, number)
     *
     * @param parameters The parameters from which to generate the callback. Normally from ExecutableElement.getParameters
     * @param excludedElements Elements that should be excluded e.g. callback parameters as above
     *
     * @return CodeBlock with the generated source as above
     */
    protected CodeBlock makeNamedParameterMethodCall(List<? extends VariableElement> parameters,
                                                     Element... excludedElements) {
        List<Element> excludedElementList = Arrays.asList(excludedElements);
        CodeBlock.Builder block = CodeBlock.builder().add("(");
        List<String> paramNames = new ArrayList<>();

        for(VariableElement variable : parameters) {
            Element variableTypeElement = processingEnv.getTypeUtils().asElement(variable.asType());
            if(excludedElementList.contains(variableTypeElement))
                continue;

            paramNames.add(variable.getSimpleName().toString());
        }

        block.add(String.join(", ", paramNames));

        return block.add(")").build();
    }

    /**
     * Generate a code block that will set syncable primary keys. This is for use on entities
     * that are using the syncable primary key system.
     *
     * This will generate code along the lines of this for a single entity insert:
     * <code>
     * if(entityVarName.getPrimaryKey() == 0L) {
     *     long _baseSyncablePrimaryKey = _syncableDb.getSyncablePrimaryKeyDao().getAndIncrement(
     *          tableId, 1)
     *     entityVarName.setPrimaryKey(_baseSyncablePrimaryKey);
     * }
     * </code>
     * ... and this for an array or list insert:
     *
     * <code>
     * List&lt;EntityType&gt; _syncablePksRequired = new ArrayList&gt;&lt;();
     * for(EntityType _element : entityVarName) {
     *     if(_element.getPrimaryKey() == 0L)
     *          _syncablePksRequired.add(_element);
     * }
     * if(!_syncablePksRequired.isEmpty()) {
     *      long _baseSyncablePrimaryKey = _syncableDb.getSyncablePrimaryKeyDao().getAndIncrement(
     *          tableId, _syncablePksRequired.size());
     *      for(EntityType _element : _syncablePksRequired) {
     *          _element.setPrimaryKey(_baseSyncablePrimaryKey++);
     *      }
     * }
     * </code>
     *
     *
     *
     * @param daoMethod ExecutableElement representing the method that we are generating
     * @param daoClass The DAO class itself (used to resolve type variables etc)
     * @param processingEnv Processing Environment
     * @param dbVarName Variable name of the database class
     * @param syncableDbVarName Variable name of the database class, casted to UmSyncableDatabase (
     *                          or null if no such variable exists)
     * @return A CodeBlock that will set a syncable primary key on the entity parameter arguments
     */
    protected CodeBlock generateSetSyncablePrimaryKey(ExecutableElement daoMethod,
                                                      TypeElement daoClass,
                                                      ProcessingEnvironment processingEnv,
                                                      String dbVarName, String syncableDbVarName,
                                                      String resultVarName) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoClass, processingEnv);
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveEntityParameterComponentType());
        String paramVarName = methodInfo.getEntityParameterElement().getSimpleName().toString();
        TypeMirror resultType = methodInfo.resolveResultType();

        String resultVarNameActive = resultVarName;

        boolean isVoid = isVoid(resultType);
        if(!isVoid) {
            if(methodInfo.hasListResultType()) {
                codeBlock.add("$T $L = new $T<>();\n", resultType, resultVarName, ArrayList.class);
            }else if(methodInfo.hasArrayResultType()) {
                TypeMirror componentType = ((ArrayType)methodInfo.resolveResultType())
                        .getComponentType();
                if(componentType.getKind().isPrimitive()) {
                    componentType = processingEnv.getTypeUtils()
                            .boxedClass((PrimitiveType) componentType).asType();
                }

                codeBlock.add("$T<$T> $L_list = new $T<>();\n", List.class,
                        componentType, resultVarName, ArrayList.class);
                resultVarNameActive = resultVarName + "_list";
            }else {
                codeBlock.add("$T $L = $L;\n", resultType, resultVarName, defaultValue(resultType));
            }
        }

        int tableId = entityTypeElement.getAnnotation(UmEntity.class).tableId();
        if(syncableDbVarName == null) {
            syncableDbVarName = "_syncableDbPk";
            codeBlock.add("$1T $2L = ($1T)$3L;\n", UmSyncableDatabase.class, syncableDbVarName,
                    dbVarName);
        }

        boolean isListOrArray =methodInfo.hasEntityListParam() || methodInfo.hasEntityArrayParam();
        VariableElement pkElement = findPrimaryKey(entityTypeElement);
        String ifStmtStr = "if($L.get$L() == $L)";
        Object[] ifStmtArgs = new Object[]{isListOrArray ? "_element" : paramVarName,
                capitalize(pkElement.getSimpleName()), defaultValue(pkElement.asType())};

        if(isListOrArray) {
            String addKeyToListStr = "";
            Object[] addKeyToListArgs = new Object[0];
            if(!isVoid) {
                addKeyToListStr = "$L.add(_baseSyncablePk);\n";
                addKeyToListArgs = new Object[]{resultVarNameActive};
            }

            codeBlock.add("$T<$T> _syncablePksRequired = new $T<>();\n", List.class,
                    methodInfo.resolveEntityParameterComponentType(), ArrayList.class)
                    .beginControlFlow("for($T _element : $L)", entityTypeElement,
                            paramVarName)
                        .beginControlFlow(ifStmtStr, ifStmtArgs)
                            .add("_syncablePksRequired.add(_element);\n")
                        .endControlFlow()
                    .endControlFlow()
                    .beginControlFlow("if(!_syncablePksRequired.isEmpty())")
                        .add("long _baseSyncablePk = $L.getSyncablePrimaryKeyDao()" +
                                        ".getAndIncrement($L, _syncablePksRequired.size());\n",
                            syncableDbVarName, tableId)
                        .beginControlFlow("for($T _element : _syncablePksRequired)",
                                entityTypeElement)
                            .add(addKeyToListStr, addKeyToListArgs)
                            .add("_element.set$L(_baseSyncablePk++);\n",
                                capitalize(pkElement.getSimpleName()))
                        .endControlFlow()
                    .endControlFlow();
        }else {
            codeBlock.beginControlFlow(ifStmtStr, ifStmtArgs)
                    .add("$L.set$L($L.getSyncablePrimaryKeyDao().getAndIncrement($L, 1));\n",
                            methodInfo.getEntityParameterElement().getSimpleName(),
                            capitalize(pkElement.getSimpleName()),
                            syncableDbVarName,
                            tableId);
            if(!isVoid) {
                codeBlock.add("$L = $L.get$L();\n", resultVarNameActive, paramVarName,
                        capitalize(pkElement.getSimpleName()));
            }
            codeBlock.endControlFlow();
        }

        if(!isVoid && methodInfo.hasArrayResultType()){
            codeBlock.add("$1T $2L = $2L_list.toArray(new $3T[$2L_list.size()]);\n",
                    resultType,
                    resultVarName,
                    ((ArrayType)resultType).getComponentType());
        }

        return codeBlock.build();
    }

    /**
     * Note: When an update is run, we need to make sure the database triggers runs once, and only once.
     * Query methods that run an update query don't touch the sync sequence numbers, and the trigger
     * will when the change sequence number is unchanged after an update. With the Update method, all
     * fields are updated. If the object supplied has a different value (e.g. it came from a query,
     * and then other update methods etc. were run), then the new and old change seq num values won't
     * match, and that won't cause the trigger to run. Therefor when running an annotated update
     * method, we set all change seq numbers to zero. A second trigger condition catches this, and
     * the trigger runs appropriately.
     *
     * @param daoMethod
     * @param daoType
     * @param sycnableDbVariableName
     * @return
     */
    protected CodeBlock.Builder generateUpdateSetChangeSeqNumSection(ExecutableElement daoMethod,
                                                        TypeElement daoType,
                                                        String sycnableDbVariableName) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        codeBlock.beginControlFlow("if($L.isMaster())", sycnableDbVariableName);
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        boolean isListOrArray = methodInfo.hasArrayOrListParameter();
        List<Class<? extends Annotation>> seqNumAnnotations = Arrays.asList(
                UmSyncMasterChangeSeqNum.class, UmSyncLocalChangeSeqNum.class);
        TypeElement entityType = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveEntityParameterComponentType());
        boolean elseAdded = false;
        for(Class<? extends Annotation> annotation : seqNumAnnotations) {
            if(isListOrArray) {
                codeBlock.beginControlFlow("for($T _entity : $L)", entityType,
                        methodInfo.getEntityParameterElement().getSimpleName());
            }
            Element seqNumElement = findElementWithAnnotation(entityType, annotation,
                    processingEnv);

            if(seqNumElement != null) {
                codeBlock.add("$L.set$L(0);\n", isListOrArray ? "_entity" :
                                methodInfo.getEntityParameterElement().getSimpleName(),
                        capitalize(seqNumElement.getSimpleName()));
            }else {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod,
                        daoType) + ": generate update seq num section: cannot find element annotated " +
                        annotation.getCanonicalName(), daoType);
            }

            if(isListOrArray)
                codeBlock.endControlFlow();

            if(!elseAdded) {
                codeBlock.nextControlFlow("else");
                elseAdded = true;
            }
        }

        codeBlock.endControlFlow();

        return codeBlock;
    }

    /**
     * Get a list of all elements that could be accessible using a REST interface - this is used
     * for generation of the Jersey resource and retrofit interface
     *
     * @param daoType TypeElement representing the DAO class
     * @return List of elements that could be accessed via a REST interface
     */
    protected List<Element> findRestEnabledMethods(TypeElement daoType) {
        List<Class<? extends Annotation>> annotationList = new ArrayList<>();
        annotationList.add(UmRestAccessible.class);

        return DbProcessorUtils.findElementsWithAnnotation(daoType,
                annotationList, new ArrayList<>(), 0, processingEnv);
    }


    protected void addJaxWsParameters(ExecutableElement method, TypeElement clazzDao,
                                      MethodSpec.Builder methodBuilder,
                                      Class<? extends Annotation> queryParamAnnotation,
                                      Class<? extends Annotation> requestBodyAnnotation) {

        for(VariableElement param : method.getParameters()) {
            if(umCallbackTypeElement.equals(processingEnv.getTypeUtils().asElement(param.asType())))
                continue;

            ParameterSpec.Builder paramSpec = ParameterSpec.builder(TypeName.get(
                    DbProcessorUtils.resolveType(param.asType(), clazzDao, processingEnv)),
                    param.getSimpleName().toString());

            if(DbProcessorUtils.isQueryParam(param.asType(), processingEnv)) {
                paramSpec.addAnnotation(AnnotationSpec.builder(queryParamAnnotation)
                        .addMember("value", "$S", param.getSimpleName().toString()).build());
            }else if(requestBodyAnnotation != null) {
                paramSpec.addAnnotation(requestBodyAnnotation);
            }

            methodBuilder.addParameter(paramSpec.build());
        }
    }


    protected void addJaxWsParameters(ExecutableElement method, TypeElement clazzDao,
                                      MethodSpec.Builder methodBuilder) {
        addJaxWsParameters(method, clazzDao, methodBuilder, QueryParam.class, null);
    }

    /**
     * Add Produces, Consumes, GET/POST, etc.
     *
     * @param method
     * @param clazzDao
     * @param methodBuilder
     */
    protected void addJaxWsMethodAnnotations(ExecutableElement method, TypeElement clazzDao,
                                             MethodSpec.Builder methodBuilder) {
        int numNonQueryParams = DbProcessorUtils.getNonQueryParamCount(method, processingEnv,
                processingEnv.getElementUtils().getTypeElement(UmCallback.class.getName()));

        if(numNonQueryParams == 1) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(Consumes.class)
                    .addMember("value", "$T.APPLICATION_JSON", MediaType.class).build())
                    .addAnnotation(POST.class);
        }else if(numNonQueryParams > 1) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(method, clazzDao) + " : must not have " +
                            "more than one non-query param type for a method with JAX-RS annotations");
            return;
        }else {
            methodBuilder.addAnnotation(GET.class);
        }

        DaoMethodInfo methodInfo = new DaoMethodInfo(method, clazzDao, processingEnv);
        TypeMirror resultType = methodInfo.resolveResultEntityType();
        TypeName resultTypeName = TypeName.get(resultType);
        TypeElement stringTypeEl = processingEnv.getElementUtils().getTypeElement(
                String.class.getName());
        String producesFormat;

        if(!isVoid(resultType)) {
            if (resultTypeName.isPrimitive() || resultTypeName.isBoxedPrimitive()
                    || stringTypeEl.equals(processingEnv.getTypeUtils().asElement(resultType))) {
                producesFormat = "$T.TEXT_PLAIN";
            } else {
                producesFormat = "$T.APPLICATION_JSON";
            }

            methodBuilder.addAnnotation(AnnotationSpec.builder(Produces.class).addMember("value",
                    producesFormat, MediaType.class).build());
        }
    }

    /**
     * Generates a repository getter method.
     *
     * @param dbType TypeElement for the database class
     * @param method ExecutableElement for the method being implemented
     * @param builder MethodSpec.Builder for the given method
     * @param repositoriesListFieldName Field name of a List of UmRepositoryDb
     */
    protected void addGetRepositoryMethod(TypeElement dbType, ExecutableElement method,
                                          MethodSpec.Builder builder,
                                          String repositoriesListFieldName) {
        String baseUrlParamName = method.getParameters().get(0).getSimpleName().toString();
        String authUrlParamName = method.getParameters().get(1).getSimpleName().toString();
        builder.addCode(CodeBlock.builder()
                .add("$T _repo = $T.findRepository($L, $L, this, $L);\n",
                        UmRepositoryDb.class,
                        UmRepositoryUtils.class,
                        baseUrlParamName,
                        authUrlParamName,
                        repositoriesListFieldName)
                .beginControlFlow("if(_repo == null)")
                .add("_repo = new $L(this, $L, $L);\n", dbType.getSimpleName() +
                                DbProcessorRetrofitRepository.POSTFIX_REPOSITORY_DB,
                        baseUrlParamName, authUrlParamName)
                .add("_repositories.add(_repo);\n")
                .endControlFlow()
                .add("return ($T)_repo;\n", dbType).build());
    }

    protected MethodSpec.Builder generateDbSyncOutgoingMethod(TypeElement dbType, ExecutableElement dbMethod) {
        List<ExecutableElement> daoGettersToSync = new ArrayList<>();

        for(ExecutableElement subMethod : findMethodsToImplement(dbType)) {
            TypeMirror returnType = subMethod.getReturnType();
            if(!returnType.getKind().equals(TypeKind.DECLARED))
                continue;

            TypeElement returnTypeEl = (TypeElement)processingEnv.getTypeUtils()
                    .asElement(returnType);
            if(returnTypeEl.getAnnotation(UmDao.class) == null)
                continue;

            Element outgoingSyncMethod = findElementWithAnnotation(returnTypeEl,
                    UmSyncOutgoing.class, processingEnv);
            if(outgoingSyncMethod != null){
                daoGettersToSync.add((ExecutableElement)subMethod);
            }
        }

        MethodSpec.Builder methodBuilder = MethodSpec.overriding(dbMethod);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        String otherDbParamName = dbMethod.getParameters().get(0).getSimpleName().toString();
        String accountUidParamName = dbMethod.getParameters().get(1).getSimpleName().toString();


        for(ExecutableElement syncableDaoGetter : daoGettersToSync) {
            Element syncMethodEl = findElementWithAnnotation((TypeElement)processingEnv
                            .getTypeUtils().asElement(syncableDaoGetter.getReturnType()),
                    UmSyncOutgoing.class, processingEnv);

            codeBlock.add("$1L().$2L($3L.$1L(), $4L);\n",
                    syncableDaoGetter.getSimpleName(),
                    syncMethodEl.getSimpleName(),
                    otherDbParamName,
                    accountUidParamName);
        }
        methodBuilder.addCode(codeBlock.build());

        return methodBuilder;
    }


    /**
     * Get the TypeElements that correspond to the entities on the @UmDatabase annotation of the given
     * TypeElement
     *
     * TODO: make sure that we check each annotation, this currently **ASSUMES** the first annotation is @UmDatabase
     *
     * @param dbTypeElement TypeElement representing the class with the @UmDatabase annotation
     * @return List of TypeElement that represents the values found on entities
     */
    protected List<TypeElement> findEntityTypes(TypeElement dbTypeElement){
        List<TypeElement> entityTypeElements = new ArrayList<>();
        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationEntryMap =
                dbTypeElement.getAnnotationMirrors().get(0).getElementValues();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationEntryMap.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if (key.equals("entities")) {
                List<? extends AnnotationValue> typeMirrors =
                        (List<? extends AnnotationValue>) value;
                for(AnnotationValue entityValue : typeMirrors) {
                    entityTypeElements.add((TypeElement) processingEnv.getTypeUtils()
                            .asElement((TypeMirror) entityValue.getValue()));
                }
            }
        }

        return entityTypeElements;
    }


    /**
     * Add the required statements to the codeblock for code that will add the needed triggers
     * for change sequence numbers to be updated on insert or update of an entity.
     *
     * @param sqlProductName SQLite or Postgres as per JdbcDatabaseUtils.PRODUCT_NAME_ constants
     * @param execSqlMethod a method, accessible in this codeblock, that will run SQL e.g. 'statement.executeUpdate'
     * @param dbType TypeElement representing the database
     * @param codeBlock CodeBlock to add to
     */
    public void addCreateTriggersForEntitiesToCodeBlock(String sqlProductName,
                                                        String execSqlMethod,
                                                        TypeElement dbType,
                                                        CodeBlock.Builder codeBlock) {
        List<TypeElement> entityTypes = findEntityTypes(dbType);
        boolean isFirst = true;
        String ifStmtStr = "if(_entityClass.equals($T.class))";
        for(TypeElement entityType : entityTypes) {
            if(!DbProcessorUtils.entityHasChangeSequenceNumbers(entityType, processingEnv))
                continue;

            if(isFirst)
                codeBlock.beginControlFlow(ifStmtStr, entityType);
            else
                codeBlock.nextControlFlow("else " + ifStmtStr, entityType);


            Element localChangeSeqnumEl = DbProcessorUtils.findElementWithAnnotation(entityType,
                    UmSyncLocalChangeSeqNum.class, processingEnv);
            Element masterChangeSeqNumEl = DbProcessorUtils.findElementWithAnnotation(entityType,
                    UmSyncMasterChangeSeqNum.class, processingEnv);

            Map<String, String> triggerSqlArgs = new HashMap<>();
            triggerSqlArgs.put("tableNameLower",
                    entityType.getSimpleName().toString().toLowerCase());
            triggerSqlArgs.put("tableName", entityType.getSimpleName().toString());
            triggerSqlArgs.put("localCsnName", localChangeSeqnumEl.getSimpleName().toString());
            triggerSqlArgs.put("masterCsnName", masterChangeSeqNumEl.getSimpleName().toString());
            triggerSqlArgs.put("pkName", DbProcessorUtils.findElementWithAnnotation(entityType,
                    UmPrimaryKey.class, processingEnv).getSimpleName().toString());
            triggerSqlArgs.put("tableId", ""+entityType.getAnnotation(UmEntity.class).tableId());
            triggerSqlArgs.put("execSqlMethod", execSqlMethod);

            codeBlock.addNamed("String _tableColName_$tableName:L = isMaster() ? " +
                    "$masterCsnName:S : $localCsnName:S;\n", triggerSqlArgs);
            codeBlock.add("String _syncStatusColName_$L = isMaster() ? $S: $S;\n",
                    entityType.getSimpleName(), "masterChangeSeqNum", "localChangeSeqNum");

            Map<String, String> triggerTemplateArgs = new HashMap<>(triggerSqlArgs);
            triggerTemplateArgs.put("triggerOn", "update");
            String triggerTemplate =
                    "CREATE TRIGGER $triggerOn:L_csn_$tableNameLower:L " +
                            "AFTER $triggerOn:L ON $tableName:L FOR EACH ROW ";
            codeBlock.addNamed("String _createUpdateTriggerStmt_$tableName:L = \""
                            + triggerTemplate + " WHEN (NEW.\" + _tableColName_$tableName:L + \" = 0" + //
                            " OR OLD.\" + _tableColName_$tableName:L + \" " +
                            "= NEW.\" + _tableColName_$tableName:L  + \") \";\n",
                    triggerTemplateArgs);

            triggerTemplateArgs.put("triggerOn", "insert");
            codeBlock.addNamed("String _createInsertTriggerStmt_$tableName:L = \"" +
                            triggerTemplate + "\";\n",
                    triggerTemplateArgs);


            codeBlock.addNamed("String _triggerSql_$tableNameLower:L = \"" +
                            "UPDATE $tableName:L " +
                            "SET \" + _tableColName_$tableName:L + \" = " +
                            "(SELECT \" + _syncStatusColName_$tableName:L + \" FROM SyncStatus WHERE tableId = $tableId:L) " +
                            "WHERE $pkName:L = NEW.$pkName:L; " +
                            "UPDATE SyncStatus SET \" + " +
                            "_syncStatusColName_$tableName:L + \" = \" + _syncStatusColName_$tableName:L + \" + 1 " +
                            " WHERE tableId = $tableId:L; " +
                            "\";\n"
                    , triggerSqlArgs);
            codeBlock.addNamed("$execSqlMethod:L(\"INSERT INTO SyncStatus(tableId, " +
                        "localChangeSeqNum, masterChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                            "VALUES($tableId:L, 1, 1, 0, 0)\");\n",
                    triggerSqlArgs);

            if(sqlProductName.equals(PRODUCT_NAME_SQLITE)) {
                codeBlock.addNamed("$execSqlMethod:L(_createUpdateTriggerStmt_$tableName:L " +
                                " + \" BEGIN \" + _triggerSql_$tableNameLower:L + \" END\");\n",
                        triggerSqlArgs);
                codeBlock.addNamed("$execSqlMethod:L(_createInsertTriggerStmt_$tableName:L " +
                                " + \" BEGIN \" + _triggerSql_$tableNameLower:L + \" END\");\n",
                        triggerSqlArgs);
            }else if(sqlProductName.equals(PRODUCT_NAME_POSTGRES)) {
                codeBlock.addNamed("$execSqlMethod:L(\"CREATE OR REPLACE FUNCTION " +
                                " increment_csn_$tableNameLower:L_fn() RETURNS trigger AS $$$$ BEGIN \"" +
                                " + _triggerSql_$tableNameLower:L + \" RETURN null; END $$$$ " +
                                "LANGUAGE plpgsql\");\n",
                        triggerSqlArgs);
                codeBlock.addNamed("$execSqlMethod:L(\"CREATE TRIGGER " +
                        "increment_csn_$tableNameLower:L_trigger AFTER UPDATE OR INSERT ON " +
                        "$tableName:L FOR EACH ROW WHEN (pg_trigger_depth() = 0) " +
                        "EXECUTE PROCEDURE increment_csn_$tableNameLower:L_fn()\");\n", triggerSqlArgs);
            }


            isFirst = false;
        }

        if(!isFirst)
            codeBlock.endControlFlow();//end the if statement only if there was one

    }

}
