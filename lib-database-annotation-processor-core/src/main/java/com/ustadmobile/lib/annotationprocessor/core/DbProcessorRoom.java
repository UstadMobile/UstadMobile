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
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmDelete;
import com.ustadmobile.lib.database.annotation.UmInsert;
import com.ustadmobile.lib.database.annotation.UmNamedParameter;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmTransaction;
import com.ustadmobile.lib.database.annotation.UmUpdate;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_ROOM_OUTPUT;


@SupportedOptions({OPT_ROOM_OUTPUT})
public class DbProcessorRoom{

    private ProcessingEnvironment processingEnv;

    private Messager messager;

    private Filer filer;

    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        this.processingEnv = processingEnvironment;
        filer = processingEnvironment.getFiler();
        messager = processingEnvironment.getMessager();
    }

    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        String destinationPath = processingEnv.getOptions().get(OPT_ROOM_OUTPUT);
        if(destinationPath == null)
            return true;

        File destinationDir = new File(destinationPath);

        for(Element dbClassElement : roundEnvironment.getElementsAnnotatedWith(UmDatabase.class)) {
            try {
                processDbClass((TypeElement)dbClassElement, roundEnvironment, destinationDir);
            }catch(IOException ioe) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Exception processing "
                    + ioe.getMessage());
            }
        }

        for(Element daoClassElement : roundEnvironment.getElementsAnnotatedWith(UmDao.class)) {
            try {
                processDbDao((TypeElement)daoClassElement, destinationDir);
            }catch(Exception e) {
                e.printStackTrace();
            }
        }


        messager.printMessage(Diagnostic.Kind.NOTE, "running room processor");
        return true;
    }

    public static final String SUFFIX_ROOM_DAO = "_RoomDao";

    public static final String SUFFIX_ROOM_DBMANAGER = "_RoomDbManager";

    private static final String ROOM_PKG_NAME =  "android.arch.persistence.room";

    public void processDbClass(TypeElement dbType, RoundEnvironment env, File destinationDir) throws IOException {
        String roomDbClassName = dbType.getSimpleName() + "_RoomDb";
        String roomDbManagerClassName = dbType.getSimpleName() + SUFFIX_ROOM_DBMANAGER;
        TypeSpec.Builder roomDbTypeSpec = TypeSpec.classBuilder(roomDbClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .superclass(ClassName.get(ROOM_PKG_NAME, "RoomDatabase"))
                .addJavadoc("Generated code - DO NOT EDIT!");
        String packageName = processingEnv.getElementUtils().getPackageOf(dbType).getQualifiedName().toString();


        TypeSpec.Builder dbManagerImplSpec = TypeSpec.classBuilder(roomDbManagerClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addField(ClassName.get(ExecutorService.class), "dbExecutor", Modifier.PRIVATE)
                .addField(ClassName.get("android.content", "Context"),
                        "context",Modifier.PRIVATE)
                .addField(ClassName.get(packageName,
                        roomDbClassName), "_roomDb",
                        Modifier.PRIVATE)
                .addJavadoc("Generated code - DO NOT EDIT!");

        TypeSpec.Builder factoryClassSpec = TypeSpec.classBuilder(dbType.getSimpleName() + "_Factory")
                .addModifiers(Modifier.PUBLIC)
                .addField(ClassName.get(dbType), "instance", Modifier.PRIVATE, Modifier.STATIC)
                .addMethod(MethodSpec.methodBuilder("make" + dbType.getSimpleName())
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .addParameter(ClassName.get(Object.class), "context")
                        .returns(ClassName.get(dbType))
                        .addCode(CodeBlock.builder().add("if(instance == null) \n")
                                .add("\tinstance = new $L(context);\n", roomDbManagerClassName)
                                .add("return instance;\n").build()).build())
                .addJavadoc("Generated code - DO NOT EDIT!");

        dbManagerImplSpec.addMethod(MethodSpec.constructorBuilder()
                    .addParameter(ClassName.get(Object.class), "context")
                    .addModifiers(Modifier.PUBLIC)
                    .addCode("this.context = (Context)context;\n")
                    .addCode("this.dbExecutor = $T.newCachedThreadPool();\n", Executors.class)
                    .addCode("_roomDb = $T.databaseBuilder(this.context, " + roomDbClassName +
                            ".class, " + "\"appdbname\").build();\n", ClassName.get("android.arch.persistence.room", "Room"))
                .build());


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


        JavaFile.builder(packageName, roomDbTypeSpec.build()).build().writeTo(destinationDir);
        JavaFile.builder(packageName, dbManagerImplSpec.build()).build().writeTo(destinationDir);
        JavaFile.builder(packageName, factoryClassSpec.build()).build().writeTo(destinationDir);
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
                        .add("if($L == null){\n", daoFieldName)
                        .add("\t $L = _roomDb.$L();\n", daoFieldName, daoMethodName)
                        .add("\t $L.setExecutor(dbExecutor);\n", daoFieldName)
                        .add("}\n")
                        .add("return $L;\n", daoFieldName).build()).build());

    }


    public void processDbDao(TypeElement daoClass, File destinationDir) throws IOException, NoSuchMethodException{
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
        for(Element subElement : daoClass.getEnclosedElements()) {
            if(subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement daoMethod = (ExecutableElement) subElement;
            MethodSpec.Builder methodBuilder = null;

            if(daoMethod.getAnnotation(UmInsert.class) != null) {
                UmInsert umInsert = daoMethod.getAnnotation(UmInsert.class);
                AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Insert"))
                        .addMember("onConflict", ""+umInsert.onConflict()).build();
                methodBuilder = generateAnnotatedMethod(daoMethod, annotation, roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmDelete.class) != null) {
                methodBuilder = generateAnnotatedMethod(daoMethod,
                        AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Delete")).build(),
                        roomDaoClassSpec);
            }else if(daoMethod.getAnnotation(UmUpdate.class) != null) {
                methodBuilder = generateAnnotatedMethod(daoMethod,
                        AnnotationSpec.builder(ClassName.get(ROOM_PKG_NAME, "Update")).build(),
                        roomDaoClassSpec);
//            }else if(daoMethod.isAnnotationPresent(UmQuery.class)) {
//                methodBuilder = generateQueryMethod(daoMethod, intermediateDaoMethod, roomDaoClassSpec);
//            }else if(daoMethod.isAnnotationPresent(UmTransaction.class)) {
//                methodBuilder = generateTransactionWrapperMethod(daoMethod, intermediateDaoMethod,
//                        roomDaoClassSpec);
            }

            if(methodBuilder != null){
                roomDaoClassSpec.addMethod(methodBuilder.addAnnotation(Override.class).build());
            }
        }

        JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoClass).toString(),
                roomDaoClassSpec.build()).build().writeTo(destinationDir);
    }
