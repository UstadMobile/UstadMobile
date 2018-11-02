package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.core.db.UmObserver;
import com.ustadmobile.core.impl.UmCallback;
import com.ustadmobile.lib.database.annotation.UmRestAccessible;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JERSEY_RESOURCE_OUT;

public class DbProcessorJerseyResource extends AbstractDbProcessor {

    public static final String POSTFIX_RESUORCE = "_Resource";

    public static final String FIELDNAME_SERVLET_CONTEXT = "_servletContext";

    public static final int ASYNC_TIMEOUT_DEFAULT = 5000;


    public DbProcessorJerseyResource() {
        setOutputDirOpt(OPT_JERSEY_RESOURCE_OUT);
    }

    @Override
    public void processDbClass(TypeElement dbType, String destination) throws IOException {

    }

    @Override
    public void processDbDao(TypeElement daoType, TypeElement dbType, String destination) throws IOException {
        List<Class<? extends Annotation>> annotationList = new ArrayList<>();
        annotationList.add(UmRestAccessible.class);

        List<Element> resutList = new ArrayList<>();
        List<Element> annotatedElementList =DbProcessorUtils.findElementsWithAnnotation(daoType,
                        annotationList, resutList, 0, processingEnv);

        TypeSpec.Builder resBuilder = TypeSpec.classBuilder(daoType.getSimpleName().toString() +
                POSTFIX_RESUORCE)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value",
                "$S", daoType.getSimpleName().toString()).build())
                .addField(FieldSpec.builder(ClassName.get("javax.servlet","ServletContext"),
                        FIELDNAME_SERVLET_CONTEXT).addAnnotation(Context.class).build());

        for(Element annotatedElement : annotatedElementList) {
            ExecutableElement methodElement = (ExecutableElement) annotatedElement;
            DaoMethodInfo methodInfo = new DaoMethodInfo(methodElement, daoType, processingEnv);
            TypeName resultTypeName = TypeName.get(methodInfo.resolveEntityType());
            boolean primitiveToStringResult = false;
            boolean isVoidResult = isVoid(methodInfo.resolveResultType());

            if (resultTypeName.isPrimitive() || resultTypeName.isBoxedPrimitive()) {
                resultTypeName = ClassName.get(String.class);
                primitiveToStringResult = true;
            }

            if (resultTypeName.equals(ClassName.get(Void.class))) {
                resultTypeName = TypeName.VOID;
            }


            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(
                    methodElement.getSimpleName().toString())
                    .addModifiers(Modifier.PUBLIC)
                    .returns(resultTypeName)
                    .addAnnotation(AnnotationSpec.builder(Path.class).addMember("value",
                            "\"/$L\"", methodElement.getSimpleName().toString()).build());

            addJaxWsParameters(methodElement, daoType, methodBuilder);
            addJaxWsMethodAnnotations(methodElement, daoType, methodBuilder);

            ExecutableElement daoGetter = DbProcessorUtils.findDaoGetter(daoType, dbType,
                    processingEnv);
            if (daoGetter == null) {
                messager.printMessage(Diagnostic.Kind.ERROR,
                        formatMethodForErrorMessage(methodElement, daoType) + " : cannot find database getter method");
                continue;
            }

            CodeBlock.Builder codeBlock = CodeBlock.builder()
                    .add("$1T _db = $1T.getInstance($2L);\n", dbType, FIELDNAME_SERVLET_CONTEXT)
                    .add("$T _dao = _db.$L();\n", daoType,
                            daoGetter.getSimpleName());

            //TODO: this may need extra annotations
            if(methodInfo.isUpdateOrInsert()) {
                codeBlock.add(generateIncrementChangeSeqNumsCodeBlock(methodInfo.getEntityParameterElement().asType(),
                        methodInfo.getEntityParameterElement().getSimpleName().toString(), "_db",
                        null));
            }


            if (methodInfo.isAsyncMethod()) {
                codeBlock.add("$1T _latch = new $1T(1);\n", CountDownLatch.class);
                if (!isVoidResult)
                    codeBlock.add("$1T<$2T> _resultRef = new $1T<>();\n", AtomicReference.class,
                            methodInfo.resolveResultType());


                codeBlock.add("_dao.$L(", methodElement.getSimpleName());
                int paramCount = 0;
                for (VariableElement param : methodElement.getParameters()) {
                    if (paramCount > 0)
                        codeBlock.add(",");
                    if (!umCallbackTypeElement.equals(processingEnv.getTypeUtils()
                            .asElement(param.asType()))) {
                        codeBlock.add(param.getSimpleName().toString());
                    } else {
                        CodeBlock.Builder onSuccessCode = CodeBlock.builder();
                        if (!isVoidResult)
                            onSuccessCode.add("_resultRef.set(_result);\n");

                        onSuccessCode.add("_latch.countDown();\n");

                        TypeSpec callbackTypeSpec = TypeSpec.anonymousClassBuilder("")
                                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(UmCallback.class),
                                        TypeName.get(methodInfo.resolveResultType())))
                                .addMethod(
                                        MethodSpec.methodBuilder("onSuccess")
                                                .addParameter(TypeName.get(methodInfo.resolveResultType()), "_result")
                                                .addModifiers(Modifier.PUBLIC)
                                                .addCode(onSuccessCode.build()).build())
                                .addMethod(
                                        MethodSpec.methodBuilder("onFailure")
                                                .addModifiers(Modifier.PUBLIC)
                                                .addParameter(Throwable.class, "_throwable")
                                                .addCode("_latch.countDown();\n").build()).build();
                        codeBlock.add("$L", callbackTypeSpec);

                    }

                    paramCount++;
                }

                codeBlock.add(");\n")
                        .beginControlFlow("try")
                        .add("_latch.await($L, $T.MILLISECONDS);\n", ASYNC_TIMEOUT_DEFAULT,
                                TimeUnit.class)
                        .nextControlFlow("catch($T _e)", InterruptedException.class)
                        .endControlFlow();
                if (!isVoidResult) {
                    codeBlock.add("return ");
                    if(primitiveToStringResult)
                        codeBlock.add("String.valueOf(");

                    codeBlock.add("_resultRef.get()");
                    if(primitiveToStringResult)
                        codeBlock.add(")");

                    codeBlock.add(";\n");
                }
            }else if(methodInfo.isLiveDataReturn()) {
                TypeMirror liveDataType = methodInfo.resolveEntityType();
                codeBlock.add("$1T _latch = new $1T(1);\n", CountDownLatch.class)
                    .add("$1T<$2T> _resultRef = new $1T<>();\n", AtomicReference.class,
                            liveDataType)
                    .add("$T _liveData = _dao.$L",
                        methodInfo.resolveResultType(), methodElement.getSimpleName())
                    .add(makeNamedParameterMethodCall(methodElement.getParameters()))
                    .add(";\n")
                    .beginControlFlow("$T<$T> _observer = (_value) -> ",
                        UmObserver.class, liveDataType)
                        .add("_resultRef.set(_value);\n")
                        .add("_latch.countDown();\n")
                    .endControlFlow(" ")
                    .add("_liveData.observeForever(_observer);\n")
                    .beginControlFlow("try")
                        .add("_latch.await($L, $T.MILLISECONDS);\n", ASYNC_TIMEOUT_DEFAULT,
                                TimeUnit.class)
                    .nextControlFlow("catch($T _e)", InterruptedException.class)
                        .add("_e.printStackTrace();\n")
                    .endControlFlow()
                    .add("_liveData.removeObserver(_observer);\n")
                    .add("return _resultRef.get();\n");
            }else {
                if(!isVoidResult)
                    codeBlock.add("return ");

                if(primitiveToStringResult)
                    codeBlock.add("String.valueOf(");

                codeBlock.add("_dao.$L", methodElement.getSimpleName());
                codeBlock.add(makeNamedParameterMethodCall(methodElement.getParameters()));

                if(primitiveToStringResult)
                    codeBlock.add(")");

                codeBlock.add(";\n");
            }

            methodBuilder.addCode(codeBlock.build());
            resBuilder.addMethod(methodBuilder.build());
        }


        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(daoType);
        JavaFile databaseJavaFile = JavaFile.builder(packageElement.getQualifiedName().toString(),
                resBuilder.build())
                .indent("    ").build();
        writeJavaFileToDestination(databaseJavaFile, destination);

    }

    protected void addJaxWsParameters(ExecutableElement method, TypeElement clazzDao,
                                      MethodSpec.Builder methodBuilder) {

        for(VariableElement param : method.getParameters()) {
            if(umCallbackTypeElement.equals(processingEnv.getTypeUtils().asElement(param.asType())))
                continue;

            ParameterSpec.Builder paramSpec = ParameterSpec.builder(TypeName.get(
                    DbProcessorUtils.resolveType(param.asType(), clazzDao, processingEnv)),
                    param.getSimpleName().toString());

            if(DbProcessorUtils.isQueryParam(param.asType(), processingEnv)) {
                paramSpec.addAnnotation(AnnotationSpec.builder(QueryParam.class)
                        .addMember("value", "$S", param.getSimpleName().toString()).build());
            }

            methodBuilder.addParameter(paramSpec.build());
        }
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

        if(numNonQueryParams == 1) {
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
        TypeMirror resultType = methodInfo.resolveEntityType();
        TypeName resultTypeName = TypeName.get(resultType);
        TypeElement stringTypeEl = processingEnv.getElementUtils().getTypeElement(
                String.class.getName());
        String producesFormat;

        if(!isVoid(resultType)) {
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

}
