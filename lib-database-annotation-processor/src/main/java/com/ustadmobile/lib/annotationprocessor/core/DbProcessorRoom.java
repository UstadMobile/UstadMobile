package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindUpdateable;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.UmDbWithExecutor;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_ROOM_OUTPUT;

/**
 * This annotation processor will generate a Room Persistence annotated Database class, an
 * Factory to create an instance of each @UmDatabase annotated class, and a Room Persistence
 * annotated DAO.
 *
 */
@SupportedOptions({OPT_ROOM_OUTPUT})
public class DbProcessorRoom extends AbstractDbProcessor{

    public static final String SUFFIX_ROOM_DAO = "_RoomDao";

    public static final String SUFFIX_ROOM_DBMANAGER = "_RoomDbManager";

    private static final String ROOM_PKG_NAME =  "android.arch.persistence.room";

    private static final String UMDB_CORE_PKG_NAME = "com.ustadmobile.core.db";

    public DbProcessorRoom() {
        setOutputDirOpt(OPT_ROOM_OUTPUT);
    }

    /**
     * Process a class with the @UmDatabase annotation. This will generate
     *
     * - A room database class, which will have the required getters for all DAOs
     * - A child class of the database class, which will use the room database class to return a DAO,
     *   and will setup the DAO with a shared ExecutorService.
     * - A factory class implementation.
     *
     *
     * @param dbType TypeElement representing the class annotated with @UmDatabase
     * @param destination Root package directory (e.g. build/generated/source/umdbprocessor) to
     *                       place generated sources in, or "filer" to use the annotation processor filer
     * @throws IOException If there are IO exceptions writing newly generated classes
     */
    @Override
    public void processDbClass(TypeElement dbType,  String destination) throws IOException {
        String roomDbClassName = dbType.getSimpleName() + "_RoomDb";
        String roomDbManagerClassName = dbType.getSimpleName() + SUFFIX_ROOM_DBMANAGER;

        System.out.println("DbProcessorRoom processing db class: " + dbType.getSimpleName());

        TypeSpec.Builder roomDbTypeSpec = TypeSpec.classBuilder(roomDbClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(ClassName.get(ROOM_PKG_NAME, "RoomDatabase"))
                .addJavadoc("Generated code - DO NOT EDIT!");
        String packageName = processingEnv.getElementUtils().getPackageOf(dbType).getQualifiedName().toString();


        TypeSpec.Builder dbManagerImplSpec = TypeSpec.classBuilder(roomDbManagerClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addSuperinterface(UmDbWithExecutor.class)
                .addField(ClassName.get(ExecutorService.class), "dbExecutor", Modifier.PRIVATE)
                .addField(ParameterizedTypeName.get(List.class, UmRepositoryDb.class), "_repositories",
                        Modifier.PRIVATE)
                .addField(ClassName.get("android.content", "Context"),
                        "context",Modifier.PRIVATE)
                .addField(ClassName.get(packageName,
                        roomDbClassName), "_roomDb",
                        Modifier.PRIVATE)
                .addJavadoc("Generated code - DO NOT EDIT!");

        TypeSpec.Builder factoryClassSpec = DbProcessorUtils.makeFactoryClass(dbType,
                roomDbManagerClassName);



        dbManagerImplSpec.addMethod(MethodSpec.constructorBuilder()
                    .addParameter(ClassName.get(Object.class), "context")
                    .addParameter(ClassName.get(String.class), "dbName")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("this.context = (Context)context;\n")
                    .addCode("this.dbExecutor = $T.newCachedThreadPool();\n", Executors.class)
                    .addCode("this._repositories = new $T<>();\n", Vector.class)
                    .addCode("_roomDb = $T.databaseBuilder(this.context, " + roomDbClassName +
                            ".class, dbName).build();\n",
                            ClassName.get("android.arch.persistence.room", "Room"))
                .build())
            .addMethod(MethodSpec.methodBuilder("execute")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(Runnable.class, "_runnable")
                    .addCode("this.dbExecutor.execute(_runnable);\n").build());

        UmDatabase db = dbType.getAnnotation(UmDatabase.class);

        AnnotationSpec.Builder roomDbAnnotationSpec = AnnotationSpec
                .builder(ClassName.get("android.arch.persistence.room", "Database"))
                .addMember("version", String.valueOf(db.version()));

        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationEntryMap =
            dbType.getAnnotationMirrors().get(0).getElementValues();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationEntryMap.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if(key.equals("entities")) {
                List<? extends AnnotationValue> typeMirrors =
                        (List<? extends AnnotationValue>)value;

                CodeBlock.Builder roomEntitiesCodeblock = CodeBlock.builder().add("{");

                for(int i = 0; i < typeMirrors.size(); i++) {
                    roomEntitiesCodeblock.add("$T.class", (TypeMirror)typeMirrors.get(i).getValue());
                    if(i < typeMirrors.size() - 1)
                        roomEntitiesCodeblock.add(", ");
                }

                roomEntitiesCodeblock.add("}");
                roomDbAnnotationSpec.addMember("entities", roomEntitiesCodeblock.build());
                roomDbTypeSpec.addAnnotation(roomDbAnnotationSpec.build());

            }
        }

        //now go through all methods that return DAO objects and create matching methods
        for(Element subElement : dbType.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement daoMethod = (ExecutableElement)subElement;
            if(daoMethod.getAnnotation(UmDbContext.class) != null) {
                MethodSpec.Builder contextMethodBuilder =
                        MethodSpec.methodBuilder(daoMethod.getSimpleName().toString())
                        .returns(ClassName.get(Object.class))
                        .addCode("return this.context;\n")
                        .addAnnotation(Override.class);

                if(daoMethod.getModifiers().contains(Modifier.PROTECTED))
                    contextMethodBuilder.addModifiers(Modifier.PROTECTED);
                else if(daoMethod.getModifiers().contains(Modifier.PUBLIC))
                    contextMethodBuilder.addModifiers(Modifier.PUBLIC);

                dbManagerImplSpec.addMethod(contextMethodBuilder.build());
            }else if(daoMethod.getAnnotation(UmClearAll.class) != null) {
                dbManagerImplSpec.addMethod(generateClearAllMethod(daoMethod).build());
            }else if(daoMethod.getAnnotation(UmRepository.class) != null) {
                MethodSpec.Builder repoMethodBuilder = MethodSpec.overriding(daoMethod);
                addGetRepositoryMethod(dbType, daoMethod, repoMethodBuilder,
                        "_repositories");
                dbManagerImplSpec.addMethod(repoMethodBuilder.build());
            }else if(daoMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                dbManagerImplSpec.addMethod(generateDbSyncOutgoingMethod(dbType, daoMethod).build());
            }

            //Lookup using processingEnv.getElementUtils.getTypeElement
            if(daoMethod.getReturnType() == null)
                continue;

            if(daoMethod.getReturnType().getKind() != TypeKind.DECLARED)
                continue;

            DeclaredType returnType = (DeclaredType)daoMethod.getReturnType();
            if(returnType.asElement().getAnnotation(UmDao.class) == null)
                continue;

            addDaoMethod(roomDbTypeSpec, dbManagerImplSpec, daoMethod);
        }


        writeJavaFileToDestination(JavaFile.builder(packageName, roomDbTypeSpec.build()).build(),
            destination);
        writeJavaFileToDestination(JavaFile.builder(packageName, dbManagerImplSpec.build()).build(),
            destination);
        writeJavaFileToDestination(JavaFile.builder(packageName, factoryClassSpec.build()).build(),
            destination);
    }

    private void addDaoMethod(TypeSpec.Builder roomDbTypeSpec, TypeSpec.Builder dbManagerSpec,
                              ExecutableElement daoMethod)  {
        DeclaredType coreDaoClassType = (DeclaredType)daoMethod.getReturnType();
        Element coreDaoClass = coreDaoClassType.asElement();
        String roomDaoClassName = coreDaoClass.getSimpleName().toString() + SUFFIX_ROOM_DAO;
        String daoPackageName = processingEnv.getElementUtils().getPackageOf(coreDaoClass)
                .getQualifiedName().toString();
        ClassName roomDaoClassNameType = ClassName.get(daoPackageName, roomDaoClassName);

        String daoMethodName = daoMethod.getSimpleName().toString();
        roomDbTypeSpec.addMethod(MethodSpec.methodBuilder(daoMethodName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(roomDaoClassNameType)
                .build());

        String daoFieldName = "_" + roomDaoClassName;
        dbManagerSpec.addField(FieldSpec.builder(roomDaoClassNameType, daoFieldName,
                Modifier.PRIVATE).build());
        dbManagerSpec.addMethod(MethodSpec.methodBuilder(daoMethodName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(ClassName.get(daoMethod.getReturnType()))
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if($L == null)", daoFieldName)
                            .add("$L = _roomDb.$L();\n", daoFieldName, daoMethodName)
                            .add("$L.setExecutor(dbExecutor);\n", daoFieldName)
                            .add("$L.setDbManager(this);\n", daoFieldName)
                        .endControlFlow()
                        .add("return $L;\n", daoFieldName).build()).build());

    }


    /**
     * Process the given DAO class and generate a child class with the appropriate room annotations.
     *
     * @param daoClass TypeElement representing the class with @UmDao annotation
     * @param destination Root package directory for generated source output
     *
     * @throws IOException When there is an IO issue writing the generated output
     */
    public void processDbDao(TypeElement daoClass, TypeElement dbType, String destination) throws IOException {
        String daoClassName = daoClass.getSimpleName() + SUFFIX_ROOM_DAO;
        TypeSpec.Builder roomDaoClassSpec = TypeSpec.classBuilder(daoClassName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addAnnotation(ClassName.get(ROOM_PKG_NAME, "Dao"))
                .superclass(ClassName.get(daoClass))
                .addField(ClassName.get(ExecutorService.class), "dbExecutor", Modifier.PRIVATE)
                .addField(Object.class, "_dbManager", Modifier.PRIVATE);

        roomDaoClassSpec.addMethod(MethodSpec.methodBuilder("setExecutor")
                .addParameter(ClassName.get(ExecutorService.class), "dbExecutor")
                .addModifiers(Modifier.PUBLIC)
                .addCode("this.dbExecutor = dbExecutor;\n").build());

        roomDaoClassSpec.addMethod(MethodSpec.methodBuilder("setDbManager")
                .addParameter(Object.class, "_dbManager")
                .addModifiers(Modifier.PUBLIC)
                .addCode("this._dbManager = _dbManager;\n").build());

        //now generate methods for all query, insert, and delete methods
        for(ExecutableElement daoMethod : findMethodsToImplement(daoClass)) {
            MethodSpec.Builder methodBuilder = null;

            if(daoMethod.getAnnotation(UmInsert.class) != null) {
                UmInsert umInsert = daoMethod.getAnnotation(UmInsert.class);
                AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Insert"))
                        .addMember("onConflict", ""+umInsert.onConflict()).build();
                methodBuilder = generateAnnotatedMethod(daoMethod, daoClass, annotation, roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmDelete.class) != null) {
                methodBuilder = generateAnnotatedMethod(daoMethod, daoClass,
                        AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Delete")).build(),
                        roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmUpdate.class) != null) {
                methodBuilder = generateAnnotatedMethod(daoMethod, daoClass,
                        AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Update")).build(),
                        roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmQuery.class) != null) {
                methodBuilder = generateQueryMethod(daoMethod.getAnnotation(UmQuery.class).value(),
                        daoMethod, daoClass, roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmQueryFindByPrimaryKey.class) != null) {
                methodBuilder = generateQueryMethod(
                        generateFindByPrimaryKeySql(daoClass, daoMethod, processingEnv, '`'),
                        daoMethod, daoClass, roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmSyncIncoming.class) != null) {
                addSyncHandleIncomingMethod(daoMethod, daoClass, roomDaoClassSpec, "_dbManager");
            }else if(daoMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                addSyncOutgoing(daoMethod, daoClass, roomDaoClassSpec, "_dbManager");
            }else if(daoMethod.getAnnotation(UmSyncFindLocalChanges.class) != null) {
                methodBuilder = generateQueryMethod(generateFindLocalChangesSql(daoClass, daoMethod,
                        processingEnv), daoMethod, daoClass, roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmSyncFindAllChanges.class) != null) {
                methodBuilder = generateQueryMethod(generateSyncFindAllChanges(daoClass, daoMethod,
                        processingEnv), daoMethod, daoClass, roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmSyncFindUpdateable.class) != null) {
                methodBuilder = generateQueryMethod(generateSyncFindUpdatable(daoClass, daoMethod,
                        processingEnv), daoMethod, daoClass, roomDaoClassSpec);
            }

            if(methodBuilder != null){
                roomDaoClassSpec.addMethod(methodBuilder.addAnnotation(Override.class).build());
            }
        }

        writeJavaFileToDestination(
                JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoClass).toString(),
                roomDaoClassSpec.build()).build(), destination);
    }

    /**
     * Generate an annotated method wrapper with the given annotation. This can be synchronous
     * (simply override) or asynchronous, in which case the implementing method will use the
     * DAOs ExecutorService to run the Room Persistence method. The room persistence method itself
     * will be added as well.
     *
     * @param daoMethod DaoMethod to generate an annotated method wrapper for
     * @param annotationSpec Annotation to add to the given method
     * @param roomDaoClassSpec The DAO TypeSpec.Builder for the DAO class being generated
     *
     * @return MethodSpec.Builder for the override implementation of the method.
     */
    private MethodSpec.Builder generateAnnotatedMethod(ExecutableElement daoMethod,
                                                       TypeElement daoType,
                                                       AnnotationSpec annotationSpec,
                                                       TypeSpec.Builder roomDaoClassSpec) {
        MethodSpec.Builder methodBuilder;

        List<? extends VariableElement> variableElementList = daoMethod.getParameters();
        List<Element> variableTypeElements = getMethodParametersAsElements(daoMethod);

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                "com.ustadmobile.core.impl.UmCallback");


        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);

