package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

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
                .addField(retrofitTypeName, "_retrofit")
                .addField(ClassName.get(dbType), "_db")
                .addField(ClassName.get(daoType), "_dao")
                .addField(ClassName.get(dbType), "_repo")
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Retrofit.class, "_retrofit")
                    .addParameter(ClassName.get(dbType), "_db")
                    .addParameter(ClassName.get(daoType), "_dao")
                    .addParameter(ClassName.get(dbType), "_repo")
                    .addCode(CodeBlock.builder()
                            .add("this._retrofit = _retrofit.create($L.class);\n",
                                    retrofitInterfaceName)
                            .add("this._db = _db;\n")
                            .add("this._dao = _dao;\n")
                            .add("this._repo = _repo;\n").build())
                    .build());

        JavaFile repoJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                repoBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(repoJavaFile, destination);
    }
}
