package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmQuery;
import com.ustadmobile.lib.database.annotation.UmRepository;
import com.ustadmobile.lib.database.annotation.UmSyncIncoming;
import com.ustadmobile.lib.db.retrofit.RetrofitUmCallbackAdapter;
import com.ustadmobile.lib.db.sync.UmSyncableDatabase;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_RETROFIT_OUTPUT;

public class DbProcessorRetrofitRepository extends AbstractDbProcessor {

    public static final String POSTFIX_RETROFIT_INTERFACE = "_Retrofit";

    public static final String POSTFIX_REPOSITORY_DAO = "_RetrofitRepository";

    public DbProcessorRetrofitRepository() {
        setOutputDirOpt(OPT_RETROFIT_OUTPUT);
    }


    @Override
    public void processDbClass(TypeElement dbType, String destination) throws IOException {

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
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
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

        List<ExecutableElement> repoMethodsToImplement = findDaoMethodsToImplement(daoType);

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
                }else if(repoMethod.getAnnotation(UmQuery.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType
                            .DELEGATE_TO_DAO;
                }else if(repoMethod.getAnnotation(UmSyncIncoming.class) != null) {
                    repoMethodMode = UmRepository.UmRepositoryMethodType
                            .DELEGATE_TO_WEBSERVICE;
                }
            }

            if(repoMethodMode == -1)
                continue;


            codeBlock.add("$1T _syncableDb = ($1T)_db;\n", UmSyncableDatabase.class);


            if(repoMethodMode == UmRepository.UmRepositoryMethodType
                    .INCREMENT_CHANGE_SEQ_NUMS_THEN_DELEGATE_TO_DAO) {
                codeBlock.add(generateIncrementChangeSeqNumsCodeBlock(methodInfo.resolveEntityParameterType(),
                        methodInfo.getEntityParameterElement().getSimpleName().toString(),
                        "_db", "_syncableDb"));
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
                codeBlock.add("$T<$T> _call = _webService.$L$L;\n", Call.class,
                        methodInfo.resolveResultEntityType(),
                        repoMethod.getSimpleName().toString(),
                        makeNamedParameterMethodCall(repoMethod.getParameters(),
                                umCallbackTypeElement));

                if(methodInfo.isAsyncMethod()){
                    codeBlock.add("_webService.$L$L.enqueue(new $T<$T>($L));\n",
                            repoMethod.getSimpleName(),
                            makeNamedParameterMethodCall(repoMethod.getParameters(),
                                    umCallbackTypeElement),
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

            methodBuilder.addCode(codeBlock.build());
            repoBuilder.addMethod(methodBuilder.build());
        }


        JavaFile repoJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                repoBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(repoJavaFile, destination);
    }

}
