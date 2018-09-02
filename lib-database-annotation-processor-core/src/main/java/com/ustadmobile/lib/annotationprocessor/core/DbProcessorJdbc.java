package com.ustadmobile.lib.annotationprocessor.core;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.ustadmobile.lib.database.annotation.UmDatabase;
import com.ustadmobile.lib.database.annotation.UmIndexField;
import com.ustadmobile.lib.database.annotation.UmPrimaryKey;
import com.ustadmobile.lib.database.jdbc.JdbcDatabaseUtils;
import com.ustadmobile.lib.database.jdbc.UmJdbcDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.tools.Diagnostic;

import static com.ustadmobile.lib.annotationprocessor.core.DbProcessorCore.OPT_JDBC_OUTPUT;

public class DbProcessorJdbc extends AbstractDbProcessor {

    private static String SUFFIX_JDBC_DBMANAGER = "_Jdbc";

    private static final String SUFFIX_JDBC_DAO = "_JdbcDaoImpl";

    public DbProcessorJdbc() {
        setOutputDirOpt(OPT_JDBC_OUTPUT);
    }

    public void processDbClass(TypeElement dbType, File destinationDir) throws IOException {
        String jdbcDbClassName = dbType.getSimpleName() + SUFFIX_JDBC_DBMANAGER;
        PackageElement packageElement = processingEnv.getElementUtils().getPackageOf(dbType);

        ClassName initialContextClassName = ClassName.get(InitialContext.class);
        TypeSpec.Builder jdbcDbTypeSpec = TypeSpec.classBuilder(jdbcDbClassName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(dbType))
                .addJavadoc("Generated code - DO NOT EDIT!\n")
                .addField(ClassName.get(Object.class), "_context")
                .addField(ClassName.get(DataSource.class), "_dataSource")
                .addField(ClassName.get(Connection.class), "_connection")
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(TypeName.get(Object.class), "context")
                    .addParameter(TypeName.get(String.class), "dbName")
                    .addCode(CodeBlock.builder().add("\tthis._context = context;\n")
                        .beginControlFlow("try ")
                            .add("$T iContext = new $T();\n", initialContextClassName, initialContextClassName)
                            .add("this._dataSource = (DataSource)iContext.lookup(\"java:/comp/env/jdbc/\"+dbName);\n")
                            .add("this._connection = _dataSource.getConnection();\n")
                            .add("createAllTables(_connection);\n")
                        .endControlFlow()
                        .beginControlFlow("catch($T|$T e)",
                                ClassName.get(NamingException.class), ClassName.get(SQLException.class))
                            .add("throw new RuntimeException(e);\n")
                        .endControlFlow()
                        .build())
                .build());

        TypeSpec.Builder factoryClassSpec = DbProcessorUtils.makeFactoryClass(dbType, jdbcDbClassName);

        addCreateTablesMethodToClassSpec(dbType, jdbcDbTypeSpec);

        for(Element subElement : dbType.getEnclosedElements()) {
            if (subElement.getKind() != ElementKind.METHOD)
                continue;

            ExecutableElement dbMethod = (ExecutableElement)subElement;
            if(dbMethod.getModifiers().contains(Modifier.STATIC))
                continue;


            MethodSpec.Builder overrideSpec =  MethodSpec.overriding(dbMethod);
            TypeKind returnTypeKind = dbMethod.getReturnType().getKind();
            if(returnTypeKind.equals(TypeKind.DECLARED)) {
                overrideSpec.addCode("return null;\n");
            }else if(returnTypeKind.equals(TypeKind.LONG) || returnTypeKind.equals(TypeKind.INT)) {
                overrideSpec.addCode("return 0;\n");
            }

            jdbcDbTypeSpec.addMethod(overrideSpec.build());

        }



