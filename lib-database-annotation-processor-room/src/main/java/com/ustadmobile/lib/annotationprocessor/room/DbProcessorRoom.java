package com.ustadmobile.lib.annotationprocessor.room;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.db.UmLiveData;
import com.ustadmobile.core.db.UmProvider;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.core.impl.UmCallbackUtil;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmNamedParameter;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.lang.model.element.Modifier;


public class DbProcessorRoom {

    public static final String SUFFIX_ROOM_DAO = "_RoomDao";

    public static final String SUFFIX_ROOM_DBMANAGER = "_RoomDbManager";

    private static final String ROOM_PKG_NAME =  "android.arch.persistence.room";

    public void processDbClass(Class clazz, File destinationDir) throws IOException,
            ClassNotFoundException {
        String roomDbClassName = clazz.getSimpleName() + "_RoomDb";
        String roomDbManagerClassName = clazz.getSimpleName() + SUFFIX_ROOM_DBMANAGER;
        TypeSpec.Builder roomDbTypeSpec = TypeSpec.classBuilder(roomDbClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(ClassName.get(ROOM_PKG_NAME, "RoomDatabase"));

        TypeSpec.Builder dbManagerImplSpec = TypeSpec.classBuilder(roomDbManagerClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(clazz)
                .addField(ClassName.get(ExecutorService.class), "dbExecutor", Modifier.PRIVATE)
                .addField(ClassName.get("android.content", "Context"),
                        "context",Modifier.PRIVATE)
                .addField(ClassName.get(clazz.getPackage().getName(), roomDbClassName), "_roomDb",
                        Modifier.PRIVATE);

        TypeSpec.Builder factoryClassSpec = TypeSpec.classBuilder(clazz.getSimpleName() + "_Factory")
                .addModifiers(Modifier.PUBLIC)
                .addField(ClassName.get(clazz), "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addMethod(MethodSpec.methodBuilder("make" + clazz.getSimpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(ClassName.get(Object.class), "context")
                        .returns(ClassName.get(clazz))
                        .addCode(CodeBlock.builder().add("if(instance == null) \n")
                                .add("\tinstance = new $L(context);\n", roomDbManagerClassName)
                                .add("return instance;\n").build()).build());

        dbManagerImplSpec.addMethod(MethodSpec.constructorBuilder()
                    .addParameter(ClassName.get(Object.class), "context")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("this.context = (Context)context;\n")
                    .addCode("this.dbExecutor = $T.newCachedThreadPool();\n", Executors.class)
                    .addCode("_roomDb = $T.databaseBuilder(this.context, " + roomDbClassName +
                            ".class, " + "\"appdbname\").build();\n", ClassName.get("android.arch.persistence.room", "Room"))
                .build());


        UmDatabase db = (UmDatabase)clazz.getAnnotation(UmDatabase.class);

        AnnotationSpec.Builder roomDbAnnotationSpec = AnnotationSpec
                .builder(ClassName.get("android.arch.persistence.room", "Database"))
                .addMember("version", String.valueOf(db.version()));
        CodeBlock.Builder roomEntitiesCodeblock = CodeBlock.builder().add("{");
        for(int i = 0; i < db.entities().length; i++) {
            roomEntitiesCodeblock.add("$T.class", db.entities()[i]);
            if(i < db.entities().length - 1)
                roomEntitiesCodeblock.add(", ");
        }
        roomEntitiesCodeblock.add("}");
        roomDbAnnotationSpec.addMember("entities", roomEntitiesCodeblock.build());
        roomDbTypeSpec.addAnnotation(roomDbAnnotationSpec.build());

        //now go through all methods that return DAO objects and create matching methods
        for(Method daoMethod : clazz.getMethods()) {
            if(daoMethod.isAnnotationPresent(UmDbContext.class)) {
                MethodSpec.Builder contextMethodBuilder = MethodSpec.methodBuilder(daoMethod.getName())
                        .returns(ClassName.get(Object.class))
                        .addCode("return this.context;\n")
                        .addAnnotation(Override.class);
                addModifiersFromMethod(contextMethodBuilder, daoMethod);
                dbManagerImplSpec.addMethod(contextMethodBuilder.build());
            }

            if(!daoMethod.getReturnType().isAnnotationPresent(UmDao.class))
                continue;

            addDaoMethod(roomDbTypeSpec, dbManagerImplSpec, daoMethod);
        }


        JavaFile.builder(clazz.getPackage().getName(), roomDbTypeSpec.build()).build().writeTo(destinationDir);
        JavaFile.builder(clazz.getPackage().getName(), dbManagerImplSpec.build()).build().writeTo(destinationDir);
        JavaFile.builder(clazz.getPackage().getName(), factoryClassSpec.build()).build().writeTo(destinationDir);
    }

    private void addDaoMethod(TypeSpec.Builder roomDbTypeSpec, TypeSpec.Builder dbManagerSpec,
                              Method daoMethod) throws ClassNotFoundException {
        Class coreDaoClass = daoMethod.getReturnType();
        String roomDaoClassName = coreDaoClass.getSimpleName() + SUFFIX_ROOM_DAO;
        ClassName roomDaoClassNameType = ClassName.get(coreDaoClass.getPackage().getName(), roomDaoClassName);

        roomDbTypeSpec.addMethod(MethodSpec.methodBuilder(daoMethod.getName())
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .returns(roomDaoClassNameType)
                .build());

        String daoFieldName = "_" + roomDaoClassName;
        dbManagerSpec.addField(FieldSpec.builder(roomDaoClassNameType, daoFieldName,
                Modifier.PRIVATE).build());
        dbManagerSpec.addMethod(MethodSpec.methodBuilder(daoMethod.getName())
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(daoMethod.getReturnType())
                .addCode(CodeBlock.builder()
                        .add("if($L == null){\n", daoFieldName)
                        .add("\t $L = _roomDb.$L();\n", daoFieldName, daoMethod.getName())
                        .add("\t $L.setExecutor(dbExecutor);\n", daoFieldName)
                        .add("}\n")
                        .add("return $L;\n", daoFieldName).build()).build());

    }

    public void processDbDao(Class daoClass, Class intermediateDaoClass, File destinationDir) throws IOException, NoSuchMethodException{
        String daoClassName = daoClass.getSimpleName() + SUFFIX_ROOM_DAO;
        TypeSpec.Builder roomDaoClassSpec = TypeSpec.classBuilder(daoClassName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .addAnnotation(ClassName.get(ROOM_PKG_NAME, "Dao"))
                .superclass(ClassName.get(daoClass))
                .addField(ClassName.get(ExecutorService.class), "dbExecutor", Modifier.PRIVATE);

        roomDaoClassSpec.addMethod(MethodSpec.methodBuilder("setExecutor")
                .addParameter(ClassName.get(ExecutorService.class), "dbExecutor")
                .addModifiers(Modifier.PUBLIC)
                .addCode("this.dbExecutor = dbExecutor;\n").build());

        //now generate methods for all query, insert, and delete methods
        for(Method intermediateDaoMethod : intermediateDaoClass.getDeclaredMethods()) {
            Method daoMethod = daoClass.getDeclaredMethod(intermediateDaoMethod.getName(),
                    intermediateDaoMethod.getParameterTypes());
            MethodSpec.Builder methodBuilder = null;

            if(daoMethod.isAnnotationPresent(UmInsert.class)) {
                UmInsert umInsert = daoMethod.getAnnotation(UmInsert.class);
                AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Insert"))
                        .addMember("onConflict", ""+umInsert.onConflict()).build();
                methodBuilder = generateAnnotatedMethod(daoMethod, intermediateDaoMethod,
                        annotation, roomDaoClassSpec);
            }else if(daoMethod.isAnnotationPresent(UmDelete.class)) {
                methodBuilder = generateAnnotatedMethod(daoMethod, intermediateDaoMethod,
                        AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Delete")).build(),
                        roomDaoClassSpec);
            }else if(daoMethod.isAnnotationPresent(UmUpdate.class)) {
                methodBuilder = generateAnnotatedMethod(daoMethod, intermediateDaoMethod,
                        AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Update")).build(),
                        roomDaoClassSpec);
            }else if(daoMethod.isAnnotationPresent(UmQuery.class)) {
                methodBuilder = generateQueryMethod(daoMethod, intermediateDaoMethod, roomDaoClassSpec);
            }else if(daoMethod.isAnnotationPresent(UmTransaction.class)) {
                methodBuilder = generateTransactionWrapperMethod(daoMethod, intermediateDaoMethod,
                        roomDaoClassSpec);
            }

            if(methodBuilder != null){
                roomDaoClassSpec.addMethod(methodBuilder.addAnnotation(Override.class).build());
            }
        }

        JavaFile.builder(daoClass.getPackage().getName(), roomDaoClassSpec.build()).build()
                .writeTo(destinationDir);
    }


    private MethodSpec.Builder generateTransactionWrapperMethod(Method daoMethod,
                                                                Method intermediateDaoMethod,
                                                                TypeSpec.Builder roomDaoClassSpec) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(daoMethod.getName())
                .addAnnotation(ClassName.get(ROOM_PKG_NAME, "Transaction"))
                .returns(TypeName.get(daoMethod.getGenericReturnType()));
        addModifiersFromMethod(methodBuilder, daoMethod);
        addNamedParametersToMethodBuilder(methodBuilder, intermediateDaoMethod);
        methodBuilder.addCode(daoMethod.getGenericReturnType().equals(Void.TYPE) ?
                        "super.$L$L;\n" : "return super.$L$L;\n",
                daoMethod.getName(),
                makeNamedParameterMethodCall(intermediateDaoMethod.getParameters()));
        return methodBuilder;
    }

    private MethodSpec.Builder generateAnnotatedMethod(Method daoMethod, Method intermediateDaoMethod,
                                                       AnnotationSpec annotationSpec,
                                                       TypeSpec.Builder roomDaoClassSpec) {
        MethodSpec.Builder methodBuilder;
        List<Class<?>> parameterTypeList = Arrays.asList(daoMethod.getParameterTypes());
        boolean isAsyncMethod = parameterTypeList.contains(UmCallback.class);
        String insertMethodName = isAsyncMethod ? daoMethod.getName() +"_Room"
                : daoMethod.getName();

        TypeName insertRetType;
        int asyncParamIndex = parameterTypeList.indexOf(UmCallback.class);
        if(isAsyncMethod) {
            ParameterizedType asyncParamaterizedType = (ParameterizedType)daoMethod
                    .getGenericParameterTypes()[asyncParamIndex];
            Type actualType = asyncParamaterizedType.getActualTypeArguments()[0];
            insertRetType = convertToPrimitiveIfApplicable(actualType);
        }else {
            insertRetType = TypeName.get(daoMethod.getGenericReturnType());
        }


        MethodSpec.Builder roomInsertMethodBuilder = MethodSpec.methodBuilder(insertMethodName)
                .addAnnotation(annotationSpec)
                .addModifiers(Modifier.ABSTRACT)
                .returns(insertRetType);
        addNamedParametersToMethodBuilder(roomInsertMethodBuilder, intermediateDaoMethod,
                UmCallback.class);
        addModifiersFromMethod(roomInsertMethodBuilder, daoMethod);

        if(!isAsyncMethod) {
            methodBuilder = roomInsertMethodBuilder;
        }else {
            roomDaoClassSpec.addMethod(roomInsertMethodBuilder.build());
            methodBuilder = MethodSpec.methodBuilder(daoMethod.getName())
                    .returns(daoMethod.getGenericReturnType());
            addNamedParametersToMethodBuilder(methodBuilder, intermediateDaoMethod);
            addModifiersFromMethod(methodBuilder, intermediateDaoMethod);
            String callbackParamName = intermediateDaoMethod.getParameters()[asyncParamIndex]
                    .getAnnotation(UmNamedParameter.class).value();
            CodeBlock.Builder asyncInsert = CodeBlock.builder();
            asyncInsert.add("dbExecutor.execute(() -> ");
            if(daoMethod.getReturnType().equals(Void.class)){
                asyncInsert.add("{$L$L; $T.onSuccessIfNotNull($L, null);}",
                        insertMethodName,
                        makeNamedParameterMethodCall(intermediateDaoMethod.getParameters(), UmCallback.class),
                        ClassName.get(UmCallbackUtil.class),
                        callbackParamName);
            }else {
                asyncInsert.add("$T.onSuccessIfNotNull($L, $L$L)",
                        ClassName.get(UmCallbackUtil.class),
                        callbackParamName, insertMethodName,
                        makeNamedParameterMethodCall(intermediateDaoMethod.getParameters(), UmCallback.class));
            }
            asyncInsert.add(");\n");
            methodBuilder.addCode(asyncInsert.build());
        }

        return methodBuilder;
    }

    private TypeName convertToPrimitiveIfApplicable(Type type) {
        if(type.equals(Void.class)) {
            return TypeName.VOID;
        }else if(type.equals(Integer.class)) {
            return TypeName.INT;
        }else if(type.equals(Long.class)) {
            return TypeName.LONG;
        }else {
            return TypeName.get(type);
        }
    }


    private MethodSpec.Builder generateQueryMethod(Method daoMethod, Method intermediateMethod,
                                                   TypeSpec.Builder daoClassBuilder) {
        //check for livedata return types
        Class returnType = daoMethod.getReturnType();
        UmQuery umQuery = daoMethod.getAnnotation(UmQuery.class);
        ClassName queryClassName = ClassName.get(ROOM_PKG_NAME, "Query");
        AnnotationSpec.Builder querySpec = AnnotationSpec.builder(queryClassName)
                .addMember("value", CodeBlock.builder().add("$S", umQuery.value()).build());

        MethodSpec.Builder retMethod = MethodSpec.methodBuilder(daoMethod.getName())
                .returns(ClassName.get(daoMethod.getGenericReturnType()));
        addModifiersFromMethod(retMethod, intermediateMethod);


        for(Parameter param : intermediateMethod.getParameters()) {
            retMethod.addParameter(param.getParameterizedType(),
                    param.getAnnotation(UmNamedParameter.class).value());
        }

        if(returnType.equals(UmLiveData.class)) {
            ParameterizedType type = (ParameterizedType)daoMethod.getGenericReturnType();
            ParameterizedTypeName liveDataReturnType = ParameterizedTypeName.get(
                    ClassName.get("android.arch.lifecycle", "LiveData"),
                    ClassName.get(type.getActualTypeArguments()[0]));
            String liveDataMethodName = daoMethod.getName() + "_RoomLive";
            MethodSpec.Builder roomLiveDataBuilder = MethodSpec.methodBuilder(liveDataMethodName)
                    .addAnnotation(querySpec.build())
                    .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
                    .returns(liveDataReturnType);

            CodeBlock.Builder retMethodCodeBlock = CodeBlock.builder().add(
                    "return new $T<>($L",
                    ClassName.get("com.ustadmobile.port.android.db.dao",
                            "UmLiveDataAndroid"), liveDataMethodName);
            Parameter[] parameters = intermediateMethod.getParameters();

            roomLiveDataBuilder = addNamedParamsToMethodSpec(parameters, roomLiveDataBuilder);
            retMethodCodeBlock.add(makeNamedParameterMethodCall(parameters)).add(");\n");

            retMethod.addCode(retMethodCodeBlock.build());
            daoClassBuilder.addMethod(roomLiveDataBuilder.build());
        }else if(returnType.equals(UmProvider.class)) {
            ParameterizedType type = (ParameterizedType)daoMethod.getGenericReturnType();
            ParameterizedTypeName factoryReturnType = ParameterizedTypeName.get(
                    ClassName.get("android.arch.paging.DataSource", "Factory"),
                    ClassName.get(Integer.class), ClassName.get(type.getActualTypeArguments()[0]));
            String factoryMethodName = daoMethod.getName() + "_RoomFactory";
            CodeBlock.Builder retMethodCodeBlock = CodeBlock.builder()
                    .add("return () -> $L$L;\n",
                            factoryMethodName,
                            makeNamedParameterMethodCall(intermediateMethod.getParameters()));
            retMethod.addCode(retMethodCodeBlock.build());

            MethodSpec.Builder factoryMethodBuilder = MethodSpec.methodBuilder(factoryMethodName)
                    .addAnnotation(querySpec.build())
                    .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
                    .returns(factoryReturnType);
            addNamedParamsToMethodSpec(intermediateMethod.getParameters(), factoryMethodBuilder);
            daoClassBuilder.addMethod(factoryMethodBuilder.build());
        }else if(Arrays.asList(daoMethod.getParameterTypes()).contains(UmCallback.class)) {
            //this is an async method, run it on the executor
            String roomMethodName = daoMethod.getName() + "_Room";

            //find the callback
            int callbackParamNum = Arrays.asList(daoMethod.getParameterTypes()).indexOf(UmCallback.class);
            Parameter callbackParam = intermediateMethod.getParameters()[callbackParamNum];
            ParameterizedType roomMethodRetType = (ParameterizedType)callbackParam.getParameterizedType();

            TypeName returnTypeName = convertToPrimitiveIfApplicable(
                    roomMethodRetType.getActualTypeArguments()[0]);
            MethodSpec.Builder roomMethodBuilder = MethodSpec.methodBuilder(roomMethodName)
                    .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                    .addAnnotation(querySpec.build())
                    .returns(returnTypeName);

            addNamedParametersToMethodBuilder(roomMethodBuilder, intermediateMethod,
                    UmCallback.class);

            daoClassBuilder.addMethod(roomMethodBuilder.build());

            String callbackParamName = callbackParam.getAnnotation(UmNamedParameter.class).value();
            if(!returnTypeName.equals(TypeName.VOID)) {
                retMethod.addCode("dbExecutor.execute(() -> $T.onSuccessIfNotNull($L, $L$L));",
                        ClassName.get(UmCallbackUtil.class),
                        callbackParamName,
                        roomMethodName,
                        makeNamedParameterMethodCall(intermediateMethod.getParameters(),
                                UmCallback.class));
            }else {
                retMethod.addCode("dbExecutor.execute(() -> { $L$L; $T.onSuccessIfNotNull($L, null); });",
                        roomMethodName,
                        makeNamedParameterMethodCall(intermediateMethod.getParameters(),
                                UmCallback.class),
                        ClassName.get(UmCallbackUtil.class),
                        callbackParamName);
            }
        }else {
            //this is just a simple override
            retMethod.addAnnotation(querySpec.build())
                    .addModifiers(Modifier.ABSTRACT);
        }

        return retMethod;

    }

    private MethodSpec.Builder addModifiersFromMethod(MethodSpec.Builder methodBuilder, Method method) {
        int methodModifers = method.getModifiers();
        if(java.lang.reflect.Modifier.isPublic(methodModifers))
            methodBuilder.addModifiers(Modifier.PUBLIC);
        else if(java.lang.reflect.Modifier.isProtected(methodModifers))
            methodBuilder.addModifiers(Modifier.PROTECTED);

        return methodBuilder;
    }


    private CodeBlock makeNamedParameterMethodCall(Parameter[] parameters, Class... excludedClasses) {
        List<Class> excludedClassList = Arrays.asList(excludedClasses);
        CodeBlock.Builder block = CodeBlock.builder().add("(");
        List<String> paramNames = new ArrayList<>();

        for(Parameter param : parameters) {
            if(excludedClasses != null && excludedClassList.contains(param.getType()))
                continue;

            paramNames.add(param.getAnnotation(UmNamedParameter.class).value());
        }

        block.add(String.join(", ", paramNames));

        return block.add(")").build();
    }

    private MethodSpec.Builder addNamedParamsToMethodSpec(Parameter[] parameters,
                                                          MethodSpec.Builder methodBuilder) {
        for(Parameter param : parameters) {
            methodBuilder.addParameter(ClassName.get(param.getParameterizedType()),
                    param.getAnnotation(UmNamedParameter.class).value());
        }

        return methodBuilder;
    }

    private void addNamedParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
                                                   Method annotatedMethod, Class... excludedTypes) {
        List<Class> excludedTypeList = Arrays.asList(excludedTypes);
        for(Parameter parameter: annotatedMethod.getParameters()) {
            if(excludedTypeList.contains(parameter.getType()))
                continue;

            UmNamedParameter namedParameter = parameter.getAnnotation(UmNamedParameter.class);
            methodBuilder.addParameter(parameter.getParameterizedType(),
                    namedParameter != null ? namedParameter.value() : parameter.getName());
        }
    }

//    private void addNamedParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
//                                                   Method annotatedMethod) {
//        addNamedParametersToMethodBuilder(methodBuilder, annotatedMethod, null);
//    }


    public void processAllDbClasses(File destinationDir) throws IOException, ClassNotFoundException {
        Reflections reflections = new Reflections();
        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(UmDatabase.class);
        for(Class clazz : annotatedClasses) {
            processDbClass(clazz, destinationDir);
        }
    }

    public void processAllDaos(File destinationDir) throws IOException, ClassNotFoundException,
            NoSuchMethodException {
        Reflections reflections = new Reflections();
        Set<Class<?>> annotatedDaos = reflections.getTypesAnnotatedWith(UmDao.class);
        for(Class clazz : annotatedDaos) {
            if(clazz.getDeclaredAnnotation(UmDao.class) != null){
                // only those with it directly declared
                Class daoIntermediateClass = Class.forName(clazz.getCanonicalName() + "_CoreIntermediate");
                processDbDao(clazz, daoIntermediateClass, destinationDir);
            }

        }
    }


    public static void main(String args[]) {
        Options cmdOptions = new Options();
        Option destinationDirOption = new Option("d", "dest",true, "Source output destination dir");
        cmdOptions.addOption(destinationDirOption);

        try {
            CommandLine cmdLine = new DefaultParser().parse(cmdOptions, args);
            File destinationDir = new File(cmdLine.getOptionValue("dest"));
            if(!destinationDir.exists()) {
                boolean dirMade = destinationDir.mkdirs();
                if(!dirMade) {
                    System.err.println("WARNING: could not create dir " +
                            destinationDir.getAbsolutePath());
                }
            }
            DbProcessorRoom dbProcessorRoom = new DbProcessorRoom();
            dbProcessorRoom.processAllDbClasses(destinationDir);
            dbProcessorRoom.processAllDaos(destinationDir);
        }catch(ParseException e) {
            HelpFormatter helpFormatter = new HelpFormatter();
            System.err.println(e.getMessage());
            helpFormatter.printHelp("DbProcessorRoom", cmdOptions);
            System.exit(1);
        }catch(IOException|ClassNotFoundException|NoSuchMethodException e) {
            System.err.println("Exception processing database for Room: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

}
