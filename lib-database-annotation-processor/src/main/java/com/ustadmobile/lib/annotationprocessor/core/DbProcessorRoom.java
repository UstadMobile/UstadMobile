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
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmEntity;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanInsert;
import com.ustadmobile.lib.database.annotation.UmSyncCountLocalPendingChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindAllChanges;
import com.ustadmobile.lib.database.annotation.UmSyncFindLocalChanges;
import com.ustadmobile.lib.database.annotation.UmSyncCheckIncomingCanUpdate;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.database.annotation.UmUpdate;
import com.ustadmobile.lib.db.UmDbWithExecutor;
import com.ustadmobile.lib.db.UmDbWithSyncableInsertLock;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;
import com.ustadmobile.lib.db.sync.entities.SyncDeviceBits;
import com.ustadmobile.lib.db.sync.entities.SyncStatus;
import com.ustadmobile.lib.db.sync.entities.SyncablePrimaryKey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
import javax.lang.model.type.ArrayType;
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
public class DbProcessorRoom extends AbstractDbProcessor implements QueryMethodGenerator{

    public static final String SUFFIX_ROOM_DAO = "_RoomDao";

    public static final String SUFFIX_ROOM_DBMANAGER = "_RoomDbManager";

    private static final String ROOM_PKG_NAME =  "android.arch.persistence.room";

    private static final String UMDB_CORE_PKG_NAME = "com.ustadmobile.core.db";

    private static final List<TypeKind> INSERT_BIND_LONG = Arrays.asList(TypeKind.INT,
            TypeKind.SHORT, TypeKind.LONG, TypeKind.BYTE);

    private static final List<TypeKind> INSERT_BIND_DOUBLE = Arrays.asList(TypeKind.FLOAT,
            TypeKind.DOUBLE);

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


        dbManagerImplSpec.addMethod(MethodSpec.constructorBuilder()
                    .addParameter(ClassName.get(Object.class), "context")
                    .addParameter(ClassName.get(String.class), "dbName")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("this.context = (Context)context;\n")
                    .addCode("this.dbExecutor = $T.newCachedThreadPool();\n", Executors.class)
                    .addCode("this._repositories = new $T<>();\n", Vector.class)
                    .addCode("_roomDb = $T.databaseBuilder(this.context, " + roomDbClassName +
                            ".class, dbName).addCallback(new DbManagerCallback()).build();\n",
                            ClassName.get("android.arch.persistence.room", "Room"))
                .build())
            .addMethod(MethodSpec.methodBuilder("execute")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(Runnable.class, "_runnable")
                    .addCode("this.dbExecutor.execute(_runnable);\n").build())
            .addType(TypeSpec.classBuilder("DbManagerCallback")
                    .superclass(ClassName.get(ROOM_PKG_NAME, "RoomDatabase")
                            .nestedClass("Callback"))
                    .addMethod(generateOnCreateMethod(dbType, dbManagerImplSpec)).build());