        JavaFile.builder(packageElement.getQualifiedName().toString(), factoryClassSpec.build())
                .build().writeTo(destinationDir);
        JavaFile.builder(packageElement.getQualifiedName().toString(), jdbcDbTypeSpec.build())
                .build().writeTo(destinationDir);
    }

    protected void addCreateTablesMethodToClassSpec(TypeElement dbType, TypeSpec.Builder classBuilder) {
        List<? extends AnnotationMirror> annotationMirrors = dbType.getAnnotationMirrors();
        Element umDbElement = processingEnv.getElementUtils()
                .getPackageElement(UmDatabase.class.getCanonicalName());

        Map<? extends ExecutableElement, ? extends AnnotationValue> annotationEntryMap =
                dbType.getAnnotationMirrors().get(0).getElementValues();
        for(Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationEntryMap.entrySet()) {
            String key = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            if (key.equals("entities")) {
                List<? extends AnnotationValue> typeMirrors =
                        (List<? extends AnnotationValue>) value;
                MethodSpec.Builder createMethod = MethodSpec.methodBuilder("createAllTables");
                createMethod.addParameter(ClassName.get(Connection.class), "con");
                CodeBlock.Builder createCb = CodeBlock.builder();
                createCb.beginControlFlow("try")
                    .add("$T _existingTableNames = $T.getTableNames(_connection);\n",
                        ParameterizedTypeName.get(ClassName.get(List.class), ClassName.get(String.class)),
                            ClassName.get(JdbcDatabaseUtils.class))
                    .add("$T _stmt = con.createStatement();\n", ClassName.get(Statement.class));
                for(AnnotationValue entityValue : typeMirrors) {
                    TypeElement entityTypeElement = (TypeElement)processingEnv.getTypeUtils()
                            .asElement((TypeMirror)entityValue.getValue());
                    addCreateTableStatements(createCb, "_stmt", entityTypeElement,
                            '`');
                }
                createCb.add("_stmt.close();\n");

                createCb.endControlFlow() //end try/catch control flow
                    .beginControlFlow("catch($T e)\n", SQLException.class)
                    .add("throw new $T(e);\n", RuntimeException.class)
                    .endControlFlow();



                createMethod.addCode(createCb.build());
                classBuilder.addMethod(createMethod.build());
            }
        }
    }

    /**
     * Generates code that will execute CREATE TABLE and CREATE INDEX as required for the given
     * entity.
     *
     * @param codeBlock CodeBlock.Builder that this code will be added to
     * @param stmtVariableName Name of the SQL Statement object variable in the CodeBlock
     * @param entitySpec The TypeElement representing the entity for which the statements are being generated
     * @param quoteChar The quote char used to contain SQL table names e.g. '`' for MySQL and Sqlite
     */
    protected void addCreateTableStatements(CodeBlock.Builder codeBlock, String stmtVariableName,
                                                 TypeElement entitySpec, char quoteChar) {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("CREATE TABLE IF NOT EXISTS ").append(quoteChar)
                .append(entitySpec.getSimpleName()).append(quoteChar)
                .append(" (");

        List<? extends Element> subElementList = entitySpec.getEnclosedElements();


        boolean fieldVariablesStarted = false;
        Map<String, List<String>> indexes = new HashMap<>();
        for(int i = 0; i < subElementList.size(); i++) {
            if(subElementList.get(i).getKind() != ElementKind.FIELD)
                continue;

            if(fieldVariablesStarted)
                sbuf.append(", ");

            VariableElement fieldVariable = (VariableElement)subElementList.get(i);
            UmPrimaryKey primaryKeyAnnotation = fieldVariable.getAnnotation(UmPrimaryKey.class);



            if(fieldVariable.getAnnotation(UmIndexField.class) != null) {
                indexes.put("index_" + entitySpec.getSimpleName() + '_' + fieldVariable.getSimpleName(),
                        Collections.singletonList(fieldVariable.getSimpleName().toString()));
            }

            sbuf.append(quoteChar).append(fieldVariable.getSimpleName().toString())
                    .append(quoteChar).append(' ').append(makeSqlTypeDeclaration(fieldVariable));

            if(primaryKeyAnnotation!= null) {
                sbuf.append(" PRIMARY KEY ");
                if(primaryKeyAnnotation.autoIncrement())
                    sbuf.append(" AUTOINCREMENT ");
                sbuf.append(" NOT NULL ");
            }

            fieldVariablesStarted = true;
        }

        sbuf.append(')');


        codeBlock.beginControlFlow("if(!_existingTableNames.contains($S))",
                entitySpec.getSimpleName().toString())
                .add("$L.executeUpdate($S);\n", stmtVariableName, sbuf.toString());
        for(Map.Entry<String, List<String>> index : indexes.entrySet()) {
            Map<String, String> formatArgs = new HashMap<>();
            formatArgs.put("quot", String.valueOf(quoteChar));
            formatArgs.put("index_name", index.getKey());
            formatArgs.put("table_name", entitySpec.getSimpleName().toString());
            formatArgs.put("stmt", stmtVariableName);
            boolean indexFieldCommaNeeded = false;
            StringBuffer indexFieldBuffer = new StringBuffer();
            for(String fieldName : index.getValue()) {
                if(indexFieldCommaNeeded)
                    indexFieldBuffer.append(',');

                indexFieldCommaNeeded = true;
                indexFieldBuffer.append(quoteChar).append(fieldName).append(quoteChar).append(' ');
            }

            formatArgs.put("index_fields", indexFieldBuffer.toString());
            codeBlock.addNamed("$stmt:L.executeUpdate(\"CREATE INDEX $quot:L$index_name:L$quot:L ON $quot:L$table_name:L$quot:L ($index_fields:L)\");\n",
                    formatArgs);
        }

        codeBlock.endControlFlow();
    }

    protected String makeSqlTypeDeclaration(VariableElement field) {
        TypeMirror fieldType = field.asType();

        switch(fieldType.getKind()) {
            case BOOLEAN:
                return "BOOL";

            case INT:
                return "INTEGER";

            case LONG:
                return "BIGINT";

            case FLOAT:
                return "FLOAT";

            case DOUBLE:
                return "DOUBLE";

            case DECLARED:
                Element typeEl = processingEnv.getTypeUtils().asElement(fieldType);
                if(processingEnv.getElementUtils().getTypeElement("java.lang.String")
                        .equals(typeEl)) {
                    return "TEXT";
                }else if(processingEnv.getElementUtils().getTypeElement("java.lang.Integer")
                    .equals(typeEl)) {
                    return "INTEGER";
                }else if(processingEnv.getElementUtils().getTypeElement("java.lang.Long")
                    .equals(typeEl)) {
                    return "BIGINT";
                }

                break;
        }

        //didn't recognize that.
        messager.printMessage(Diagnostic.Kind.ERROR,
                "Could not determine SQL data type for field: " + field.getEnclosingElement() +
                "." + field.getSimpleName().toString());

        return null;
    }


    @Override
    public void processDbDao(TypeElement daoClass, File destinationDir) throws IOException {
        String daoClassName = daoClass.getSimpleName() + SUFFIX_JDBC_DAO;
        TypeSpec.Builder jdbcDaoClassSpec = TypeSpec.classBuilder(daoClassName)
                .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                .superclass(ClassName.get(daoClass))
                .addField(ClassName.get(UmJdbcDatabase.class), "_db", Modifier.PRIVATE)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(UmJdbcDatabase.class), "_db")
                    .addCode("this._db = _db;\n").build());


        JavaFile.builder(processingEnv.getElementUtils().getPackageOf(daoClass).toString(),
                jdbcDaoClassSpec.build()).build().writeTo(destinationDir);
    }
}
