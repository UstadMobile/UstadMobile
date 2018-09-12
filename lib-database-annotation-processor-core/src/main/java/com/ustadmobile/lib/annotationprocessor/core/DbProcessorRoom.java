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
import com.ustadmobile.lib.database.annotation.UmUpdate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_ROOM_OUTPUT;

/**
 * This annotation processor will generate a Room Persistence annotated Database class, an
 * Factory to create an instance of each @UmDatabase annotated class, and a Room Persistence
 * annotated DAO.
 *
 */
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
                processDbClass((TypeElement)dbClassElement, destinationDir);
            }catch(IOException ioe) {
                messager.printMessage(Diagnostic.Kind.ERROR, "IOException processing DB "
                    + ioe.getMessage());
            }
        }

        for(Element daoClassElement : roundEnvironment.getElementsAnnotatedWith(UmDao.class)) {
            try {
                processDbDao((TypeElement)daoClassElement, destinationDir);
            }catch(IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "IOException proceossing DAO "
                    + e.getMessage());
            }
        }


        messager.printMessage(Diagnostic.Kind.NOTE, "running room processor");
        return true;
    }

    public static final String SUFFIX_ROOM_DAO = "_RoomDao";

    public static final String SUFFIX_ROOM_DBMANAGER = "_RoomDbManager";

    private static final String ROOM_PKG_NAME =  "android.arch.persistence.room";

    private static final String UMDB_CORE_PKG_NAME = "com.ustadmobile.core.db";

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
     * @param destinationDir Root package directory (e.g. build/generated/source/umdbprocessor) to
     *                       place generated sources in.
     * @throws IOException If there are IO exceptions writing newly generated classes
     */
    public void processDbClass(TypeElement dbType,  File destinationDir) throws IOException {
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
            }else if(daoMethod.getAnnotation(UmClearAll.class) != null) {
                dbManagerImplSpec.addMethod(generateClearAllMethod(daoMethod).build());
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


    /**
     * Process the given DAO class and generate a child class with the appropriate room annotations.
     *
     * @param daoClass TypeElement representing the class with @UmDao annotation
     * @param destinationDir Root package directory for generated source output
     *
     * @throws IOException When there is an IO issue writing the generated output
     */
    private void processDbDao(TypeElement daoClass, File destinationDir) throws IOException {
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
            }else if(daoMethod.getAnnotation(UmQuery.class) != null) {
                methodBuilder = generateQueryMethod(daoMethod, roomDaoClassSpec);
            }

            if(methodBuilder != null){
                roomDaoClassSpec.addMethod(methodBuilder.addAnnotation(Override.class).build());
            }
        }

        JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoClass).toString(),
                roomDaoClassSpec.build()).build().writeTo(destinationDir);
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


        addParametersToMethodBuilder(roomInsertMethodBuilder, daoMethod, umCallbackTypeElement);

        addAccessModifiersFromMethod(roomInsertMethodBuilder, daoMethod);

        if(!isAsyncMethod) {
            methodBuilder = roomInsertMethodBuilder;
        }else {
            roomDaoClassSpec.addMethod(roomInsertMethodBuilder.build());

            DeclaredType declaredType = (DeclaredType)variableElementList.get(asyncParamIndex).asType();
            methodBuilder = MethodSpec.methodBuilder(daoMethod.getSimpleName().toString())
                    .returns(TypeName.VOID);
            addParametersToMethodBuilder(methodBuilder, daoMethod);
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
     * @param excludedTypes excluded TypeElements that should not be added to the given MethodSpec.Builder
     */
    private void addParametersToMethodBuilder(MethodSpec.Builder methodBuilder,
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
    private MethodSpec.Builder generateQueryMethod(ExecutableElement daoMethod,
                                                   TypeSpec.Builder daoClassBuilder) {
        //check for livedata return types
        //Class returnType = daoMethod.getReturnType();
        TypeMirror returnType = daoMethod.getReturnType();
        Element returnTypeElement = processingEnv.getTypeUtils().asElement(returnType);
        TypeElement umLiveDataTypeElement = processingEnv.getElementUtils().getTypeElement(
                UMDB_CORE_PKG_NAME + ".UmLiveData");

        UmQuery umQuery = daoMethod.getAnnotation(UmQuery.class);
        ClassName queryClassName = ClassName.get(ROOM_PKG_NAME, "Query");
        AnnotationSpec.Builder querySpec = AnnotationSpec.builder(queryClassName)
                .addMember("value", CodeBlock.builder().add("$S", umQuery.value()).build());

        MethodSpec.Builder retMethod = MethodSpec.methodBuilder(daoMethod.getSimpleName().toString())
                .returns(TypeName.get(returnType));
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

            addParametersToMethodBuilder(roomLiveDataBuilder, daoMethod);
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
            addParametersToMethodBuilder(factoryMethodBuilder, daoMethod);
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

            addParametersToMethodBuilder(roomMethodBuilder, daoMethod, umCallbackTypeElement);
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

}
