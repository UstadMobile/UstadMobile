package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.impl.UmCallbackResultOverrider;
import com.ustadmobile.lib.database.annotation.UmClearAll;
import com.ustadmobile.lib.database.annotation.UmDao;
import com.ustadmobile.lib.database.annotation.UmDbContext;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmQueryFindByPrimaryKey;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.database.annotation.UmSyncOutgoing;
import com.ustadmobile.lib.db.UmDbWithExecutor;
import com.ustadmobile.lib.db.retrofit.RetrofitUmCallbackAdapter;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_RETROFIT_OUTPUT;

public class DbProcessorRetrofitRepository extends AbstractDbProcessor {

    public static final String POSTFIX_RETROFIT_INTERFACE = "_Retrofit";

    public static final String POSTFIX_REPOSITORY_DAO = "_RetrofitRepository";

    public static final String POSTFIX_REPOSITORY_DB = "_DbRetrofitRepository";

    public DbProcessorRetrofitRepository() {
        setOutputDirOpt(OPT_RETROFIT_OUTPUT);
    }


    @Override
    public void processDbClass(TypeElement dbType, String destination) throws IOException {
        TypeSpec.Builder dbRepoBuilder = TypeSpec.classBuilder(dbType.getSimpleName() +
                POSTFIX_REPOSITORY_DB)
                .superclass(ClassName.get(dbType))
                .addSuperinterface(UmRepositoryDb.class)
                .addModifiers(Modifier.PUBLIC)
                .addField(Retrofit.class, "_retrofit", Modifier.PRIVATE)
                .addField(ClassName.get(dbType), "_db", Modifier.PRIVATE)
                .addField(String.class, "_auth", Modifier.PRIVATE)
                .addField(String.class, "_baseUrl", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(dbType), "_db")
                    .addParameter(String.class, "_baseUrl")
                    .addParameter(String.class, "_auth")
                    .addCode(CodeBlock.builder()
                            .add("this._db = _db;\n")
                            .add("this._auth = _auth;\n")
                            .add("this._baseUrl = _baseUrl;\n")
                            .add("this._retrofit = new $T.Builder()" +
                                    ".addConverterFactory($T.create())" +
                                    ".addConverterFactory($T.create())" +
                                    ".baseUrl(_baseUrl)" +
                                    ".build();\n", Retrofit.class,
                                        ScalarsConverterFactory.class,
                                        GsonConverterFactory.class).build())
                    .build())
                .addMethod(MethodSpec.methodBuilder("getBaseUrl")
                        .addCode("return _baseUrl;\n")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addAnnotation(Override.class)
                    .build())
                .addMethod(MethodSpec.methodBuilder("getAuth")
                        .addCode("return _auth;\n")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(String.class)
                        .addAnnotation(Override.class)
                    .build())
                .addMethod(MethodSpec.methodBuilder("getDatabase")
                        .addCode("return _db;\n")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Object.class)
                        .addAnnotation(Override.class)
                    .build());

        for(ExecutableElement subElement : findMethodsToImplement(dbType)) {
            if (subElement.getAnnotation(UmDbContext.class) != null) {
                dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                        .addCode("return _db.$L();\n", subElement.getSimpleName()).build());
            }else if(subElement.getAnnotation(UmClearAll.class) != null) {
                dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                        .addCode("throw new $T($S);\n",
                                RuntimeException.class,
                                "Cannot use repository to clear database!")
                        .build());
            }else if(subElement.getAnnotation(UmRepository.class) != null) {
                dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                        .addCode("throw new RuntimeException($S);\n",
                                "Cannot get a repository for a repository")
                        .build());
            }else {
                TypeMirror retType = subElement.getReturnType();
                if(!retType.getKind().equals(TypeKind.DECLARED)
                        || processingEnv.getTypeUtils().asElement(retType)
                            .getAnnotation(UmDao.class) == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, formatMethodForErrorMessage(
                            subElement, dbType) + " return type is not a class annotated with UmDao",
                            subElement);
                    continue;
                }

                TypeElement daoTypeEl = (TypeElement)processingEnv.getTypeUtils().asElement(retType);

                if(daoTypeEl.getAnnotation(UmRepository.class) == null) {
                    dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                            .addCode("System.err.println($S);\n",
                                    "WARNING: Attempt to access repository getter for DAO " +
                                            "not annotated with @UmRepository")
                            .addCode("(new Throwable()).printStackTrace();\n")
                            .addCode("return null;\n")
                            .build());
                    continue;
                }

                PackageElement daoPackageEl = processingEnv.getElementUtils().getPackageOf(daoTypeEl);
                String retrofitDaoName = daoTypeEl.getSimpleName() + POSTFIX_REPOSITORY_DAO;
                String daoFieldName = "_" + daoTypeEl.getSimpleName();
                ClassName repoDaoClassName = ClassName.get(daoPackageEl.getQualifiedName().toString(),
                        retrofitDaoName);
                dbRepoBuilder.addField(repoDaoClassName, daoFieldName);
                dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                        .addCode(CodeBlock.builder()
                            .beginControlFlow("if($L == null)", daoFieldName)
                                .add("$L = new $T(_retrofit, _db, _db.$L(), this);\n",
                                        daoFieldName, repoDaoClassName, subElement.getSimpleName())
                            .endControlFlow()
                            .add("return $L;\n", daoFieldName)
                            .build())
                        .build());
            }
        }


        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(dbType);
        JavaFile repoJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                    dbRepoBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(repoJavaFile, destination);
    }

    @Override
    public void processDbDao(TypeElement daoType, TypeElement dbType, String destination) throws IOException {
        if(daoType.getAnnotation(UmRepository.class) == null)
            return;

        List<Element> restAccessibleMethods = findRestEnabledMethods(daoType);
        String retrofitInterfaceName = daoType.getSimpleName() + POSTFIX_RETROFIT_INTERFACE;
        TypeSpec.Builder retrofitBuilder = TypeSpec.interfaceBuilder(retrofitInterfaceName)
                .addModifiers(Modifier.PUBLIC);

        for(Element annotatedElement: restAccessibleMethods) {
            ExecutableElement method = (ExecutableElement)annotatedElement;
            DaoMethodInfo methodInfo = new DaoMethodInfo(method, daoType, processingEnv);
            Class<? extends Annotation> methodAnnotation =
                    DbProcessorUtils.getNonQueryParamCount(method, processingEnv,
                            umCallbackTypeElement) > 0  ? POST.class : GET.class;
            MethodSpec.Builder methodBuilder = MethodSpec
                    .methodBuilder(method.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addAnnotation(AnnotationSpec.builder(methodAnnotation)
                            .addMember("value", "\"$L/$L\"", daoType.getSimpleName(),
                                    method.getSimpleName()).build());
            addJaxWsParameters(method, daoType, methodBuilder, Query.class, Body.class);
            TypeName resultTypeName = TypeName.get(methodInfo.resolveResultEntityType());
            if(resultTypeName.isPrimitive()) {
                resultTypeName = resultTypeName.box();
            }else if(resultTypeName.equals(TypeName.VOID)) {
                resultTypeName = ClassName.get(Void.class);
            }

            methodBuilder.returns(ParameterizedTypeName.get(ClassName.get(Call.class),
                    resultTypeName));
            retrofitBuilder.addMethod(methodBuilder.build());
        }

        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(daoType);
        JavaFile databaseJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                retrofitBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(databaseJavaFile, destination);

        //now generate the repository
        TypeName retrofitTypeName = ClassName.get(packageElement.getQualifiedName().toString(),
                retrofitInterfaceName);

        TypeSpec.Builder repoBuilder = TypeSpec.classBuilder(daoType.getSimpleName() +
                POSTFIX_REPOSITORY_DAO)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(daoType))
                .addField(retrofitTypeName, "_webService", Modifier.PRIVATE)
                .addField(ClassName.get(dbType), "_db", Modifier.PRIVATE)
                .addField(ClassName.get(daoType), "_dao", Modifier.PRIVATE)
                .addField(ClassName.get(dbType), "_repo", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Retrofit.class, "_retrofit")
                    .addParameter(ClassName.get(dbType), "_db")
                    .addParameter(ClassName.get(daoType), "_dao")
                    .addParameter(ClassName.get(dbType), "_repo")
                    .addCode(CodeBlock.builder()
                            .add("this._webService = _retrofit.create($L.class);\n",
                                    retrofitInterfaceName)
                            .add("this._db = _db;\n")
                            .add("this._dao = _dao;\n")
                            .add("this._repo = _repo;\n").build())
                    .build());

        List<ExecutableElement> repoMethodsToImplement = findMethodsToImplement(daoType);

        for(ExecutableElement repoMethod : repoMethodsToImplement) {
            MethodSpec.Builder methodBuilder = overrideAndResolve(repoMethod, daoType, processingEnv);
            UmRepository repositoryAnnotation = repoMethod.getAnnotation(UmRepository.class);
            DaoMethodInfo methodInfo = new DaoMethodInfo(repoMethod, daoType, processingEnv);
            int repoMethodMode = -1;
            CodeBlock.Builder codeBlock = CodeBlock.builder();

            if(repositoryAnnotation != null) {
                repoMethodMode = repositoryAnnotation.delegateType();
            }else {
                if(methodInfo.isUpdateOrInsert()) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType
                            .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmQuery.class) != null
                        || repoMethod.getAnnotation(UmQueryFindByPrimaryKey.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType
                            .DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmSyncIncoming.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType
                            .DELEGATE_TO_WEBSERVICE;
                }else if(repoMethod.getAnnotation(UmSyncOutgoing.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType
                            .DELEGATE_TO_DAO;
                }
            }

            boolean runOnExecutor = methodInfo.isAsyncMethod()
                    && (repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO
                    || repoMethodMode == UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO);

            if(repoMethodMode == -1)
                continue;


            if(runOnExecutor) {
                codeBlock.beginControlFlow("(($T)_db).execute(() ->",
                        UmDbWithExecutor.class);
            }

            codeBlock.add("$1T _syncableDb = ($1T)_db;\n", UmSyncableDatabase.class);


            if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO) {
                codeBlock.add(generateIncrementChangeSeqNumsCodeBlock(methodInfo.resolveEntityParameterType(),
                        methodInfo.getEntityParameterElement().getSimpleName().toString(),
                        "_db", "_syncableDb", repoMethod, daoType));
            }

            if(methodInfo.isInsertWithAutoSyncPrimaryKey()) {
                codeBlock.add(generateSetSyncablePrimaryKey(repoMethod, daoType, processingEnv,
                        "_db", "_syncableDb", "_syncablePkResult"));
            }

            if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO
                    || repoMethodMode == UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO) {

                //if this is an insert method, with an autosync primary key, and we return the
                // result, we need to substitute the callback parameter with a wrapper so it returns
                // the syncable primary key just created.
                if(methodInfo.isAsyncMethod()
                        && methodInfo.isInsertWithAutoSyncPrimaryKey()
                        && !isVoid(methodInfo.resolveResultType())) {
                    codeBlock.add("_dao.$L(", repoMethod.getSimpleName());
                    boolean commaRequired = false;
                    for(VariableElement varEl : repoMethod.getParameters()) {
                        if(commaRequired)
                            codeBlock.add(", ");
                        if(!umCallbackTypeElement.equals(processingEnv.getTypeUtils()
                                .asElement(varEl.asType()))){
                            codeBlock.add(varEl.getSimpleName().toString());
                        }else {
                            codeBlock.add("new $T($L, _syncablePkResult)",
                                    UmCallbackResultOverrider.class,
                                    varEl.getSimpleName());
                        }

                        commaRequired = true;
                    }
                    codeBlock.add(");\n");

                }else {
                    if(!methodInfo.isInsertWithAutoSyncPrimaryKey()
                            && !repoMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
                        codeBlock.add("return ");
                    }

                    codeBlock.add("_dao.$L$L;\n", repoMethod.getSimpleName(),
                            makeNamedParameterMethodCall(repoMethod.getParameters()));

                    if(methodInfo.isInsertWithAutoSyncPrimaryKey()
                            && !repoMethod.getReturnType().getKind().equals(TypeKind.VOID)){
                        codeBlock.add("return _syncablePkResult;\n");
                    }
                }
            }else if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .DELEGATE_TO_WEBSERVICE) {
                codeBlock.add("$T<$T> _call = _webService.$L$L;\n", Call.class,
                        methodInfo.resolveResultEntityType(),
                        repoMethod.getSimpleName().toString(),
                        makeNamedParameterMethodCall(repoMethod.getParameters(),
                                umCallbackTypeElement));

                if(methodInfo.isAsyncMethod()){
                    codeBlock.add("_call.enqueue(new $T<$T>($L));\n",
                            RetrofitUmCallbackAdapter.class,
                            methodInfo.resolveResultType(),
                            repoMethod.getParameters().get(methodInfo.getAsyncParamIndex())
                                    .getSimpleName());
                }else {
                    boolean isVoid = methodInfo.resolveResultType().getKind().equals(TypeKind.VOID);
                    codeBlock.beginControlFlow("try");
                    if(!isVoid) {
                        codeBlock.add("return _call.execute().body();\n");
                    }else {
                        codeBlock.add("_cal.execute();\n");
                    }
                    codeBlock.nextControlFlow("catch($T _ioe)", IOException.class)
                            .add("_ioe.printStackTrace();\n")
                            .endControlFlow();

                    if(!isVoid)
                        codeBlock.add("return $L;\n", defaultValue(methodInfo.resolveResultType()));
                }
            }

            if(runOnExecutor) {
                codeBlock.endControlFlow(")");
            }


            methodBuilder.addCode(codeBlock.build());
            repoBuilder.addMethod(methodBuilder.build());
        }


        JavaFile repoJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                repoBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(repoJavaFile, destination);
    }

}