        boolean isAsyncMethod = variableTypeElements.contains(umCallbackTypeElement);

        String insertMethodName = isAsyncMethod ? daoMethod.getSimpleName() +"_Room"
                : daoMethod.getSimpleName().toString();

        TypeName insertRetType;
        int asyncParamIndex = variableTypeElements.indexOf(umCallbackTypeElement);
        if(isAsyncMethod) {
            //TODO: throw an error if this is untyped
            DeclaredType declaredType = (DeclaredType)variableElementList.get(asyncParamIndex).asType();
            insertRetType = convertToPrimitiveIfApplicable(declaredType.getTypeArguments().get(0));
        }else {
            insertRetType = TypeName.get(daoMethod.getReturnType());
        }

        MethodSpec.Builder roomInsertMethodBuilder = MethodSpec.methodBuilder(insertMethodName)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.ABSTRACT)
                .returns(insertRetType);


        addParametersToMethodBuilder(roomInsertMethodBuilder, daoMethod, daoType, umCallbackTypeElement);

        addAccessModifiersFromMethod(roomInsertMethodBuilder, daoMethod);

        if(!isAsyncMethod) {
            methodBuilder = roomInsertMethodBuilder;
        }else {
            roomDaoClassSpec.addMethod(roomInsertMethodBuilder.build());

            DeclaredType declaredType = (DeclaredType)variableElementList.get(asyncParamIndex).asType();
            methodBuilder = MethodSpec.methodBuilder(daoMethod.getSimpleName().toString())
                    .returns(TypeName.VOID);
            addParametersToMethodBuilder(methodBuilder, daoMethod, daoType);
            addAccessModifiersFromMethod(methodBuilder, daoMethod);
            String callbackParamName = variableElementList.get(asyncParamIndex).getSimpleName().toString();
            CodeBlock.Builder asyncInsert = CodeBlock.builder();
            asyncInsert.add("dbExecutor.execute(() -> ");
            ClassName umCallbackUtilClassName = ClassName.get("com.ustadmobile.core.impl",
                    "UmCallbackUtil");
            if(declaredType.getKind().equals(TypeKind.VOID)){
                asyncInsert.add("{$L$L; $T.onSuccessIfNotNull($L, null);}",
                        insertMethodName,
                        makeNamedParameterMethodCall(daoMethod.getParameters(), umCallbackTypeElement),
                        umCallbackUtilClassName,
                        callbackParamName);
            }else {
                asyncInsert.add("$T.onSuccessIfNotNull($L, $L$L)",
                        umCallbackUtilClassName,
                        callbackParamName, insertMethodName,
                        makeNamedParameterMethodCall(daoMethod.getParameters(), umCallbackTypeElement));
            }
            asyncInsert.add(");\n");
            methodBuilder.addCode(asyncInsert.build());
        }

        return methodBuilder;
    }


    /**
     * Add the parameters from an ExecutableElement to the given MethodSpec.Builder, optionally
     * excluding particular types (e.g. callbacks)
     *
     * @param methodBuilder MethodSpec.Builder for the method being created
     * @param method source method from which argument parameters will be added
     * @param clazz the direct parent class, for which a method is being overridden (used for type argument resolution)
     * @param excludedTypes excluded TypeElements that should not be added to the given MethodSpec.Builder
     */
    private void addParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
                                              ExecutableElement method, TypeElement clazz,
                                              Element... excludedTypes) {
        List<Element> excludedTypeList = Arrays.asList(excludedTypes);
        List<? extends VariableElement> variableElementList = method.getParameters();

        for(int i = 0; i < variableElementList.size(); i++) {
            TypeMirror variableType = variableElementList.get(i).asType();
            if(excludedTypeList.contains(processingEnv.getTypeUtils().asElement(variableType)))
                continue;

            TypeMirror paramTypeMirror = DbProcessorUtils.resolveType(variableType, clazz,
                    processingEnv);
            methodBuilder.addParameter(TypeName.get(paramTypeMirror),
                    variableElementList.get(i).getSimpleName().toString());
        }
    }

    private TypeName convertToPrimitiveIfApplicable(TypeMirror type) {
        if(type.getKind().equals(TypeKind.DECLARED)) {
            Element typeEl = processingEnv.getTypeUtils().asElement(type);
            if(processingEnv.getElementUtils().getPackageOf(typeEl).getQualifiedName().toString().equals("java.lang")) {
                String className = typeEl.getSimpleName().toString();
                if(className.equals("Long") || className.equals("Integer"))
                    return TypeName.get(processingEnv.getTypeUtils().unboxedType(type));
                else if(className.equals("Void"))
                    return TypeName.VOID;
            }
        }

        return TypeName.get(type);
    }


    /**
     * Generate a DAO Query Method for a method on the DAO that was annotated @UmQuery.
     *
     * This could involve EntryProvider and LiveData wrappers. This could be a simple override with
     * adding room annotation. This can also involve an async method that uses UmCallback&lt;T&gt;.
     *
     * @param daoMethod ExecutableElement representing the method with @UmQuery annotation
     * @param daoClassBuilder JavaPoet ClassBuilder for the DAO that contains this method
     *
     * @return MethodSpec.Builder for the implementation of this DAO method.
     */
    private MethodSpec.Builder generateQueryMethod(String querySql,
                                                   ExecutableElement daoMethod,
                                                   TypeElement daoType,
                                                   TypeSpec.Builder daoClassBuilder) {
        //check for livedata return types
        //Class returnType = daoMethod.getReturnType();
        TypeMirror returnType = daoMethod.getReturnType();
        Element returnTypeElement = processingEnv.getTypeUtils().asElement(returnType);
        TypeElement umLiveDataTypeElement = processingEnv.getElementUtils().getTypeElement(
                UMDB_CORE_PKG_NAME + ".UmLiveData");

        ClassName queryClassName = ClassName.get(ROOM_PKG_NAME, "Query");
        AnnotationSpec.Builder querySpec = AnnotationSpec.builder(queryClassName)
                .addMember("value", CodeBlock.builder().add("$S", querySql).build());

        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);

        MethodSpec.Builder retMethod = MethodSpec.methodBuilder(daoMethod.getSimpleName().toString())
                .returns(TypeName.get(daoMethodInfo.resolveReturnType()));
        addAccessModifiersFromMethod(retMethod, daoMethod);


        for(VariableElement argument : daoMethod.getParameters()) {
            retMethod.addParameter(TypeName.get(argument.asType()),
                    argument.getSimpleName().toString());
        }

        List<Element> paramTypeElements = new ArrayList<>();
        for(VariableElement argument : daoMethod.getParameters()) {
            paramTypeElements.add(processingEnv.getTypeUtils().asElement(argument.asType()));
        }

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils()
                .getTypeElement("com.ustadmobile.core.impl.UmCallback");

        if(umLiveDataTypeElement.equals(returnTypeElement)) {
            DeclaredType declaredType = (DeclaredType)daoMethod.getReturnType();

            ParameterizedTypeName liveDataReturnType = ParameterizedTypeName.get(
                    ClassName.get("android.arch.lifecycle", "LiveData"),
                    TypeName.get(declaredType.getTypeArguments().get(0)));
            String liveDataMethodName = daoMethod.getSimpleName() + "_RoomLive";
            MethodSpec.Builder roomLiveDataBuilder = MethodSpec.methodBuilder(liveDataMethodName)
                    .addAnnotation(querySpec.build())
                    .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
                    .returns(liveDataReturnType);

            CodeBlock.Builder retMethodCodeBlock = CodeBlock.builder().add(
                    "return new $T<>($L",
                    ClassName.get("com.ustadmobile.port.android.db",
                            "UmLiveDataAndroid"), liveDataMethodName);

            addParametersToMethodBuilder(roomLiveDataBuilder, daoMethod, daoType);
            retMethodCodeBlock.add(makeNamedParameterMethodCall(daoMethod.getParameters())).add(");\n");

            retMethod.addCode(retMethodCodeBlock.build());
            daoClassBuilder.addMethod(roomLiveDataBuilder.build());
        }else if(processingEnv.getElementUtils()
                .getTypeElement(UMDB_CORE_PKG_NAME + ".UmProvider")
                .equals(returnTypeElement)) {
            DeclaredType declaredType = (DeclaredType) daoMethod.getReturnType();

            ParameterizedTypeName factoryReturnType = ParameterizedTypeName.get(
                    ClassName.get("android.arch.paging.DataSource", "Factory"),
                    ClassName.get(Integer.class), ClassName.get(declaredType.getTypeArguments().get(0)));
            String factoryMethodName = daoMethod.getSimpleName() + "_RoomFactory";
            CodeBlock.Builder retMethodCodeBlock = CodeBlock.builder()
                    .add("return () -> $L$L;\n",
                            factoryMethodName,
                            makeNamedParameterMethodCall(daoMethod.getParameters()));
            retMethod.addCode(retMethodCodeBlock.build());

            MethodSpec.Builder factoryMethodBuilder = MethodSpec.methodBuilder(factoryMethodName)
                    .addAnnotation(querySpec.build())
                    .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
                    .returns(factoryReturnType);
            addParametersToMethodBuilder(factoryMethodBuilder, daoMethod, daoType);
            daoClassBuilder.addMethod(factoryMethodBuilder.build());
        }else if(paramTypeElements.contains(umCallbackTypeElement)){
            //this is an async method, run it on the executor
            String roomMethodName = daoMethod.getSimpleName() + "_Room";

            //find the callback
            int callbackParamNum = paramTypeElements.indexOf(umCallbackTypeElement);

            DeclaredType callbackDeclaredType = (DeclaredType)daoMethod.getParameters()
                    .get(callbackParamNum).asType();
            TypeName returnTypeName = convertToPrimitiveIfApplicable(
                    callbackDeclaredType.getTypeArguments().get(0));
            MethodSpec.Builder roomMethodBuilder = MethodSpec.methodBuilder(roomMethodName)
                    .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                    .addAnnotation(querySpec.build())
                    .returns(returnTypeName);

            addParametersToMethodBuilder(roomMethodBuilder, daoMethod, daoType, umCallbackTypeElement);
            daoClassBuilder.addMethod(roomMethodBuilder.build());

            String callbackParamName = daoMethod.getParameters().get(callbackParamNum)
                    .getSimpleName().toString();
            ClassName umCallbackUtilClassName = ClassName.get("com.ustadmobile.core.impl",
                    "UmCallbackUtil");
            if(!returnTypeName.equals(TypeName.VOID)) {
                retMethod.addCode("dbExecutor.execute(() -> $T.onSuccessIfNotNull($L, $L$L));\n",
                        umCallbackUtilClassName,
                        callbackParamName,
                        roomMethodName,
                        makeNamedParameterMethodCall(daoMethod.getParameters(),
                                umCallbackTypeElement));
            }else {
                retMethod.addCode("dbExecutor.execute(() -> { $L$L; $T.onSuccessIfNotNull($L, null); });\n",
                        roomMethodName,
                        makeNamedParameterMethodCall(daoMethod.getParameters(),
                                umCallbackTypeElement),
                        umCallbackUtilClassName,
                        callbackParamName);
            }
        }else {
            //this is just a simple override
            retMethod.addAnnotation(querySpec.build())
                    .addModifiers(Modifier.ABSTRACT);
        }

        return retMethod;

    }

    private MethodSpec.Builder generateClearAllMethod(ExecutableElement daoMethod) {
        return MethodSpec.overriding(daoMethod).addCode("_roomDb.clearAllTables();\n");
    }


    /**
     * Set the access modifier for the given method builder according to the given ExecutableElement
     * @param methodBuilder MethodBuilder to set access modifier for
     * @param method ExecutableElement to set access modifier from
     */
    private void addAccessModifiersFromMethod(MethodSpec.Builder methodBuilder, ExecutableElement method) {
        if(method.getModifiers().contains(Modifier.PUBLIC)){
            methodBuilder.addModifiers(Modifier.PUBLIC);
        }else if(method.getModifiers().contains(Modifier.PROTECTED)){
            methodBuilder.addModifiers(Modifier.PROTECTED);
        }
    }


}
