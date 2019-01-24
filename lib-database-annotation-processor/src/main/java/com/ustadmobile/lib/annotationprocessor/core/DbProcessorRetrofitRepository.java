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
import com.ustadmobile.lib.database.annotation.UmDbGetAttachment;
import com.ustadmobile.lib.database.annotation.UmDbSetAttachment;
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
import com.ustadmobile.lib.db.retrofit.RetrofitUmCallbackAdapter;
import com.ustadmobile.lib.db.sync.UmRepositoryDb;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
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
                            .add("$1T _client = new $1T.Builder().addInterceptor(this::addAuthHeader)" +
                                    ".build();\n", OkHttpClient.class)
                            .add("this._retrofit = new $T.Builder()" +
                                    ".client(_client)" +
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
                    .build())
                .addMethod(MethodSpec.methodBuilder("addAuthHeader")
                        .addParameter(Interceptor.Chain.class, "_chain")
                        .addException(IOException.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Response.class)
                        .addCode("$T _request = _chain.request().newBuilder()" +
                                ".addHeader($S, this._auth).build();\n", Request.class, "X-Auth-Token")
                        .addCode("return _chain.proceed(_request);\n").build());

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
            }else if(subElement.getAnnotation(UmSyncOutgoing.class) != null) {
                dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                        .addCode("throw new RuntimeException($S);\n",
                                "Cannot run outgoing sync on repository. Must be outgoing from database")
                        .build());
            }else if(subElement.getAnnotation(UmSyncCountLocalPendingChanges.class) != null) {
                dbRepoBuilder.addMethod(MethodSpec.overriding(subElement)
                        .addCode("throw new RuntimeException($S);\n",
                                "Cannot check number of pending local changes using repository. Must use database.")
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

        TypeMirror inputStreamType = processingEnv.getElementUtils().getTypeElement(
                InputStream.class.getName()).asType();


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

            for(VariableElement param : method.getParameters()) {
                if(param.asType().equals(inputStreamType)) {
                    methodBuilder.addAnnotation(Multipart.class);
                    break;
                }
            }

            Map<TypeMirror, TypeMirror> substituteClasses = new HashMap<>();
            substituteClasses.put(
                processingEnv.getElementUtils().getTypeElement(InputStream.class.getName()).asType(),
                processingEnv.getElementUtils().getTypeElement(RequestBody.class.getName()).asType());
            addJaxWsParameters(method, daoType, methodBuilder, Query.class, Body.class, Part.class,
                    substituteClasses, false);
            TypeName resultTypeName = TypeName.get(methodInfo.resolveResultEntityType());
            if(resultTypeName.isPrimitive()) {
                resultTypeName = resultTypeName.box();
            }else if(resultTypeName.equals(TypeName.VOID)) {
                resultTypeName = ClassName.get(Void.class);
            }else if(resultTypeName.equals(ClassName.get(InputStream.class))) {
                resultTypeName = ClassName.get(ResponseBody.class);
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

        /*
         * Sometimes a non-abstract implemented method might be annotated as DELEGATE_TO_WEBSERVICE
         * (e.g. for code that should always run on the server side). When creating the repository
         * this needs to be overriden, so it will use retrofit to call the method on the server side.
         */
        for(Element restAccessibleMethod : restAccessibleMethods) {
            if(restAccessibleMethod.getAnnotation(UmRepository.class) != null
                && restAccessibleMethod.getAnnotation(UmRepository.class).delegateType() ==
                        UmRepository.UmRepositoryMethodType.DELEGATE_TO_WEBSERVICE) {
                ExecutableElement methodToOverride = (ExecutableElement)restAccessibleMethod;
                if(!listContainsMethod(methodToOverride, repoMethodsToImplement, daoType))
                    repoMethodsToImplement.add(methodToOverride);
            }
        }

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
                }else if(repoMethod.getAnnotation(UmSyncFindAllChanges.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmSyncFindLocalChanges.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmSyncCheckIncomingCanUpdate.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmSyncCheckIncomingCanInsert.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmSyncCountLocalPendingChanges.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmDbGetAttachment.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmDbSetAttachment.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO;
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


            TypeMirror entityParamComponentType = methodInfo.isUpdateOrInsert() ?
                    methodInfo.resolveEntityParameterComponentType() : null;

            if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO
                    && repoMethod.getAnnotation(UmUpdate.class) != null
                    && DbProcessorUtils.entityHasChangeSequenceNumbers(
                            methodInfo.resolveEntityParameterComponentType(), processingEnv)) {
                codeBlock.add(generateUpdateSetChangeSeqNumSection(repoMethod, daoType,
                        "_syncableDb").build());
            }

            boolean isUpdateOrInsertSetLastChangedBy = methodInfo.isUpdateOrInsert() &&
                    ((repoMethod.getAnnotation(UmUpdate.class) != null
                            && !repoMethod.getAnnotation(UmUpdate.class).preserveLastChangedBy())
                    || (repoMethod.getAnnotation(UmInsert.class) != null
                            && !repoMethod.getAnnotation(UmInsert.class).preserveLastChangedBy()));

            if((repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO
                        || repoMethodMode == UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO)
                    && isUpdateOrInsertSetLastChangedBy
                    && DbProcessorUtils.entityHasChangeSequenceNumbers(entityParamComponentType,
                    processingEnv)) {

                codeBlock.add(generateSetLastChangedBy(repoMethod, daoType, "_syncableDb"));
            }



            if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO
                    || repoMethodMode == UmRepository.UmRepositoryMethodType.DELEGATE_TO_DAO) {


                if(!repoMethod.getReturnType().getKind().equals(TypeKind.VOID)) {
                    codeBlock.add("return ");
                }

                codeBlock.add("_dao.$L$L;\n", repoMethod.getSimpleName(),
                        makeNamedParameterMethodCall(repoMethod.getParameters()));

            }else if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .DELEGATE_TO_WEBSERVICE) {

                //InputStream parameters (e.g. file uploads) are passed as RequestBody
                Map<String, String> paramNameSubstitutions = new HashMap<>();
                for(VariableElement param : repoMethod.getParameters()) {
                    if(param.asType().equals(inputStreamType)) {
                        codeBlock.add("$1T $2L_baos = new $1T();\n",
                                ByteArrayOutputStream.class, param.getSimpleName())
                                .add("int $L_bytesRead;\n", param.getSimpleName())
                                .add("byte[] $L_buf = new byte[1024];\n", param.getSimpleName())
                                .beginControlFlow("while(($1L_bytesRead = $1L.read($1L_buf)) != -1)",
                                        param.getSimpleName())
                                    .add("$1L_baos.write($1L_buf, 0, $1L_bytesRead);\n",
                                            param.getSimpleName())
                                .endControlFlow()
                                .add("$1T $2L_requestBody = $1T.create($3T.parse($4S), $2L_baos.toByteArray());\n",
                                        RequestBody.class, param.getSimpleName(),
                                        MediaType.class,
                                        "application/octet-stream");
                        paramNameSubstitutions.put(param.getSimpleName().toString(),
                                param.getSimpleName().toString() + "_requestBody");
                    }
                }

                TypeMirror callResultType = DbProcessorUtils.boxIfPrimitive(
                        methodInfo.resolveResultEntityType(), processingEnv);

                boolean isInputStreamResult = false;
                if(callResultType.equals(inputStreamType)) {
                    callResultType = processingEnv.getElementUtils().getTypeElement(
                            ResponseBody.class.getName()).asType();
                    isInputStreamResult = true;
                }

                codeBlock.add("$T<$T> _call = _webService.$L$L;\n", Call.class,
                        callResultType,
                        repoMethod.getSimpleName().toString(),
                        makeNamedParameterMethodCall(repoMethod.getParameters(),
                                paramNameSubstitutions, umCallbackTypeElement));

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
                        codeBlock.add("$T<$T> _response = _call.execute();\n",
                                    retrofit2.Response.class, callResultType)
                                .add("$T _result = _response.body();\n", callResultType);
                        if(!isInputStreamResult) {
                            codeBlock.add("return _result;\n");
                        }else {
                            codeBlock.add("return _result.byteStream();\n");
                        }
                    }else {
                        codeBlock.add("_call.execute();\n");
                    }

                    TypeMirror ioExceptionType = processingEnv.getElementUtils().getTypeElement(
                            IOException.class.getName()).asType();
                    codeBlock.nextControlFlow("catch($T _ioe)", IOException.class)
                            .add("_ioe.printStackTrace();\n");

                    boolean throwsIoException = repoMethod.getThrownTypes().contains(ioExceptionType);
                    if(throwsIoException)
                        codeBlock.add("throw _ioe;\n");

                    codeBlock.endControlFlow();

                    if(!isVoid && !throwsIoException)
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