        if(DbProcessorUtils.isSyncableDatabase(dbType.asType(), processingEnv)) {
            addDbWithSyncableInsertLockImplementation(dbManagerImplSpec);
            dbManagerImplSpec.addSuperinterface(ClassName.get("com.ustadmobile.lib.database",
                    "UmRoomDbManagerWithSyncablePk"))
                    .addField(ClassName.get("android.arch.persistence.db",
                            "SupportSQLiteQuery"), "_lastPksQuery")
                    .addField(ClassName.get("android.arch.persistence.db",
                            "SupportSQLiteStatement"), "_deleteLastPksStatement")
                    .addMethod(MethodSpec.methodBuilder("getLastPksQuery")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("android.arch.persistence.db",
                                "SupportSQLiteQuery"))
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("if(_lastPksQuery == null)")
                                    .add("_lastPksQuery = new $T();\n",
                                            ClassName.get("com.ustadmobile.lib.database",
                                                    "SyncablePkUtilsAndroid",
                                                    "GetLastSyncablePkQuery"))
                                    .endControlFlow()
                                    .add("return _lastPksQuery;\n").build())
                            .build())
                    .addMethod(MethodSpec.methodBuilder("getDeleteLastPksStatement")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("android.arch.persistence.db",
                                "SupportSQLiteStatement"))
                        .addCode(CodeBlock.builder()
                            .beginControlFlow("if(_deleteLastPksStatement == null)")
                                .add("_deleteLastPksStatement = _roomDb.compileStatement($S);\n",
                                        "DELETE FROM _lastsyncablepk")
                                .endControlFlow()
                                .add("return _deleteLastPksStatement;\n").build())
                            .build());

        }

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

            ExecutableElement dbMethod = (ExecutableElement)subElement;
            if(dbMethod.getAnnotation(UmDbContext.class) != null) {
                MethodSpec.Builder contextMethodBuilder =
                        MethodSpec.methodBuilder(dbMethod.getSimpleName().toString())
                        .returns(ClassName.get(Object.class))
                        .addCode("return this.context;\n")
                        .addAnnotation(Override.class);

                if(dbMethod.getModifiers().contains(Modifier.PROTECTED))
                    contextMethodBuilder.addModifiers(Modifier.PROTECTED);
                else if(dbMethod.getModifiers().contains(Modifier.PUBLIC))
                    contextMethodBuilder.addModifiers(Modifier.PUBLIC);

                dbManagerImplSpec.addMethod(contextMethodBuilder.build());
            }else if(dbMethod.getAnnotation(UmClearAll.class) != null) {
                dbManagerImplSpec.addMethod(generateClearAllMethod(dbMethod, dbType).build());
            }else if(dbMethod.getAnnotation(UmRepository.class) != null) {
                MethodSpec.Builder repoMethodBuilder = MethodSpec.overriding(dbMethod);
                addGetRepositoryMethod(dbType, dbMethod, repoMethodBuilder,
                        "_repositories");
                dbManagerImplSpec.addMethod(repoMethodBuilder.build());
            }else if(dbMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                dbManagerImplSpec.addMethod(generateDbSyncOutgoingMethod(dbType, dbMethod).build());
            }else if(dbMethod.getAnnotation(UmSyncCountLocalPendingChanges.class) != null) {
                dbManagerImplSpec.addMethod(generateDbSyncCountLocalPendingChangesMethod(dbType,
                        dbMethod));
            }

            //Lookup using processingEnv.getElementUtils.getTypeElement
            if(dbMethod.getReturnType() == null)
                continue;

            if(dbMethod.getReturnType().getKind() != TypeKind.DECLARED)
                continue;

            DeclaredType returnType = (DeclaredType)dbMethod.getReturnType();
            if(returnType.asElement().getAnnotation(UmDao.class) == null)
                continue;

            addDaoMethod(roomDbTypeSpec, dbManagerImplSpec, dbMethod);
        }


        writeJavaFileToDestination(JavaFile.builder(packageName, roomDbTypeSpec.build()).build(),
            destination);
        writeJavaFileToDestination(JavaFile.builder(packageName, dbManagerImplSpec.build()).build(),
            destination);
    }


    private MethodSpec generateOnCreateMethod(TypeElement dbType, TypeSpec.Builder dbManagerBuilder) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onCreate")
                .addParameter(ClassName.get("android.arch.persistence.db",
                        "SupportSQLiteDatabase"), "_db")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC);
        CodeBlock.Builder codeBlock = CodeBlock.builder();

        if(DbProcessorUtils.isSyncableDatabase(dbType.asType(), processingEnv)) {
            codeBlock.add("_db.execSQL($S);\n", SQLITE_CREATE_LAST_SYNCABLE_PK_SQL);
            codeBlock.add("int _deviceBits = new $T().nextInt();\n", Random.class);
            codeBlock.add("_db.execSQL(\"INSERT INTO SyncDeviceBits (id, deviceBits, master) " +
                    "VALUES (1, \" + _deviceBits + \", 0)\");\n");
        }

        for(TypeElement entityType : DbProcessorUtils.findEntityTypes(dbType, processingEnv)) {
            if(DbProcessorUtils.entityHasChangeSequenceNumbers(entityType, processingEnv)) {
                codeBlock.add("// Begin: $L - create triggers\n", entityType.getSimpleName());
                codeBlock.add(generateChangeSequenceTriggersCodeBlock("SQLite",
                        "_db.execSQL", entityType));
                codeBlock.add("// END: $L - create triggers\n\n", entityType.getSimpleName());
            }
        }

        builder.addCode(codeBlock.build());
        return builder.build();
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
                            .add("$L.setRoomDatabase(_roomDb);\n", daoFieldName)
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

        ClassName roomDatabaseClassName = ClassName.get(ROOM_PKG_NAME, "RoomDatabase");
        roomDaoClassSpec.addField(roomDatabaseClassName, "_roomDb");
        MethodSpec.Builder setRoomDbMethodBuilder = MethodSpec.methodBuilder("setRoomDatabase")
                .addParameter(roomDatabaseClassName, "roomDb")
                .addModifiers(Modifier.PUBLIC);
        CodeBlock.Builder setRoomDbCodeBlock = CodeBlock.builder()
                .add("this._roomDb = roomDb;\n");


        //now generate methods for all query, insert, and delete methods
        Map<String, Integer> entityInsertionVarNamePostfixMap = new HashMap<>();

        for(ExecutableElement daoMethod : findMethodsToImplement(daoClass)) {
            MethodSpec.Builder methodBuilder = null;
            DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoClass, processingEnv);


            if(methodInfo.isInsertWithAutoSyncPrimaryKey()) {
                roomDaoClassSpec.addMethod(generateRoomSyncableInsertMethod(daoMethod, daoClass,
                        roomDaoClassSpec, setRoomDbCodeBlock, "_roomDb",
                        entityInsertionVarNamePostfixMap));
            }else if(daoMethod.getAnnotation(UmInsert.class) != null) {
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
                roomDaoClassSpec.addMethod(
                        generateQueryMethod(daoMethod.getAnnotation(UmQuery.class).value(),
                        daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmQueryFindByPrimaryKey.class) != null) {
                roomDaoClassSpec.addMethod(generateQueryMethod(
                        generateFindByPrimaryKeySql(daoClass, daoMethod, processingEnv, '`'),
                        daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmSyncIncoming.class) != null) {
                roomDaoClassSpec.addMethod(
                        generateSyncIncomingMethod(daoMethod, daoClass, roomDaoClassSpec,
                                "_dbManager"));
            }else if(daoMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                addSyncOutgoing(daoMethod, daoClass, roomDaoClassSpec, "_dbManager");
            }else if(daoMethod.getAnnotation(UmSyncFindLocalChanges.class) != null) {
                roomDaoClassSpec.addMethod(
                        generateQueryMethod(generateFindLocalChangesSql(daoClass, daoMethod,
                        processingEnv), daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmSyncFindAllChanges.class) != null) {
                roomDaoClassSpec.addMethod(generateQueryMethod(
                        generateSyncFindAllChanges(daoClass, daoMethod,
                        processingEnv), daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmSyncCheckIncomingCanUpdate.class) != null) {
                roomDaoClassSpec.addMethod(generateQueryMethod(generateSyncFindUpdatableSql(daoClass,
                        daoMethod, processingEnv), daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmSyncCheckIncomingCanInsert.class) != null) {
                roomDaoClassSpec.addMethod(generateQueryMethod(generateSyncCheckCanInsertSql(daoClass,
                        daoMethod, processingEnv), daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmSyncCountLocalPendingChanges.class) != null) {
                roomDaoClassSpec.addMethod(generateQueryMethod(
                        generateSyncCountPendingLocalChangesSql(daoClass, daoMethod, processingEnv),
                        daoMethod, daoClass, dbType, roomDaoClassSpec));
            }else if(daoMethod.getAnnotation(UmDbGetAttachment.class) != null) {
                roomDaoClassSpec.addMethod(generateGetAttachmentMethod(daoClass, daoMethod,
                        "_dbManager"));
            }else if(daoMethod.getAnnotation(UmDbSetAttachment.class) != null) {
                roomDaoClassSpec.addMethod(generateSetAttachmentMethod(daoClass, daoMethod,
                        "_dbManager"));
            }

            if(methodBuilder != null){
                roomDaoClassSpec.addMethod(methodBuilder.addAnnotation(Override.class).build());
            }
        }

        setRoomDbMethodBuilder.addCode(setRoomDbCodeBlock.build());
        roomDaoClassSpec.addMethod(setRoomDbMethodBuilder.build());

        writeJavaFileToDestination(
                JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoClass).toString(),
                roomDaoClassSpec.build()).build(), destination);
    }

    private MethodSpec generateRoomSyncableInsertMethod(ExecutableElement daoMethod,
                                                        TypeElement daoType,
                                                        TypeSpec.Builder daoTypeBuilder,
                                                        CodeBlock.Builder constructorCodeBlock,
                                                        String roomDbVarName,
                                                        Map<String, Integer> adapterPostfixMap) {
        MethodSpec.Builder methodBuilder = overrideAndResolve(daoMethod, daoType, processingEnv);
        CodeBlock.Builder codeBlock = CodeBlock.builder();
        DaoMethodInfo methodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);
        TypeMirror entityComponentType = methodInfo.resolveEntityParameterComponentType();
        TypeElement entityComponentTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(
                entityComponentType);
        TypeName entityInsertionAdapterTypeName = ParameterizedTypeName.get(
                ClassName.get("android.arch.persistence.room", "EntityInsertionAdapter"),
                ClassName.get(entityComponentType));




        CodeBlock.Builder insertQueryCodeBlock = CodeBlock.builder()
                .add("return \"INSERT INTO `$L_spk_view` (", entityComponentTypeEl.getSimpleName());
        CodeBlock.Builder bindCodeBlock = CodeBlock.builder();
        StringBuilder paramSection = new StringBuilder();
        int fieldCount = 0;
        List<VariableElement> fieldList = DbProcessorUtils.getEntityFieldElements(
                entityComponentTypeEl, processingEnv);
        for(VariableElement field : fieldList) {
            if(fieldCount > 0) {
                insertQueryCodeBlock.add(", ");
                paramSection.append(", ");
            }

            insertQueryCodeBlock.add("`$L`", field.getSimpleName());
            paramSection.append("?");

            TypeMirror fieldType = DbProcessorUtils.unboxIfBoxed(field.asType(), processingEnv);

            if(fieldType.getKind().equals(TypeKind.DECLARED)) {
                TypeElement fieldTypeEl = (TypeElement)processingEnv.getTypeUtils()
                        .asElement(field.asType());
                if(fieldTypeEl.getQualifiedName().toString().equals(String.class.getName())) {
                    bindCodeBlock.beginControlFlow("if(value.get$L() != null)",
                            DbProcessorUtils.capitalize(field.getSimpleName()))
                                .add("stmt.bindString($L, value.get$L());\n",
                                    fieldCount + 1,
                                    DbProcessorUtils.capitalize(field.getSimpleName()))
                            .nextControlFlow("else")
                                .add("stmt.bindNull($L);\n", fieldCount+1)
                            .endControlFlow();
                }
            }else if(INSERT_BIND_LONG.contains(fieldType.getKind())) {
                bindCodeBlock.add("stmt.bindLong($L, value.get$L());\n",
                        fieldCount + 1, DbProcessorUtils.capitalize(field.getSimpleName()));
            }else if(fieldType.getKind().equals(TypeKind.BOOLEAN)) {
                bindCodeBlock.add("stmt.bindLong($L, value.is$L() ? 1: 0);\n",
                        fieldCount + 1, DbProcessorUtils.capitalize(field.getSimpleName()));
            }else if(INSERT_BIND_DOUBLE.contains(fieldType.getKind())) {
                bindCodeBlock.add("stmt.bindDouble($L, value.get$L());\n",
                        fieldCount + 1, DbProcessorUtils.capitalize(field.getSimpleName()));
            }

            fieldCount++;
        }

        insertQueryCodeBlock.add(") VALUES ($L)\";\n", paramSection);

        String insertQueryBlockStr = insertQueryCodeBlock.build().toString();
        Integer insertQueryVariablePostfix = adapterPostfixMap.get(insertQueryBlockStr);
        String entityInsertionAdapterVarName = "_entityInsertionAdapter" + insertQueryVariablePostfix;
        if(insertQueryVariablePostfix == null) {
            entityInsertionAdapterVarName = "_entityInsertionAdapter" + adapterPostfixMap.size();
            TypeSpec.Builder anonymousClassSpec = TypeSpec.anonymousClassBuilder(roomDbVarName)
                    .addSuperinterface(entityInsertionAdapterTypeName)
                    .addMethod(MethodSpec.methodBuilder("createQuery")
                            .addModifiers(Modifier.PUBLIC)
                            .returns(String.class)
                            .addAnnotation(Override.class)
                            .addCode(insertQueryCodeBlock.build())
                            .build())
                    .addMethod(MethodSpec.methodBuilder("bind")
                            .addAnnotation(Override.class)
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(ClassName.get("android.arch.persistence.db",
                                    "SupportSQLiteStatement"), "stmt")
                            .addParameter(ClassName.get(methodInfo.resolveEntityParameterComponentType()),
                                    "value")
                            .addCode(bindCodeBlock.build())
                            .build());

            constructorCodeBlock.add("// code block for $L \n", insertQueryBlockStr);
            constructorCodeBlock.add("$L = $L;\n", entityInsertionAdapterVarName,
                    anonymousClassSpec.build());
            daoTypeBuilder.addField(entityInsertionAdapterTypeName, entityInsertionAdapterVarName,
                    Modifier.PRIVATE);
            adapterPostfixMap.put(insertQueryBlockStr, adapterPostfixMap.size());
        }


        if(methodInfo.isAsyncMethod()) {
            codeBlock.beginControlFlow("dbExecutor.execute(() -> ");
        }

        if(!isVoid(methodInfo.resolveResultType())) {
            codeBlock.add("$T _result = $L;\n", methodInfo.resolveResultType(),
                    defaultValue(methodInfo.resolveResultType()));
        }

        codeBlock.add("$L.beginTransaction();\n", roomDbVarName)
                .add("$1T _dbWithSyncableInsertLock = ($1T)_dbManager;\n",
                        UmDbWithSyncableInsertLock.class)
                .beginControlFlow("try")
                .add("_dbWithSyncableInsertLock.lockSyncableInserts();\n")
                .add("$L.insert($L);\n", entityInsertionAdapterVarName,
                        methodInfo.getEntityParameterElement().getSimpleName());

        ClassName syncablePkUtilsClsName = ClassName.get("com.ustadmobile.lib.database",
                "SyncablePkUtilsAndroid");
        codeBlock.add("$1T _syncablePkDbMgr = ($1T)_dbManager;\n", ClassName.get(
                "com.ustadmobile.lib.database", "UmRoomDbManagerWithSyncablePk"));

        if(!isVoid(methodInfo.resolveResultType())) {
            codeBlock.add("_result = ");
            if(methodInfo.hasListResultType()) {
                codeBlock.add("$T.getGeneratedKeysAsList(_roomDb, _syncablePkDbMgr);\n",
                        syncablePkUtilsClsName);
            }else if(methodInfo.hasArrayResultType()) {
                TypeMirror compType = ((ArrayType)methodInfo.resolveResultType()).getComponentType();
                if(compType.getKind().equals(TypeKind.LONG)) {
                    codeBlock.add("$T.getGeneratedKeysAsArray(_roomDb, _syncablePkDbMgr);\n",
                            syncablePkUtilsClsName);
                }else {
                    codeBlock.add("$T.getGeneratedKeysAsBoxedArray(_roomDb, _syncablePkDbMgr);\n",
                            syncablePkUtilsClsName);
                }
            }else {
                codeBlock.add("$T.getGeneratedKey(_roomDb, _syncablePkDbMgr);\n",
                        syncablePkUtilsClsName);
            }
        }else {
            codeBlock.add("$T.deleteLastGeneratedPks(_syncablePkDbMgr);\n",
                    syncablePkUtilsClsName);
        }

        codeBlock.add("$L.setTransactionSuccessful();\n", roomDbVarName);

        codeBlock.nextControlFlow("finally")
                .add("_dbWithSyncableInsertLock.unlockSyncableInserts();\n")
                .add("$L.endTransaction();\n", roomDbVarName)
                .endControlFlow();

        if(!isVoid(daoMethod.getReturnType())){
            codeBlock.add("return _result;\n");
        }else if(methodInfo.isAsyncMethod()) {
            String asyncParamName = daoMethod.getParameters().get(methodInfo.getAsyncParamIndex())
                    .getSimpleName().toString();
            if(isVoid(methodInfo.resolveResultType())) {
                codeBlock.add("$T.onSuccessIfNotNull($L, null);\n",
                        UmCallbackUtil.class, asyncParamName);
            }else {
                codeBlock.add("$T.onSuccessIfNotNull($L, _result);\n",
                        UmCallbackUtil.class, asyncParamName);
            }
        }

        if(methodInfo.isAsyncMethod()) {
            codeBlock.endControlFlow(")");
        }

        methodBuilder.addCode(codeBlock.build());
        return methodBuilder.build();
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
    @Override
    public MethodSpec generateQueryMethod(String querySql,
                                                   ExecutableElement daoMethod,
                                                   TypeElement daoType,
                                                   TypeElement dbType,
                                                   TypeSpec.Builder daoClassBuilder) {
        //check for livedata return types
        //Class returnType = daoMethod.getReturnType();
        TypeMirror returnType = daoMethod.getReturnType();
        Element returnTypeElement = processingEnv.getTypeUtils().asElement(returnType);
        TypeElement umLiveDataTypeElement = processingEnv.getElementUtils().getTypeElement(
                UMDB_CORE_PKG_NAME + ".UmLiveData");
        DaoMethodInfo daoMethodInfo = new DaoMethodInfo(daoMethod, daoType, processingEnv);

        if(daoMethodInfo.isQueryUpdateWithLastChangedByField(dbType)) {
            querySql = addSetLastModifiedByToUpdateSql(querySql, daoMethod, daoType, dbType);
        }

        ClassName queryClassName = ClassName.get(ROOM_PKG_NAME, "Query");
        AnnotationSpec.Builder querySpec = AnnotationSpec.builder(queryClassName)
                .addMember("value", CodeBlock.builder().add("$S", querySql).build());



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

        return retMethod.build();

    }

    private MethodSpec.Builder generateClearAllMethod(ExecutableElement daoMethod, TypeElement dbType) {
        MethodSpec.Builder methodSpec = MethodSpec.overriding(daoMethod);
        CodeBlock.Builder codeBlock = CodeBlock.builder().add("_roomDb.clearAllTables();\n");
        for(TypeElement entityTypeEl : DbProcessorUtils.findEntityTypes(dbType, processingEnv)) {
            if(DbProcessorUtils.entityHasChangeSequenceNumbers(entityTypeEl, processingEnv)) {
                int tableId = entityTypeEl.getAnnotation(UmEntity.class).tableId();
                codeBlock.add("getSyncStatusDao().insert(new $T($L));\n", SyncStatus.class,
                        tableId);
                codeBlock.add("getSyncablePrimaryKeyDao().insert(new $T($L, 1));\n",
                        SyncablePrimaryKey.class, tableId);
            }

        }

        TypeMirror syncableDbType = processingEnv.getElementUtils().getTypeElement(
                UmSyncableDatabase.class.getName()).asType();
        if(processingEnv.getTypeUtils().isAssignable(dbType.asType(), syncableDbType)) {
            codeBlock.add("getSyncablePrimaryKeyDao().insertDeviceBits($T.newRandomInstance());\n",
                    SyncDeviceBits.class);
            codeBlock.add("invalidateDeviceBits();\n");
        }

        methodSpec.addCode(codeBlock.build());
        return methodSpec;
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
