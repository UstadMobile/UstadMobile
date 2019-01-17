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
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;
import com.ustadmobile.lib.database.annotation.UmRestAuthorizedUidParam;
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanInsert;
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanUpdate;
import com.ustadmobile.lib.database.annotation.UmSyncCountLocalPendingChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncLastChangedBy;
import com.ustadmobile.lib.database.annotation.UmSyncLocalChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncMasterChangeSeqNum;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.UmDbWithAttachmentsDir;
import com.ustadmobile.lib.db.sync.SyncResponse;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;
import com.ustadmobile.lib.db.sync.UmRepositoryUtils;
import com.ustadmobile.lib.db.sync.UmSyncExistingEntity;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.dao.BaseDao;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import javax.tools.Diagnostic;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorUtils.capitalize;
import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorUtils.findElementWithAnnotation;
import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorUtils.findElementsWithAnnotation;
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
                TypeElement dbTypeEl = (TypeElement)dbClassElement;
                HashMap<Integer, TypeElement> tableIdMap = new HashMap<>();
                for(TypeElement entityType : DbProcessorUtils.findEntityTypes(
                        (TypeElement)dbClassElement, processingEnv)) {
                    if(entityType.getAnnotation(UmEntity.class) == null) {
                        messager.printMessage(Diagnostic.Kind.ERROR, "Entity " +
                                entityType.getQualifiedName() + "referenced " +
                                " by @UmDatabase annotation on " +
                                dbTypeEl.getQualifiedName() + " not annotated" +
                                " with @UmEntity ", entityType);
                        continue;
                    }

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
                            || dbMethod.getAnnotation(UmSyncOutgoing.class) != null
                            || dbMethod.getAnnotation(UmSyncCountLocalPendingChanges.class) != null)
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

    protected boolean listContainsMethod(ExecutableElement method, List<ExecutableElement> methodList,
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


        for(TypeMirror thrown : method.getThrownTypes()) {
            methodBuilder.addException(TypeName.get(thrown));
        }

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

    protected String generateFindLocalChangesSql(TypeElement daoType,
                                                 ExecutableElement daoMethod,
                                                 String selectClause,
                                                 String localCsnCondition,
                                                 String limitParamName,
                                                 String lastChangedByFieldParamName,
                                                 ProcessingEnvironment processingEnv) {
        TypeMirror entityTypeMirror = DbProcessorUtils.resolveDaoEntityType(daoType.asType(),
                processingEnv);
        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                entityTypeMirror);

        Element localChangeSeqNumEl = DbProcessorUtils.findElementWithAnnotation(entityTypeEl,
                UmSyncLocalChangeSeqNum.class, processingEnv);

        if(localChangeSeqNumEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + "Attempting to generate a " +
                    "findLocalChangeSeq method: no element annotated @UmSyncLocalChangeSeqNum");
            return "";
        }

        String updatePermissionCondition;
        String joinClause;
        TypeMirror inheritPermissionFromTypeMirror = DbProcessorUtils
                .findDaoToInheritPermissionFrom(daoType);
        String insertCondition;
        if(inheritPermissionFromTypeMirror != null) {
            joinClause = generateInheritPermissionJoinClause(inheritPermissionFromTypeMirror,
                    entityTypeEl, daoType.getAnnotation(UmDao.class));
            Element inheritPermissionFromTypeEl = processingEnv.getTypeUtils().asElement(
                    inheritPermissionFromTypeMirror);
            updatePermissionCondition = inheritPermissionFromTypeEl.getAnnotation(UmDao.class)
                    .updatePermissionCondition();
            insertCondition = inheritPermissionFromTypeEl.getAnnotation(UmDao.class)
                    .insertPermissionCondition();
        }else {
            updatePermissionCondition = daoType.getAnnotation(UmDao.class) != null ?
                    daoType.getAnnotation(UmDao.class).updatePermissionCondition() : "";
            insertCondition = daoType.getAnnotation(UmDao.class).insertPermissionCondition();
            joinClause = "";
        }

        joinClause += daoType.getAnnotation(UmDao.class).permissionJoin();


        if(updatePermissionCondition.equals("")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " Attempting to generate " +
                            "findLocalchangeSeq method: UmDao does not have a selectPermissionCondition" +
                            " set to use for the where clause. It needs to be added");
            return "";
        }

        Element lastChangedByField = findElementWithAnnotation(entityTypeEl,
                UmSyncLastChangedBy.class, processingEnv);

        if(lastChangedByField == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) +
                            " findLocalchanges method: entity " + entityTypeEl.getQualifiedName() +
                            " does not have a UmSyncLastChangedBy field",
                    daoType);
            return "";
        }

        String querySql = String.format("SELECT %s FROM %s %s " +
                        "WHERE %s AND %s = :%s " +
                        "AND ((%s) OR (%s))",
                selectClause,
                entityTypeEl.getSimpleName().toString(),
                joinClause,
                localCsnCondition,
                lastChangedByField.getSimpleName(),
                lastChangedByFieldParamName,
                insertCondition,
                updatePermissionCondition);
        if(limitParamName != null)
            querySql += " LIMIT :" + limitParamName;

        return querySql;
    }

    protected String generateFindLocalChangesSql(TypeElement daoType,
                                                 ExecutableElement daoMethod,
                                                 ProcessingEnvironment processingEnv) {

        if(daoMethod.getParameters().size() != 5) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Method "
                    + daoMethod.toString() + " has " + daoMethod.getParameters().size() +
                    " parameters. FindLocalChanges method " +
                    "must have 5 parameters: long fromLocalChangeSeqNum, long toLocalChangeSeqNum," +
                    "long accountPersonUid, int deviceId, " +
                    "int limit", daoType);
            return "";
        }

        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeElement entityCompTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveResultEntityComponentType());
        Element localChangeSeqNumEl = DbProcessorUtils.findElementWithAnnotation(entityCompTypeEl,
                UmSyncLocalChangeSeqNum.class, processingEnv);
        VariableElement fromLocalChangeSeqNumParam = daoMethod.getParameters().get(0);
        VariableElement toLocalChangeSeqNumParam = daoMethod.getParameters().get(1);
        //method param #2 is accountPersonUid, which is used by referring to :accountPersonUid in query
        VariableElement localDeviceIdParam = daoMethod.getParameters().get(3);
        VariableElement limitParam = daoMethod.getParameters().get(4);

        String selectCols = String.format("%s.*", entityCompTypeEl.getSimpleName());
        String localCsnCondition = String.format("%s BETWEEN :%s AND :%s",
            localChangeSeqNumEl.getSimpleName(),
            fromLocalChangeSeqNumParam.getSimpleName(),
            toLocalChangeSeqNumParam.getSimpleName());


        return generateFindLocalChangesSql(daoType, daoMethod, selectCols, localCsnCondition,
                limitParam.getSimpleName().toString(), localDeviceIdParam.getSimpleName().toString(),
                processingEnv);
    }

    /**
     *
     * @param daoType
     * @param daoMethod
     * @param processingEnv
     * @return
     */
    protected String generateSyncCountPendingLocalChangesSql(TypeElement daoType,
                                                             ExecutableElement daoMethod,
                                                             ProcessingEnvironment processingEnv) {
        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                DbProcessorUtils.resolveDaoEntityType(daoType.asType(), processingEnv));
        Element localCsnFieldEl = DbProcessorUtils.findElementWithAnnotation(entityTypeEl,
                UmSyncLocalChangeSeqNum.class, processingEnv);

        VariableElement lastChangedByEl = daoMethod.getParameters().get(1);
        int tableId = entityTypeEl.getAnnotation(UmEntity.class).tableId();
        String csnComparisonWhereClause = String.format(
                "%s > (SELECT syncedToLocalChangeSeqNum FROM SyncStatus WHERE tableId = %s)",
                localCsnFieldEl.getSimpleName().toString(), String.valueOf(tableId));


        return generateFindLocalChangesSql(daoType, daoMethod, "COUNT(*)",
                csnComparisonWhereClause, null,
                lastChangedByEl.getSimpleName().toString(), processingEnv);
    }

    /**
     * Generate a JOIN clause for use with inherited permissions in the form of
     *   LEFT JOIN InheritedPermissionEntity ON Entity.foreignKey = InheritedPermissionEntity.primaryKey
     *
     * @param inheritPermissionFromTypeMirror The TypeMirror representing the DAO from which
     *                                        permissions are to be inherited
     * @param entityTypeEl The type of entity for this DAO (which is inheriting permissions)
     * @param umDao The @UmDao annotation for this DAO (which is inheriting permissions)
     *
     * @return A LEFT JOIN clause using the given parameters
     */
    protected String generateInheritPermissionJoinClause(TypeMirror inheritPermissionFromTypeMirror,
                                                         TypeElement entityTypeEl,
                                                         UmDao umDao) {
        TypeMirror joinEntityTypeMirror = DbProcessorUtils.resolveDaoEntityType(
                inheritPermissionFromTypeMirror, processingEnv);
        TypeElement joinEntityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                joinEntityTypeMirror);

        return String.format("LEFT JOIN %s ON %s.%s = %s.%s ",
                joinEntityTypeEl.getSimpleName(), entityTypeEl.getSimpleName(),
                umDao.inheritPermissionForeignKey(), joinEntityTypeEl.getSimpleName(),
                umDao.inheritPermissionJoinedPrimaryKey());
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
        UmDao umDao = daoType.getAnnotation(UmDao.class);

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

        TypeMirror inheritPermissionFromTypeMirror = DbProcessorUtils.
                findDaoToInheritPermissionFrom(daoType);
        String joinClause = "";
        String selectPermissionCondition;
        if(inheritPermissionFromTypeMirror != null) {
            joinClause = generateInheritPermissionJoinClause(inheritPermissionFromTypeMirror,
                    entityTypeEl, umDao);
            selectPermissionCondition = processingEnv.getTypeUtils().asElement(
                    inheritPermissionFromTypeMirror).getAnnotation(UmDao.class)
                    .selectPermissionCondition();
        }else {
            selectPermissionCondition = daoType.getAnnotation(UmDao.class).selectPermissionCondition();
        }

        joinClause += daoType.getAnnotation(UmDao.class).permissionJoin();


        if(selectPermissionCondition.equals("")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                            "generate findAllChanges method: Dao has no selectPermissionCondition",
                    daoType);
            return "";
        }

        if(daoMethod.getParameters().size() != 7) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting to" +
                            "generate findAllChanges method: method must have exactly 7 parameters" +
                            " - fromLocalChangeSeqNum, toLocalChangeSeqNum, fromMasterChangeSeqNum," +
                            "toMasterChangeSeqNum, accountPersonUid, notLastChangedBy, and limit",
                    daoType);
            return "";
        }

        VariableElement fromLocalChangeSeqNumParam = daoMethod.getParameters().get(0);
        VariableElement toLocalChangeSeqNumParam = daoMethod.getParameters().get(1);
        VariableElement fromMasterChangeSeqNumParam = daoMethod.getParameters().get(2);
        VariableElement toMasterChangeSeqNumParam = daoMethod.getParameters().get(3);
        VariableElement notLastChangedByParam = daoMethod.getParameters().get(5);
        VariableElement limitParam = daoMethod.getParameters().get(6);

        Element lastChangedFieldElement = findElementWithAnnotation(entityTypeEl,
                UmSyncLastChangedBy.class, processingEnv);
        if(lastChangedFieldElement == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " attempting " +
                            "generate findAllChanges method: entity " +
                            entityTypeEl.getQualifiedName() + " does not have int field annotated " +
                            "UmSyncLastChangedBy");
            return "";
        }


        return String.format("SELECT %s.* FROM %s %s WHERE %s BETWEEN :%s AND :%s " +
                "AND %s BETWEEN :%s AND :%s AND %s != :%s  AND (%s) ORDER BY %s, %s LIMIT :%s",
                entityTypeEl.getSimpleName(),
                entityTypeEl.getSimpleName(),
                joinClause,
                localChangeSeqNumEl.getSimpleName(),
                fromLocalChangeSeqNumParam.getSimpleName(),
                toLocalChangeSeqNumParam.getSimpleName(),
                masterChangeSeqNumEl.getSimpleName(),
                fromMasterChangeSeqNumParam.getSimpleName(),
                toMasterChangeSeqNumParam.getSimpleName(),
                lastChangedFieldElement.getSimpleName(),
                notLastChangedByParam.getSimpleName(),
                selectPermissionCondition,
                masterChangeSeqNumEl.getSimpleName(),
                localChangeSeqNumEl.getSimpleName(),
                limitParam.getSimpleName());

    }

    protected String generateSyncFindUpdatableSql(TypeElement daoType,
                                                  ExecutableElement methodBeingGenerated,
                                                  String primaryKeyArrayParamName,
                                                  ProcessingEnvironment processingEnv) {
        TypeElement baseDaoTypeEl = processingEnv.getElementUtils().getTypeElement(
                BaseDao.class.getName());
        TypeMirror entityTypeMirror = daoType.asType().accept(
                new TypeVariableResolutionVisitor("T", baseDaoTypeEl), new ArrayList<>());

        if(entityTypeMirror == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(methodBeingGenerated, daoType) + " attempting to" +
                            "generate SyncCheckIncomingCanUpdate: DAO class must extend a BaseDao with an " +
                            "entity type variable argument.");
            return "";
        }

        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(entityTypeMirror);
        Element primaryKeyEl = findPrimaryKey(entityTypeEl);
        TypeMirror inheritFromDaoTypeMirror = DbProcessorUtils.findDaoToInheritPermissionFrom(daoType);


        String updatePermissionCondition;
        String joinClause;
        if(inheritFromDaoTypeMirror != null) {
            joinClause = generateInheritPermissionJoinClause(inheritFromDaoTypeMirror, entityTypeEl,
                    daoType.getAnnotation(UmDao.class));
            updatePermissionCondition = processingEnv.getTypeUtils()
                    .asElement(inheritFromDaoTypeMirror).getAnnotation(UmDao.class)
                    .updatePermissionCondition();
        }else {
            joinClause = "";
            updatePermissionCondition = daoType.getAnnotation(UmDao.class) != null ?
                    daoType.getAnnotation(UmDao.class).updatePermissionCondition() : "";
        }

        joinClause += daoType.getAnnotation(UmDao.class).permissionJoin();

        if(updatePermissionCondition.equals("")) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(methodBeingGenerated, daoType) + " attempting to" +
                            "generate SyncCheckIncomingCanUpdate: DAO class must have selectPermissionCondition.");
            return "";
        }


        return String.format("SELECT %s.%s as primaryKey, (%s) as userCanUpdate FROM %s %s " +
                        "WHERE %s.%s in (:%s)",
                entityTypeEl.getSimpleName(),
                primaryKeyEl.getSimpleName(),
                updatePermissionCondition,
                entityTypeEl.getSimpleName(),
                joinClause,
                entityTypeEl.getSimpleName(),
                primaryKeyEl.getSimpleName(),
                primaryKeyArrayParamName);
    }

    protected String generateSyncFindUpdatableSql(TypeElement daoType, ExecutableElement daoMethod,
                                                  ProcessingEnvironment processingEnv) {
        return generateSyncFindUpdatableSql(daoType, daoMethod,
                daoMethod.getParameters().get(0).getSimpleName().toString(),
                processingEnv);
    }

    protected String generateSyncCheckCanInsertSql(TypeElement daoType, ExecutableElement daoMethod,
                                                ProcessingEnvironment processingEnv) {

        TypeMirror inheritPermissionFromTypeMirror = DbProcessorUtils.findDaoToInheritPermissionFrom(
                daoType);
        String canInsertCondition;
        if(inheritPermissionFromTypeMirror != null) {
            canInsertCondition = processingEnv.getTypeUtils()
                    .asElement(inheritPermissionFromTypeMirror).getAnnotation(UmDao.class)
                    .insertPermissionCondition();
        }else {
            canInsertCondition = daoType.getAnnotation(UmDao.class) != null ?
                    daoType.getAnnotation(UmDao.class).insertPermissionCondition() : null;
        }

        if(canInsertCondition == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + ": Attempting to" +
                            "generate SyncCheckCanInsertSql: DAO class must have a insertPermissionCondition");
            return "";
        }

        return String.format("SELECT %s AS canInsert", canInsertCondition);
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
    public MethodSpec generateSyncIncomingMethod(ExecutableElement daoMethod,
                                                         TypeElement daoType,
                                                         TypeSpec.Builder daoBuilder,
                                                         String dbName) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);

        if(daoMethod.getParameters().size() != 6) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Method " + daoMethod.toString() + " " +
                    "annotated UmSyncIncoming must have 6 parameters " +
                    "incomingChanges List<T>, double fromLocalChangeSeqNum, " +
                    "double fromMasterChangeSeqNum, long accountPersonUid, int deviceId, " +
                            "int receiveLimit. " +
                            "Actually has : " + daoMethod.getParameters().size() + " parameters.",
                    daoType);
            return methodBuilder.build();
        }

        CodeBlock.Builder codeBlock = CodeBlock.builder();
        VariableElement incomingChangesParam = daoMethod.getParameters().get(0);
        VariableElement fromLocalChangeSeqNumParam = daoMethod.getParameters().get(1);
        VariableElement fromMasterChangeSeqNumParam = daoMethod.getParameters().get(2);
        VariableElement accountPersonUidParam = daoMethod.getParameters().get(3);
        VariableElement deviceIdParam = daoMethod.getParameters().get(4);
        VariableElement receiveLimitParam = daoMethod.getParameters().get(5);

        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeMirror entityType = daoMethodInfo.resolveEntityParameterComponentType();
        TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils()
                .asElement(entityType);
        String entityPrimaryKeyFieldName = findPrimaryKey(entityTypeElement).getSimpleName().toString();
        UmEntity umEntityAnnotation = entityTypeElement.getAnnotation(UmEntity.class);
        String incomingChangesParamName = daoMethod.getParameters().get(0).getSimpleName().toString();
        Element masterChangeSeqFieldEl = DbProcessorUtils.findElementWithAnnotation(
                entityTypeElement, UmSyncMasterChangeSeqNum.class, processingEnv);
        String checkIncomingEntitiesMethodName = "_checkIncoming";

        if(masterChangeSeqFieldEl == null){
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                    ": Method annotated @UmSyncIncoming entity type must have one field " +
                            "annotated with @UmSyncMasterChangeSeqNum", daoMethod);
            return methodBuilder.build();
        }

        Element localChangeSeqFieldEl = DbProcessorUtils.findElementWithAnnotation(
                entityTypeElement, UmSyncLocalChangeSeqNum.class, processingEnv);
        if(localChangeSeqFieldEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                    ": Method annotated @UmSyncIncoming entity type must have one field " +
                    "annotated with @UmSyncLocalChangeSeqNum", daoMethod);
            return methodBuilder.build();
        }

        Element findUpdateableEntitiesMethod = DbProcessorUtils.findElementWithAnnotation(
                daoType, UmSyncCheckIncomingCanUpdate.class, processingEnv);
        if(findUpdateableEntitiesMethod == null){
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod) +
                ": Method Method annotated @UmSyncIncoming requires a method annotated with @UmSyncCheckIncomingCanUpdate");
            return methodBuilder.build();
        }

        Element findChangedEntitiesMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncFindAllChanges.class, processingEnv);
        if(findChangedEntitiesMethod == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) +" method annotated " +
                            "@UmSyncIncoming requires a method anotated with @UmSincFindAllChanges");
            return methodBuilder.build();
        }

        codeBlock.add("$1T _syncableDb = ($1T)$2L;\n", UmSyncableDatabase.class, dbName)
                .add("$T _response = new $T<>();\n",
                ParameterizedTypeName.get(ClassName.get(SyncResponse.class),
                        TypeName.get(entityType)), ClassName.get(SyncResponse.class))
            .add("long _toMasterChangeSeq = Long.MAX_VALUE;\n")
            .add("long _toLocalChangeSeq = Long.MAX_VALUE;\n")
            .add("boolean _isMaster = _syncableDb.isMaster();\n")
            .beginControlFlow("if(_isMaster)")
            .add("_response.setCurrentMasterChangeSeqNum(_syncableDb.getSyncStatusDao()" +
                    ".getMasterChangeSeqNum($L) - 1);\n", umEntityAnnotation.tableId())
            .endControlFlow()
                .beginControlFlow("if(_isMaster)")
                .beginControlFlow("for($T _changed : $L)",
                        entityType, incomingChangesParamName)
                    .add("_changed.set$L(0);\n", capitalize(localChangeSeqFieldEl.getSimpleName()))
                .endControlFlow()
            .endControlFlow()
            .add("$T<$T> _updateList = new $T<>();\n", List.class, entityTypeElement,
                    ArrayList.class)
            .add("$T<$T> _insertList = new $T<>();\n", List.class, entityTypeElement,
                    ArrayList.class)
            .add("$T<$T, Integer> _entityActions = $L($L, $L);\n", Map.class, entityType,
                    checkIncomingEntitiesMethodName, incomingChangesParamName,
                    accountPersonUidParam.getSimpleName())
            .beginControlFlow("for($T.Entry<$T, Integer> _entityAction : _entityActions.entrySet())",
                    Map.class, entityType)
                .beginControlFlow("switch(_entityAction.getValue())")
                    .add("case $T.ACTION_UPDATE:\n", UmSyncIncoming.class)
                    .indent()
                        .add("_updateList.add(_entityAction.getKey());\n")
                        .add("break;\n")
                    .unindent()
                    .add("case $T.ACTION_INSERT:\n", UmSyncIncoming.class)
                    .indent()
                        .add("_insertList.add(_entityAction.getKey());\n")
                        .add("break;\n")
                    .unindent()
                .endControlFlow()
            .endControlFlow()
            .add("insertList(_insertList);\n")
            .add("updateList(_updateList);\n")
            .add("_response.setRemoteChangedEntities($L($L, $L, $L, $L, $L, $L, $L));\n",
                    findChangedEntitiesMethod.getSimpleName(),
                    fromLocalChangeSeqNumParam.getSimpleName(), "_toLocalChangeSeq",
                    fromMasterChangeSeqNumParam.getSimpleName(), "_toMasterChangeSeq",
                    accountPersonUidParam.getSimpleName(),
                    deviceIdParam.getSimpleName(),
                    receiveLimitParam.getSimpleName())
            .add("return _response;\n");


        methodBuilder.addCode(codeBlock.build());

        //now add the _checkIncoming method
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                methodInfo.resolveEntityParameterComponentType());
        daoBuilder.addMethod(generateCheckIncomingEntitiesMethod("_checkIncoming",
                Modifier.PUBLIC, entityTypeEl, daoType, daoMethod,
                (QueryMethodGenerator)this, daoBuilder));


        return methodBuilder.build();
    }

    /**
     * Generate a method to check incoming entities on sync. This will use the UmDao
     * updatePermissionCondition and insertPermissionCondition to determine whether the incoming
     * change should be inserted, updated, or dropped.
     *
     * @param methodName The name of the method to generate
     * @param methodModifier Access modifier to apply to the method e.g. Modifier.PUBLIC
     * @param entityType TypeElement representing the type of entity
     * @param daoType TypeElement representing the DAO currently being generated
     * @param queryMethodGenerator Query method generator (e.g. subclass of AbstractDbProcessor)
     *                             which can, if required, add extra query methods to the DAO.
     *
     * @return MethodSpec for a method that will check incoming entities+
     */
    public MethodSpec generateCheckIncomingEntitiesMethod(String methodName,
                                                          Modifier methodModifier,
                                                          TypeElement entityType,
                                                          TypeElement daoType,
                                                          ExecutableElement methodBeingGenerated,
                                                          QueryMethodGenerator queryMethodGenerator,
                                                          TypeSpec.Builder daoTypeBuilder) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        String accountPersonUidParamName = "accountPersonUid";
        String entitiesListParamName = "_entities";
        VariableElement pkElement = findPrimaryKey(entityType);
        String entityPrimaryKeyFieldName = pkElement.getSimpleName().toString();

        TypeName retTypeName = ParameterizedTypeName.get(ClassName.get(Map.class),
                ClassName.get(entityType), ClassName.get(Integer.class));
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addParameter(ParameterizedTypeName.get(ClassName.get(List.class),
                        ClassName.get(entityType)), entitiesListParamName)
                .addParameter(TypeName.LONG, accountPersonUidParamName)
                .addModifiers(methodModifier)
                .returns(retTypeName);

        String findUpdatableMethodName = null;
        Element findUpdateableMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncCheckIncomingCanUpdate.class, processingEnv);
        Element findCanInsertElementMethod = DbProcessorUtils.findElementWithAnnotation(daoType,
                UmSyncCheckIncomingCanInsert.class, processingEnv);


        if(findUpdateableMethod != null) {
            findUpdatableMethodName = findUpdateableMethod.getSimpleName().toString();
        }else {
            //TODO: if the method does not exist, generate it. Requires refactor of DaoMethodInfo
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(
                    methodBeingGenerated, daoType) + ": " + " cannot find " +
                    "UmSyncCheckingIncomingCanUpdate method");
            return methodBuilder.build();
        }

        if(findCanInsertElementMethod == null) {
            //TODO: if the method does not exist, generate it. Requires refactor of DaoMethodInfo
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(
                    methodBeingGenerated, daoType) + ": cannot find UmSyncCheckIncomingCanInsert",
                    daoType);
            return methodBuilder.build();
        }

        codeBlock
                .add("$T<$T, $T> _result = new $T<>();\n",
                        Map.class, entityType, Integer.class, HashMap.class)
                .add("$T<$T, $T> _pkToEntityMap = new $T<>();\n",
                    Map.class, DbProcessorUtils.boxIfPrimitive(pkElement.asType(), processingEnv),
                    entityType, HashMap.class)
                .beginControlFlow("for($T _entry : $L)", entityType,
                        entitiesListParamName)
                    .add("_pkToEntityMap.put(_entry.get$L(), _entry);\n",
                            capitalize(entityPrimaryKeyFieldName))
                .endControlFlow()
                .add("$T<$T> _primaryKeyList = new $T<>(_pkToEntityMap.size());\n",
                        List.class, Long.class, ArrayList.class)
                .add("_primaryKeyList.addAll(_pkToEntityMap.keySet());\n")
                .add("$T<$T> _updatableEntities = $L(_primaryKeyList, $L);\n", List.class,
                        UmSyncExistingEntity.class, findUpdatableMethodName,
                        accountPersonUidParamName)
                .beginControlFlow("for($T _existingEntity: _updatableEntities)",
                        UmSyncExistingEntity.class)
                    .add("_result.put(_pkToEntityMap.get(_existingEntity.getPrimaryKey()), " +
                            "_existingEntity.isUserCanUpdate() ? " +
                            "$1T.ACTION_UPDATE : $1T.ACTION_REJECT);\n", UmSyncIncoming.class)
                    .add("_pkToEntityMap.remove(_existingEntity.getPrimaryKey());\n")
                .endControlFlow()
                .add("boolean _userCanInsert = $L($L);\n",
                        findCanInsertElementMethod.getSimpleName(), accountPersonUidParamName)
                .beginControlFlow("for($T _entity : _pkToEntityMap.values())", entityType)
                    .add("_result.put(_entity, _userCanInsert ? " +
                                "$1T.ACTION_INSERT : $1T.ACTION_REJECT);\n", UmSyncIncoming.class)
                .endControlFlow()
                .add("return _result;\n");
        methodBuilder.addCode(codeBlock.build());

        return methodBuilder.build();
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
        boolean daoHasAttachments = daoType.getAnnotation(UmDao.class).hasAttachment();

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

        if(daoMethod.getParameters().size() != 4) {
            messager.printMessage(Diagnostic.Kind.ERROR, "UmSyncOutgoing must have 4" +
                    "parameters: D otherDao, long accountPersonUid, int sendLimit, int receiveLimit");
            return methodBuilder;
        }

        VariableElement otherDaoParam = daoMethod.getParameters().get(0);
        VariableElement accountPersonUidParam = daoMethod.getParameters().get(1);
        VariableElement sendLimitParam = daoMethod.getParameters().get(2);
        VariableElement receiveLimitParam = daoMethod.getParameters().get(3);
        VariableElement entityPrimaryKeyEl = findPrimaryKey(entityTypeElement);

        Element localChangeSeqNumEl = findElementWithAnnotation(entityTypeElement,
                UmSyncLocalChangeSeqNum.class, processingEnv);

        Element masterChangeNumFieldEl = findElementWithAnnotation(entityTypeElement,
                UmSyncMasterChangeSeqNum.class, processingEnv);
        if(localChangeSeqNumEl == null || masterChangeNumFieldEl == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) +
                        " method is annotated with UmSyncOutgoing but entity" +
                        entityTypeElement.getQualifiedName() +  " is missing local/master change" +
                            "sequence numbers", daoType);
            return methodBuilder;
        }


        String masterChangeSeqNumFieldName = masterChangeNumFieldEl.getSimpleName().toString();
        String localChangeSeqNumFieldName = localChangeSeqNumEl.getSimpleName().toString();


        CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("$1T _syncableDb = ($1T)$2L;\n", UmSyncableDatabase.class, dbName)
                .add("$T _initialSyncStatus = _syncableDb.getSyncStatusDao().getByUid($L);\n",
                        SyncStatus.class, umEntityAnnotation.tableId())
                .add("boolean _syncComplete = false;\n")
                .add("int _retryCount = 0;\n")
                .add("long _syncCompleteMasterChangeSeqNum = -1L;\n");

        ExecutableElement uploadAttachmentMethod = null;
        ExecutableElement downloadAttachmentMethod = null;

        if(daoHasAttachments) {
            codeBlock.add("//DAO has attachments: must do one at a time\n")
                    .add("$L = 1;\n", sendLimitParam.getSimpleName())
                    .add("$L = 1;\n", receiveLimitParam.getSimpleName());

            uploadAttachmentMethod =  findRepoDelegatedToWebServiceMethod(daoType,
                    UmDbSetAttachment.class);
            downloadAttachmentMethod = findRepoDelegatedToWebServiceMethod(daoType,
                    UmDbGetAttachment.class);

            if(uploadAttachmentMethod == null || downloadAttachmentMethod == null) {
                messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(daoMethod,
                        daoType) + ": DAO is marked as having attachments, but does not have set/get " +
                        "attachment methods annotated as delegate to webservice (required for sync" +
                        "to upload and download attachments)");
            }
        }
        codeBlock.beginControlFlow("do");
        if(daoHasAttachments) {
            codeBlock.beginControlFlow("try");
        }

        codeBlock.add("$T _attemptSyncStatus = _syncableDb.getSyncStatusDao().getByUid($L);\n",
                            SyncStatus.class, umEntityAnnotation.tableId())
                    .add("$T<$T> _locallyChangedEntities = $L(" +
                                    "_attemptSyncStatus.getSyncedToLocalChangeSeqNum() + 1, " +
                                    "_initialSyncStatus.getLocalChangeSeqNum() - 1, " +
                                    "$L, _syncableDb.getDeviceBits(), $L);\n",
                        List.class, entityType, findLocalChangesMethod.getSimpleName(),
                        accountPersonUidParam.getSimpleName(),
                        sendLimitParam.getSimpleName())
                    .add("long _syncedToLocalSeqNum = _locallyChangedEntities.isEmpty() ? " +
                        "_attemptSyncStatus.getSyncedToLocalChangeSeqNum() : " +
                        "_locallyChangedEntities.get(_locallyChangedEntities.size()-1)" +
                        ".get$L();\n", capitalize(localChangeSeqNumFieldName));
        if(daoHasAttachments) {
            codeBlock.beginControlFlow("if(!_locallyChangedEntities.isEmpty())")
                    .add("$T _changedPk = _locallyChangedEntities.get(0).get$L();\n",
                            entityPrimaryKeyEl.asType(),
                            capitalize(entityPrimaryKeyEl.getSimpleName()))
                    .add("$L.$L(_changedPk, $L(_changedPk));\n", otherDaoParam.getSimpleName(),
                            uploadAttachmentMethod.getSimpleName(),
                            downloadAttachmentMethod.getSimpleName())
                    .add("$L = 0;\n", receiveLimitParam.getSimpleName())
                    .endControlFlow();
        }

        codeBlock.add("$T<$T> _remoteChanges = $L.$L(_locallyChangedEntities, 0, " +
                        "_attemptSyncStatus.getSyncedToMasterChangeNum() + 1, $L, " +
                                    "_syncableDb.getDeviceBits(), $L);\n",
                        SyncResponse.class, entityType, otherDaoParam.getSimpleName(),
                        syncIncomingMethod.getSimpleName(),
                        accountPersonUidParam.getSimpleName(),
                        receiveLimitParam.getSimpleName())
                    .beginControlFlow("if(_remoteChanges != null)")
                        .add("$T<$T> _remoteChangedEntities = _remoteChanges" +
                                ".getRemoteChangedEntities();\n", List.class,
                                entityTypeElement)
                        .add("long _syncedToMasterSeqNum = _remoteChangedEntities.isEmpty() ? " +
                            "_attemptSyncStatus.getSyncedToMasterChangeNum() : " +
                            "_remoteChangedEntities.get(_remoteChangedEntities.size()-1)" +
                            ".get$L();\n", capitalize(masterChangeSeqNumFieldName))
                        .beginControlFlow("if(_syncCompleteMasterChangeSeqNum == -1)")
                            .add("_syncCompleteMasterChangeSeqNum  = _remoteChanges." +
                                    "getCurrentMasterChangeSeqNum();\n")
                        .endControlFlow();

        if(daoHasAttachments) {
            codeBlock.beginControlFlow("if(!_remoteChangedEntities.isEmpty())")
                    .add("$T _attachmentToDownloadId = _remoteChangedEntities.get(0).get$L();\n",
                            entityPrimaryKeyEl.asType(), capitalize(entityPrimaryKeyEl.getSimpleName()))
                    .add("$L(_attachmentToDownloadId, $L.$L(_attachmentToDownloadId));\n",
                            uploadAttachmentMethod.getSimpleName(), otherDaoParam.getSimpleName(),
                            downloadAttachmentMethod.getSimpleName())
                    .endControlFlow();
        }

        codeBlock.add("replaceList(_remoteChanges.getRemoteChangedEntities());\n")
                        .add("_syncableDb.getSyncStatusDao().updateSyncedToChangeSeqNums(" +
                                "$1L, _syncedToLocalSeqNum, _syncedToMasterSeqNum);\n",
                                umEntityAnnotation.tableId())
                        .add("_syncComplete = (_syncedToMasterSeqNum >= _syncCompleteMasterChangeSeqNum || _remoteChanges.getRemoteChangedEntities().isEmpty()) " +
                                " && (_syncedToLocalSeqNum >= _initialSyncStatus.getLocalChangeSeqNum() - 1 || _locallyChangedEntities.isEmpty());\n")
                    .nextControlFlow("else")
                        .add("_retryCount++;\n")
                    .endControlFlow();
        if(daoHasAttachments) {
            codeBlock.nextControlFlow("catch(Exception e)")
                    .add("_retryCount++;\n")
                    .add("e.printStackTrace();\n")
                    .endControlFlow();
        }

        codeBlock.endControlFlow("while(!_syncComplete && _retryCount < 3)");

        methodBuilder.addCode(codeBlock.build());
        daoBuilder.addMethod(methodBuilder.build());
        return methodBuilder;
    }

    private ExecutableElement findRepoDelegatedToWebServiceMethod(TypeElement daoType,
                                                        Class<? extends Annotation> annotation) {
        List<Element> candidates = findElementsWithAnnotation(daoType, annotation,
                new ArrayList<>(), 0, processingEnv);
        for(Element candidate : candidates) {
            if(candidate.getAnnotation(UmRepository.class) != null
                    && candidate.getAnnotation(UmRepository.class).delegateType()
                    == UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE) {
                return (ExecutableElement)candidate;
            }
        }

        return null;
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
     * @param parameters The parameters from which to generate the callback. Normally from
     *                   ExecutableElement.getParameters
     * @param substitutions if not null, the map will be used to substitute variable names
     * @param excludedElements Elements that should be excluded e.g. callback parameters as above
     *
     * @return CodeBlock with the generated source as above
     */
    protected CodeBlock makeNamedParameterMethodCall(List<? extends VariableElement> parameters,
                                                     Map<String, String> substitutions,
                                                     Element... excludedElements) {
        List<Element> excludedElementList = Arrays.asList(excludedElements);
        CodeBlock.Builder block = CodeBlock.builder().add("(");
        List<String> paramNames = new ArrayList<>();

        for(VariableElement variable : parameters) {
            Element variableTypeElement = processingEnv.getTypeUtils().asElement(variable.asType());
            if(excludedElementList.contains(variableTypeElement))
                continue;

            String variableName = variable.getSimpleName().toString();
            if(substitutions != null && substitutions.containsKey(variableName))
                variableName = substitutions.get(variableName);

            paramNames.add(variableName);
        }

        block.add(String.join(", ", paramNames));

        return block.add(")").build();
    }

    protected CodeBlock makeNamedParameterMethodCall(List<? extends VariableElement> parameters,
                                                     Element... excludedElements) {
        return makeNamedParameterMethodCall(parameters, null, excludedElements);
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

    protected CodeBlock generateSetLastChangedBy(ExecutableElement daoMethod,
                                                 TypeElement daoType,
                                                 String syncableDbVariableName) {
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeMirror entityTypeMirror = methodInfo.resolveEntityParameterComponentType();
        TypeElement entityTypeEl = (TypeElement)processingEnv.getTypeUtils()
                .asElement(entityTypeMirror);
        boolean isListOrArray = methodInfo.hasArrayOrListParameter();


        codeBlock.add("int _lastChangedBy = $L.getDeviceBits();\n", syncableDbVariableName);
        if(isListOrArray) {
            codeBlock.beginControlFlow("for($T _entity : $L)", entityTypeMirror,
                    methodInfo.getEntityParameterElement().getSimpleName());
        }

        Element lastChangedByField = findElementWithAnnotation(entityTypeEl,
                UmSyncLastChangedBy.class, processingEnv);
        if(lastChangedByField == null) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) +
                            "GenerateUpdateLastChangedBy entity " +
                            entityTypeEl.getQualifiedName() + " does not have a field " +
                            "annotated with UmSyncLastChangedBy", daoType);
            return CodeBlock.builder().build();
        }

        codeBlock.add("$L.set$L(_lastChangedBy);\n",
                isListOrArray ? "_entity" : methodInfo.getEntityParameterElement().getSimpleName(),
                capitalize(lastChangedByField.getSimpleName()));

        if(isListOrArray){
            codeBlock.endControlFlow();
        }

        return codeBlock.build();
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


    /**
     * Add web service parameter annotation to a given methodBuilder, to match a given dao method.
     * This is used for generation of Jersey Resource methods and Retrofit interface methods.
     *
     * @param method The DAO method we want to generate a webservice method signature for
     * @param daoType DAO type the method belongs to (used for type variable resolution etc)
     * @param methodBuilder JavaPoet MethodBuilder to add parameters to
     * @param queryParamAnnotation Annotation to add for a parameter that can be passed as query
     *                             parameters (primitives and list/arrays of primitives)
     * @param requestBodyAnnotation Annotation to add for a parameter that should be the request body
     *                              (if any). This is used for JSON objects
     * @param formDataAnnotation Annotation to add for multipart form upload data. This is used for
     *                           binary attachment data.
     * @param typeSubstitutions Substitute parameters (from value typemirror to key typemirror).
     * @param addAuthHeaderParamToMethod if true, if we find a parameter annotated
     *                                   UmRestAuthorizedUidParam, then an additional parameter will
     *                                   be added to the end to get the auth token.
     */
    protected void addJaxWsParameters(ExecutableElement method, TypeElement daoType,
                                      MethodSpec.Builder methodBuilder,
                                      Class<? extends Annotation> queryParamAnnotation,
                                      Class<? extends Annotation> requestBodyAnnotation,
                                      Class<? extends Annotation> formDataAnnotation,
                                      Map<TypeMirror, TypeMirror> typeSubstitutions,
                                      boolean addAuthHeaderParamToMethod) {

        DaoMethodInfo methodInfo = new DaoMethodInfo(method, daoType, processingEnv);
        TypeMirror inputStreamTypeEl = processingEnv.getElementUtils().getTypeElement(
                InputStream.class.getName()).asType();
        for(VariableElement param : method.getParameters()) {
            if(umCallbackTypeElement.equals(processingEnv.getTypeUtils().asElement(param.asType())))
                continue;

            TypeMirror paramTypeMirror = DbProcessorUtils.resolveType(param.asType(), daoType,
                    processingEnv);

            if(typeSubstitutions != null && typeSubstitutions.containsKey(paramTypeMirror)) {
                paramTypeMirror = typeSubstitutions.get(paramTypeMirror);
            }

            ParameterSpec.Builder paramSpec = ParameterSpec.builder(TypeName.get(
                    paramTypeMirror),
                    param.getSimpleName().toString());

            if(DbProcessorUtils.isQueryParam(param.asType(), processingEnv)) {
                paramSpec.addAnnotation(AnnotationSpec.builder(queryParamAnnotation)
                        .addMember("value", "$S", param.getSimpleName().toString()).build());
            }else if(param.asType().equals(inputStreamTypeEl)) {
                paramSpec.addAnnotation(AnnotationSpec.builder(formDataAnnotation)
                        .addMember("value", "$S", param.getSimpleName()).build());
            }else if(requestBodyAnnotation != null) {
                paramSpec.addAnnotation(requestBodyAnnotation);
            }

            methodBuilder.addParameter(paramSpec.build());
        }

        VariableElement uidParam = methodInfo.getAuthorizedUidParam();
        if(addAuthHeaderParamToMethod && uidParam != null) {
            methodBuilder.addParameter(ParameterSpec.builder(String.class,
                    "_authHeader")
                    .addAnnotation(AnnotationSpec.builder(HeaderParam.class)
                            .addMember("value", "$S", uidParam
                                .getAnnotation(UmRestAuthorizedUidParam.class).headerName()).build())
                    .build());
        }
    }

    protected void addJaxWsParameters(ExecutableElement method, TypeElement daoType,
                                      MethodSpec.Builder methodBuilder,
                                      Class<? extends Annotation> queryParamAnnotation,
                                      Class<? extends Annotation> requestBodyAnnotation,
                                      Class<? extends Annotation> formDataAnnotation,
                                      boolean addAuthHeaderParamToMethod) {
        addJaxWsParameters(method, daoType, methodBuilder, queryParamAnnotation,
                requestBodyAnnotation, formDataAnnotation, null,
                addAuthHeaderParamToMethod);
    }

    /**
     * Add web service parameter annotation to a given methodBuilder, to match a given dao method.
     * This is used for generation of Jersey Resource methods and Retrofit interface methods.
     *
     * Synonamous to addJaxWsParameters(method, daoType, methodBuilder, queryParamAnnotation,
     *  requestBodyAnnotation, false)
     *
     * @param method The DAO method we want to generate a webservice method signature for
     * @param daoType DAO type the method belongs to (used for type variable resolution etc)
     * @param methodBuilder JavaPoet MethodBuilder to add parameters to
     * @param queryParamAnnotation Annotation to add for a parameter that can be passed as query
     *                             parameters (primitives and list/arrays of primitives)
     * @param requestBodyAnnotation Annotation to add for a parameter that should be the request body
     *                              (if any).
     */
    protected void addJaxWsParameters(ExecutableElement method, TypeElement daoType,
                                      MethodSpec.Builder methodBuilder,
                                      Class<? extends Annotation> queryParamAnnotation,
                                      Class<? extends Annotation> requestBodyAnnotation,
                                      Class<? extends Annotation> formDataAnnotation) {
        addJaxWsParameters(method, daoType, methodBuilder, queryParamAnnotation,
                requestBodyAnnotation,formDataAnnotation, null,
                false);
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

        boolean isFormDataUpload = DbProcessorUtils.isFormDataUpload(method, processingEnv);


        if(isFormDataUpload) {
            methodBuilder.addAnnotation(AnnotationSpec.builder(Consumes.class)
                .addMember("value", "$T.MULTIPART_FORM_DATA", MediaType.class).build())
                .addAnnotation(POST.class);
        }else if(numNonQueryParams == 1) {
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

        if(!isVoid(resultType) && !resultTypeName.equals(ClassName.get(InputStream.class))) {
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


    protected List<ExecutableElement> findDaoGetterMethods(TypeElement dbType){
        List<ExecutableElement> daoGetterMethods = new ArrayList<>();
        for(ExecutableElement subMethod : findMethodsToImplement(dbType)) {
            TypeMirror returnType = subMethod.getReturnType();
            if(!returnType.getKind().equals(TypeKind.DECLARED))
                continue;

            TypeElement returnTypeEl = (TypeElement)processingEnv.getTypeUtils()
                    .asElement(returnType);
            if(returnTypeEl.getAnnotation(UmDao.class) == null)
                continue;

            daoGetterMethods.add(subMethod);
        }

        return daoGetterMethods;
    }

    protected List<ExecutableElement> findSyncableDaoGetterMethods(TypeElement dbType) {
        List<ExecutableElement> daoGettersToSync = new ArrayList<>();

        for(ExecutableElement subMethod: findDaoGetterMethods(dbType)) {
            Element outgoingSyncMethod = findElementWithAnnotation((TypeElement)processingEnv
                            .getTypeUtils().asElement(subMethod.getReturnType()),
                    UmSyncOutgoing.class, processingEnv);
            if(outgoingSyncMethod != null){
                daoGettersToSync.add(subMethod);
            }
        }

        return daoGettersToSync;
    }


    protected MethodSpec generateDbSyncCountLocalPendingChangesMethod(TypeElement dbType,
                                                                      ExecutableElement dbMethod) {
        List<ExecutableElement> syncableDaoGetters = findSyncableDaoGetterMethods(dbType);
        MethodSpec.Builder methodBuilder = overrideAndResolve(dbMethod, dbType, processingEnv);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if(dbMethod.getParameters().size() != 2) {
            messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(dbMethod, dbType) +
                    " @UmSyncCountLocalPendingChanges must have two arguments: the accountUid and the deviceId");
            return methodBuilder.build();
        }

        VariableElement accountUidParamEl = dbMethod.getParameters().get(0);
        VariableElement deviceIdParamEl = dbMethod.getParameters().get(1);

        boolean firstEl = true;
        for(ExecutableElement daoGetter : syncableDaoGetters) {
            TypeElement daoTypeEl = (TypeElement)processingEnv.getTypeUtils()
                    .asElement(daoGetter.getReturnType());
            Element countPendingChangesEl = DbProcessorUtils.findElementWithAnnotation(
                    daoTypeEl, UmSyncCountLocalPendingChanges.class, processingEnv);
            if(countPendingChangesEl == null)
                continue;

            if(firstEl) {
                codeBlock.add("int _pendingChangeCount = ");
            }else {
                codeBlock.add("_pendingChangeCount += ");
            }

            codeBlock.add("$L().$L($L, $L);\n",
                    daoGetter.getSimpleName(), countPendingChangesEl.getSimpleName(),
                    accountUidParamEl.getSimpleName(), deviceIdParamEl.getSimpleName());

            firstEl = false;
        }

        codeBlock.add("return _pendingChangeCount;\n");

        return methodBuilder.addCode(codeBlock.build()).build();
    }

    protected MethodSpec.Builder generateDbSyncOutgoingMethod(TypeElement dbType, ExecutableElement dbMethod) {
        List<ExecutableElement> daoGettersToSync = findSyncableDaoGetterMethods(dbType);

        MethodSpec.Builder methodBuilder = MethodSpec.overriding(dbMethod);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        String otherDbParamName = dbMethod.getParameters().get(0).getSimpleName().toString();
        String accountUidParamName = dbMethod.getParameters().get(1).getSimpleName().toString();

        String sendLimitParamName = dbMethod.getParameters().get(2).getSimpleName().toString();
        String receiveLimitParamName = dbMethod.getParameters().get(3).getSimpleName().toString();

        for(ExecutableElement syncableDaoGetter : daoGettersToSync) {
            Element syncMethodEl = findElementWithAnnotation((TypeElement)processingEnv
                            .getTypeUtils().asElement(syncableDaoGetter.getReturnType()),
                    UmSyncOutgoing.class, processingEnv);

            codeBlock.add("$1L().$2L($3L.$1L(), $4L, $5L, $6L);\n",
                    syncableDaoGetter.getSimpleName(),
                    syncMethodEl.getSimpleName(),
                    otherDbParamName,
                    accountUidParamName,
                    sendLimitParamName,
                    receiveLimitParamName);
        }
        methodBuilder.addCode(codeBlock.build());

        return methodBuilder;
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
        List<TypeElement> entityTypes = DbProcessorUtils.findEntityTypes(dbType, processingEnv);
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
            Element lastChangeByEl = DbProcessorUtils.findElementWithAnnotation(entityType,
                    UmSyncLastChangedBy.class, processingEnv);

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
            triggerSqlArgs.put("lastChangeFieldName", lastChangeByEl.getSimpleName().toString());

            codeBlock.addNamed("String _tableColName_$tableName:L = isMaster() ? " +
                    "$masterCsnName:S : $localCsnName:S;\n", triggerSqlArgs);
            triggerSqlArgs.put("concatTableCsnCol", "\" + _tableColName_" +
                    entityType.getSimpleName() + " + \"");

            codeBlock.add("String _syncStatusColName_$L = isMaster() ? $S: $S;\n",
                    entityType.getSimpleName(), "masterChangeSeqNum", "localChangeSeqNum");
            triggerSqlArgs.put("concatSyncStatusColName", "\" + _syncStatusColName_" +
                    entityType.getSimpleName() + " + \"");

            Map<String, String> triggerTemplateArgs = new HashMap<>(triggerSqlArgs);
            triggerTemplateArgs.put("triggerOn", "update");

            codeBlock.addNamed("$execSqlMethod:L(\"INSERT INTO SyncStatus(tableId, " +
                        "localChangeSeqNum, masterChangeSeqNum, syncedToMasterChangeNum, syncedToLocalChangeSeqNum) " +
                            "VALUES($tableId:L, 1, 1, 0, 0)\");\n",
                    triggerSqlArgs);

            if(sqlProductName.equals(PRODUCT_NAME_SQLITE)) {
                codeBlock.addNamed("$execSqlMethod:L(\"CREATE TRIGGER update_csn_$tableNameLower:L " +
                            "AFTER update ON $tableName:L FOR EACH ROW WHEN " +
                            "(NEW.$concatTableCsnCol:L = 0 " +
                                "OR OLD.$concatTableCsnCol:L = NEW.$concatTableCsnCol:L) " +
                            "BEGIN " +
                                "UPDATE $tableName:L SET $concatTableCsnCol:L = " +
                                    "(SELECT $concatSyncStatusColName:L FROM SyncStatus WHERE tableId = $tableId:L) " +
                                    "WHERE $pkName:L = NEW.$pkName:L; " +
                                "UPDATE SyncStatus SET " +
                                    "$concatSyncStatusColName:L = $concatSyncStatusColName:L + 1 " +
                                "WHERE tableId = $tableId:L; " +
                            "END\");\n", triggerSqlArgs)
                        .addNamed("$execSqlMethod:L(\"CREATE TRIGGER insert_csn_$tableNameLower:L " +
                            "AFTER insert ON $tableName:L FOR EACH ROW WHEN " +
                            "(NEW.$concatTableCsnCol:L = 0) " +
                            "BEGIN " +
                                "UPDATE $tableName:L SET $concatTableCsnCol:L = " +
                                    "(SELECT $concatSyncStatusColName:L FROM SyncStatus WHERE tableId = $tableId:L) " +
                                "WHERE $pkName:L = NEW.$pkName:L;" +
                                "UPDATE SyncStatus SET " +
                                    "$concatSyncStatusColName:L = $concatSyncStatusColName:L + 1 " +
                                "WHERE tableId = $tableId:L; " +
                            "END\");\n", triggerSqlArgs);

            }else if(sqlProductName.equals(PRODUCT_NAME_POSTGRES)) {
                codeBlock.addNamed("$execSqlMethod:L(\"CREATE OR REPLACE FUNCTION " +
                            "increment_csn_$tableNameLower:L_fn() RETURNS trigger AS $$$$ " +
                            "BEGIN " +
                                "UPDATE $tableName:L SET $concatTableCsnCol:L = " +
                                    "(SELECT $concatSyncStatusColName:L FROM SyncStatus WHERE tableId = $tableId:L) " +
                                    "WHERE $pkName:L = NEW.$pkName:L; " +
                                "UPDATE SyncStatus SET " +
                                    "$concatSyncStatusColName:L = $concatSyncStatusColName:L + 1  " +
                                    "WHERE tableId = $tableId:L; " +
                                "RETURN null; " +
                            "END $$$$" +
                            "LANGUAGE plpgsql\");\n", triggerSqlArgs)
                        .addNamed("$execSqlMethod:L(\"CREATE TRIGGER " +
                            "increment_csn_$tableNameLower:L_trigger AFTER UPDATE OR INSERT ON " +
                            "$tableName:L FOR EACH ROW WHEN (pg_trigger_depth() = 0) " +
                            "EXECUTE PROCEDURE increment_csn_$tableNameLower:L_fn()\");\n", triggerSqlArgs);
            }

            isFirst = false;
        }

        if(!isFirst)
            codeBlock.endControlFlow();//end the if statement only if there was one

    }

    /**
     * Modify the given SQL UPDATE statement to set the lastModifiedBy field.
     * e.g. this will change
     *
     * UPDATE EntityTableName SET title = :title where x &gt; :numParam
     *
     * to:
     *
     * UPDATE EntityTableName SET lastModifiedBy = :_lastModifiedBy, title = :title where x &gt; :numParam
     *
     *
     * @param sql SQL string to modify
     * @param methodEl the DAO method that this SQL is being used for
     * @param daoType DAO TypeElement
     * @param dbType Database TypeElement (used to resolve the tablename to an entity class)
     * @return
     */
    protected String addSetLastModifiedByToUpdateSql(String sql, ExecutableElement methodEl,
                                                     TypeElement daoType, TypeElement dbType) {
        DaoMethodInfo methodInfo = new DaoMethodInfo(methodEl, daoType, processingEnv);
        TypeElement entityTypeEl = methodInfo.getUpdateQueryEntityTypeElement(dbType);

        Element lastChangedByFieldEl = DbProcessorUtils.findElementWithAnnotation(
                entityTypeEl, UmSyncLastChangedBy.class, processingEnv);

        Pattern pattern = Pattern.compile("\\sSET\\s", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if(!matcher.find())
            return sql;

        sql = sql.substring(0, matcher.end()) + " " + lastChangedByFieldEl.getSimpleName() +
                " = (SELECT deviceBits FROM SyncDeviceBits LIMIT 1), " + sql.substring(matcher.end());

        return sql;
    }

    /**
     * Generates the setAttachment method.
     *
     * @param daoType TypeElement representing the DAO
     * @param daoMethod ExecutableElement representing the abstract method whose implementation is
     *                  being generated
     * @param dbVarName Variable name for the database
     * @return MethodSpec implementing the setAttachment method
     */
    protected MethodSpec generateSetAttachmentMethod(TypeElement daoType, ExecutableElement daoMethod,
                                                  String dbVarName) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if(daoMethod.getParameters().size() != 2) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " setAttachment " +
                            "must have two parameters: the primary key and an InputStream", daoType);
            return methodBuilder.build();
        }

        VariableElement pkElement = daoMethod.getParameters().get(0);
        VariableElement attachmentSrcEl = daoMethod.getParameters().get(1);

        TypeMirror inputStreamType = processingEnv.getElementUtils().getTypeElement(
                InputStream.class.getName()).asType();
        TypeMirror fileType = processingEnv.getElementUtils().getTypeElement(File.class.getName())
                .asType();

        codeBlock.add("$1T _dbWithAttachments = ($1T)$2L;\n", UmDbWithAttachmentsDir.class,
                dbVarName)
                .add("$1T _daoOutDir = new $1T(_dbWithAttachments.getAttachmentsDir(), " +
                        "$2S);\n", File.class, daoType.getSimpleName())
                .beginControlFlow("if(!_daoOutDir.exists())")
                    .add("_daoOutDir.mkdirs();\n")
                .endControlFlow()
                .add("$1T _attachmentFile = new $1T(_daoOutDir, String.valueOf($2L));\n",
                    File.class, pkElement.getSimpleName());

        if(attachmentSrcEl.asType().equals(inputStreamType)) {
            codeBlock.add("try(")
                    .indent()
                    .add("$1T _daoFileOut = new $1T(_attachmentFile);\n", FileOutputStream.class)
                    .unindent()
                    .beginControlFlow(")")
                    .add("byte[] _buf = new byte[1024];\n")
                    .add("int _bytesRead = -1;\n")
                    .beginControlFlow("while((_bytesRead = $L.read(_buf)) != -1)",
                            attachmentSrcEl.getSimpleName())
                    .add("_daoFileOut.write(_buf, 0, _bytesRead);\n")
                    .endControlFlow()
                    .nextControlFlow("catch($T _ioe)", IOException.class)
                    .add("_ioe.printStackTrace();\n")
                    .endControlFlow();
        }else {
            codeBlock.beginControlFlow("if(_attachmentFile.exists())")
                    .add("_attachmentFile.delete();\n")
                .endControlFlow()
                .add("$L.renameTo(_attachmentFile);\n", attachmentSrcEl.getSimpleName());
        }

        methodBuilder.addCode(codeBlock.build());
        return methodBuilder.build();
    }

    /**
     * Generate a getAttachment method
     *
     * @param daoType The DAO we are generating this for
     * @param daoMethod The method to generate an implementation of
     * @param dbVarName Variable name of the database
     *
     * @return MethodSpec with an implementation of getAttachment
     */
    public MethodSpec generateGetAttachmentMethod(TypeElement daoType, ExecutableElement daoMethod,
                                            String dbVarName) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        TypeMirror stringType = processingEnv.getElementUtils()
                .getTypeElement(String.class.getName()).asType();
        TypeMirror inputStreamType = processingEnv.getElementUtils()
                .getTypeElement(InputStream.class.getName()).asType();

        if(daoMethod.getParameters().size() != 1) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + "GetAttachment " +
                            "method must have one parameter: the primary key");
            return methodBuilder.build();
        }

        if(!Arrays.asList(inputStreamType, stringType).contains(daoMethod.getReturnType())) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    formatMethodForErrorMessage(daoMethod, daoType) + " GetAttachment " +
                            "method return type must be String (representing a URI) or InputStream");
        }

        VariableElement pkVariableElement = daoMethod.getParameters().get(0);

        boolean isStringUri = daoMethod.getReturnType().equals(stringType);
        codeBlock.add("$1T _dbWithAttachments = ($1T)$2L;\n", UmDbWithAttachmentsDir.class,
                dbVarName)
                .add("$1T _daoDir = new $1T(_dbWithAttachments.getAttachmentsDir(), " +
                        "$2S);\n", File.class, daoType.getSimpleName())
                .add("$1T _attachmentFile = new $1T(_daoDir, String.valueOf($2L));\n",
                        File.class, pkVariableElement.getSimpleName());
        if(isStringUri) {
            codeBlock.add("return _attachmentFile.getAbsolutePath();\n");
        }else {
            codeBlock.add("return new $T(_attachmentFile);\n\n", FileInputStream.class);
        }

        return methodBuilder.addCode(codeBlock.build()).build();
    }


}