//
//
//    private MethodSpec.Builder generateTransactionWrapperMethod(Method daoMethod,
//                                                                Method intermediateDaoMethod,
//                                                                TypeSpec.Builder roomDaoClassSpec) {
//        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(daoMethod.getName())
//                .addAnnotation(ClassName.get(ROOM_PKG_NAME, "Transaction"))
//                .returns(TypeName.get(daoMethod.getGenericReturnType()));
//        addModifiersFromMethod(methodBuilder, daoMethod);
//        addNamedParametersToMethodBuilder(methodBuilder, intermediateDaoMethod);
//        methodBuilder.addCode(daoMethod.getGenericReturnType().equals(Void.TYPE) ?
//                        "super.$L$L;\n" : "return super.$L$L;\n",
//                daoMethod.getName(),
//                makeNamedParameterMethodCall(intermediateDaoMethod.getParameters()));
//        return methodBuilder;
//    }
//
    private MethodSpec.Builder generateAnnotatedMethod(ExecutableElement daoMethod,
                                                       AnnotationSpec annotationSpec,
                                                       TypeSpec.Builder roomDaoClassSpec) {
        MethodSpec.Builder methodBuilder;

        List<? extends VariableElement> variableElementList = daoMethod.getParameters();
        List<Element> variableTypeElements = new ArrayList<>();
        for(VariableElement variableElement : variableElementList) {
            variableTypeElements.add(processingEnv.getTypeUtils().asElement(variableElement.asType()));
        }

        TypeElement umCallbackTypeElement = processingEnv.getElementUtils().getTypeElement(
                "com.ustadmobile.core.impl.UmCallback");


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


        addNamedParametersToMethodBuilder(roomInsertMethodBuilder, daoMethod, umCallbackTypeElement);

        addModifiersFromMethod(roomInsertMethodBuilder, daoMethod);

        if(!isAsyncMethod) {
            methodBuilder = roomInsertMethodBuilder;
        }else {
            roomDaoClassSpec.addMethod(roomInsertMethodBuilder.build());

            DeclaredType declaredType = (DeclaredType)variableElementList.get(asyncParamIndex).asType();
            methodBuilder = MethodSpec.methodBuilder(daoMethod.getSimpleName().toString())
                    .returns(TypeName.VOID);
            addNamedParametersToMethodBuilder(methodBuilder, daoMethod);
            addModifiersFromMethod(methodBuilder, daoMethod);
            String callbackParamName = variableElementList.get(asyncParamIndex).getSimpleName().toString();
            CodeBlock.Builder asyncInsert = CodeBlock.builder();
            asyncInsert.add("dbExecutor.execute(() -> ");
            ClassName umCallbackUtilClassName = ClassName.get("com.ustadmobile.core.impl", "UmCallbackUtil");
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


    private void addNamedParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
                                                   ExecutableElement method, Element... excludedTypes) {
        List<Element> excludedTypeList = Arrays.asList(excludedTypes);
        List<? extends VariableElement> variableElementList = method.getParameters();

        for(int i = 0; i < variableElementList.size(); i++) {
            TypeMirror variableType = variableElementList.get(i).asType();
            if(excludedTypeList.contains(processingEnv.getTypeUtils().asElement(variableType)))
                continue;

            methodBuilder.addParameter(TypeName.get(variableElementList.get(i).asType()),
                    variableElementList.get(i).getSimpleName().toString());
        }
    }

//    private void addNamedParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
//                                                   ExecutableElement method, TypeElement excludedTypes) {
//        List<TypeElement> excludedTypeList = Arrays.asList(excludedTypes);
//
//        List<? extends TypeParameterElement> paramTypeList = method.getTypeParameters();
//
//
//        TypeM
////        for(Parameter parameter: annotatedMethod.getParameters()) {
//        for(int i = 0; i < paramTypeList.size(); i++){
//            if(excludedTypeList.contains(paramTypeList.get(i))
//                continue;
//
//            UmNamedParameter namedParameter = parameter.getAnnotation(UmNamedParameter.class);
//            methodBuilder.addParameter(parameter.getParameterizedType(),
//                    namedParameter != null ? namedParameter.value() : parameter.getName());
//        }
//    }


    private TypeName convertToPrimitiveIfApplicable(TypeMirror type) {
        if(type.getKind().equals(TypeKind.VOID)) {
            return TypeName.VOID;
        }else if(type.getKind().equals(TypeKind.DECLARED)) {
            Element typeEl = processingEnv.getTypeUtils().asElement(type);
            if(processingEnv.getElementUtils().getPackageOf(typeEl).getQualifiedName().toString().equals("java.lang")) {
                String className = typeEl.getSimpleName().toString();
                if(className.equals("Long") || className.equals("Integer"))
                    return TypeName.get(processingEnv.getTypeUtils().unboxedType(type));
            }
        }

        return TypeName.get(type);
    }
//
//
//    private MethodSpec.Builder generateQueryMethod(Method daoMethod, Method intermediateMethod,
//                                                   TypeSpec.Builder daoClassBuilder) {
//        //check for livedata return types
//        Class returnType = daoMethod.getReturnType();
//        UmQuery umQuery = daoMethod.getAnnotation(UmQuery.class);
//        ClassName queryClassName = ClassName.get(ROOM_PKG_NAME, "Query");
//        AnnotationSpec.Builder querySpec = AnnotationSpec.builder(queryClassName)
//                .addMember("value", CodeBlock.builder().add("$S", umQuery.value()).build());
//
//        MethodSpec.Builder retMethod = MethodSpec.methodBuilder(daoMethod.getName())
//                .returns(ClassName.get(daoMethod.getGenericReturnType()));
//        addModifiersFromMethod(retMethod, intermediateMethod);
//
//
//        for(Parameter param : intermediateMethod.getParameters()) {
//            retMethod.addParameter(param.getParameterizedType(),
//                    param.getAnnotation(UmNamedParameter.class).value());
//        }
//
//        if(returnType.equals(UmLiveData.class)) {
//            ParameterizedType type = (ParameterizedType)daoMethod.getGenericReturnType();
//            ParameterizedTypeName liveDataReturnType = ParameterizedTypeName.get(
//                    ClassName.get("android.arch.lifecycle", "LiveData"),
//                    ClassName.get(type.getActualTypeArguments()[0]));
//            String liveDataMethodName = daoMethod.getName() + "_RoomLive";
//            MethodSpec.Builder roomLiveDataBuilder = MethodSpec.methodBuilder(liveDataMethodName)
//                    .addAnnotation(querySpec.build())
//                    .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
//                    .returns(liveDataReturnType);
//
//            CodeBlock.Builder retMethodCodeBlock = CodeBlock.builder().add(
//                    "return new $T<>($L",
//                    ClassName.get("com.ustadmobile.port.android.db.dao",
//                            "UmLiveDataAndroid"), liveDataMethodName);
//            Parameter[] parameters = intermediateMethod.getParameters();
//
//            roomLiveDataBuilder = addNamedParamsToMethodSpec(parameters, roomLiveDataBuilder);
//            retMethodCodeBlock.add(makeNamedParameterMethodCall(parameters)).add(");\n");
//
//            retMethod.addCode(retMethodCodeBlock.build());
//            daoClassBuilder.addMethod(roomLiveDataBuilder.build());
//        }else if(returnType.equals(UmProvider.class)) {
//            ParameterizedType type = (ParameterizedType)daoMethod.getGenericReturnType();
//            ParameterizedTypeName factoryReturnType = ParameterizedTypeName.get(
//                    ClassName.get("android.arch.paging.DataSource", "Factory"),
//                    ClassName.get(Integer.class), ClassName.get(type.getActualTypeArguments()[0]));
//            String factoryMethodName = daoMethod.getName() + "_RoomFactory";
//            CodeBlock.Builder retMethodCodeBlock = CodeBlock.builder()
//                    .add("return () -> $L$L;\n",
//                            factoryMethodName,
//                            makeNamedParameterMethodCall(intermediateMethod.getParameters()));
//            retMethod.addCode(retMethodCodeBlock.build());
//
//            MethodSpec.Builder factoryMethodBuilder = MethodSpec.methodBuilder(factoryMethodName)
//                    .addAnnotation(querySpec.build())
//                    .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
//                    .returns(factoryReturnType);
//            addNamedParamsToMethodSpec(intermediateMethod.getParameters(), factoryMethodBuilder);
//            daoClassBuilder.addMethod(factoryMethodBuilder.build());
//        }else if(Arrays.asList(daoMethod.getParameterTypes()).contains(UmCallback.class)) {
//            //this is an async method, run it on the executor
//            String roomMethodName = daoMethod.getName() + "_Room";
//
//            //find the callback
//            int callbackParamNum = Arrays.asList(daoMethod.getParameterTypes()).indexOf(UmCallback.class);
//            Parameter callbackParam = intermediateMethod.getParameters()[callbackParamNum];
//            ParameterizedType roomMethodRetType = (ParameterizedType)callbackParam.getParameterizedType();
//
//            TypeName returnTypeName = convertToPrimitiveIfApplicable(
//                    roomMethodRetType.getActualTypeArguments()[0]);
//            MethodSpec.Builder roomMethodBuilder = MethodSpec.methodBuilder(roomMethodName)
//                    .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
//                    .addAnnotation(querySpec.build())
//                    .returns(returnTypeName);
//
//            addNamedParametersToMethodBuilder(roomMethodBuilder, intermediateMethod,
//                    UmCallback.class);
//
//            daoClassBuilder.addMethod(roomMethodBuilder.build());
//
//            String callbackParamName = callbackParam.getAnnotation(UmNamedParameter.class).value();
//            if(!returnTypeName.equals(TypeName.VOID)) {
//                retMethod.addCode("dbExecutor.execute(() -> $T.onSuccessIfNotNull($L, $L$L));",
//                        ClassName.get(UmCallbackUtil.class),
//                        callbackParamName,
//                        roomMethodName,
//                        makeNamedParameterMethodCall(intermediateMethod.getParameters(),
//                                UmCallback.class));
//            }else {
//                retMethod.addCode("dbExecutor.execute(() -> { $L$L; $T.onSuccessIfNotNull($L, null); });",
//                        roomMethodName,
//                        makeNamedParameterMethodCall(intermediateMethod.getParameters(),
//                                UmCallback.class),
//                        ClassName.get(UmCallbackUtil.class),
//                        callbackParamName);
//            }
//        }else {
//            //this is just a simple override
//            retMethod.addAnnotation(querySpec.build())
//                    .addModifiers(Modifier.ABSTRACT);
//        }
//
//        return retMethod;
//
//    }
//

    private MethodSpec.Builder addModifiersFromMethod(MethodSpec.Builder methodBuilder, ExecutableElement method) {
        if(method.getModifiers().contains(Modifier.PUBLIC)){
            methodBuilder.addModifiers(Modifier.PUBLIC);
        }else if(method.getModifiers().contains(Modifier.PROTECTED)){
            methodBuilder.addModifiers(Modifier.PROTECTED);
        }

//        methodBuilder.addModifiers(method.getModifiers());
//        int methodModifers = method.getModifiers();
//        if(java.lang.reflect.Modifier.isPublic(methodModifers))
//            methodBuilder.addModifiers(Modifier.PUBLIC);
//        else if(java.lang.reflect.Modifier.isProtected(methodModifers))
//            methodBuilder.addModifiers(Modifier.PROTECTED);

        return methodBuilder;
    }
//
//
    private CodeBlock makeNamedParameterMethodCall(List<? extends VariableElement> parameters,
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
//
//    private MethodSpec.Builder addNamedParamsToMethodSpec(Parameter[] parameters,
//                                                          MethodSpec.Builder methodBuilder) {
//        for(Parameter param : parameters) {
//            methodBuilder.addParameter(ClassName.get(param.getParameterizedType()),
//                    param.getAnnotation(UmNamedParameter.class).value());
//        }
//
//        return methodBuilder;
//    }
//
//    private void addNamedParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
//                                                   ExecutableElement method, TypeElement excludedTypes) {
//        List<TypeElement> excludedTypeList = Arrays.asList(excludedTypes);
//
//        List<? extends TypeParameterElement> paramTypeList = method.getTypeParameters();
//
//
//        TypeM
////        for(Parameter parameter: annotatedMethod.getParameters()) {
//        for(int i = 0; i < paramTypeList.size(); i++){
//            if(excludedTypeList.contains(paramTypeList.get(i))
//                continue;
//
//            UmNamedParameter namedParameter = parameter.getAnnotation(UmNamedParameter.class);
//            methodBuilder.addParameter(parameter.getParameterizedType(),
//                    namedParameter != null ? namedParameter.value() : parameter.getName());
//        }
//    }
//
//    private void addNamedParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
//                                                   Method annotatedMethod) {
//        addNamedParametersToMethodBuilder(methodBuilder, annotatedMethod, null);
//    }
//
//
//    public void processAllDbClasses(File destinationDir) throws IOException, ClassNotFoundException {
//        Reflections reflections = new Reflections();
//        Set<Class<?>> annotatedClasses = reflections.getTypesAnnotatedWith(UmDatabase.class);
//        for(Class clazz : annotatedClasses) {
//            processDbClass(clazz, destinationDir);
//        }
//    }
//
//    public void processAllDaos(File destinationDir) throws IOException, ClassNotFoundException,
//            NoSuchMethodException {
//        Reflections reflections = new Reflections();
//        Set<Class<?>> annotatedDaos = reflections.getTypesAnnotatedWith(UmDao.class);
//        for(Class clazz : annotatedDaos) {
//            if(clazz.getDeclaredAnnotation(UmDao.class) != null){
//                // only those with it directly declared
//                Class daoIntermediateClass = Class.forName(clazz.getCanonicalName() + "_CoreIntermediate");
//                processDbDao(clazz, daoIntermediateClass, destinationDir);
//            }
//
//        }
//    }
//
//
//    public static void main(String args[]) {
//        Options cmdOptions = new Options();
//        Option destinationDirOption = new Option("d", "dest",true, "Source output destination dir");
//        cmdOptions.addOption(destinationDirOption);
//
//        try {
//            CommandLine cmdLine = new DefaultParser().parse(cmdOptions, args);
//            File destinationDir = new File(cmdLine.getOptionValue("dest"));
//            if(!destinationDir.exists()) {
//                boolean dirMade = destinationDir.mkdirs();
//                if(!dirMade) {
//                    System.err.println("WARNING: could not create dir " +
//                            destinationDir.getAbsolutePath());
//                }
//            }
//            DbProcessorRoom dbProcessorRoom = new DbProcessorRoom();
//            dbProcessorRoom.processAllDbClasses(destinationDir);
//            dbProcessorRoom.processAllDaos(destinationDir);
//        }catch(ParseException e) {
//            HelpFormatter helpFormatter = new HelpFormatter();
//            System.err.println(e.getMessage());
//            helpFormatter.printHelp("DbProcessorRoom", cmdOptions);
//            System.exit(1);
//        }catch(IOException|ClassNotFoundException|NoSuchMethodException e) {
//            System.err.println("Exception processing database for Room: " + e.getMessage());
//            e.printStackTrace();
//            System.exit(2);
//        }
//    }

}
